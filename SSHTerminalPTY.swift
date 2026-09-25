import Foundation
import SwiftUI
import Citadel
import NIOCore
import NIOSSH
import CryptoKit

/// Manages a live PTY SSH session — full interactive terminal.
@MainActor
final class SSHTerminalPTY: ObservableObject {
    @Published private(set) var isConnected = false
    @Published private(set) var isConnecting = false
    @Published private(set) var statusMessage = "Rozłączono"
    @Published private(set) var stats: ServerResourceStats?
    
    let server: SSHServer
    
    // PTY writer — sends keystrokes to remote shell
    private var stdinWriter: TTYStdinWriter?
    private var citadelClient: Citadel.SSHClient?
    private var sessionTask: Task<Void, Never>?
    private var statsTask: Task<Void, Never>?
    
    // Callback: receives raw bytes from SSH to feed into SwiftTerm TerminalView
    var onDataReceived: (([UInt8]) -> Void)?
    // Callback: connection closed
    var onDisconnected: (() -> Void)?
    
    init(server: SSHServer) {
        self.server = server
    }
    
    // MARK: - Connect with password
    func connect(password: String, cols: Int = 80, rows: Int = 24) async {
        guard !isConnecting, !isConnected else { return }
        isConnecting = true
        statusMessage = "Łączenie z \(server.host)…"
        
        do {
            let host = server.host.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !host.isEmpty else { throw SSHClientError.invalidAddress }
            
            let client = try await Citadel.SSHClient.connect(
                host: host,
                port: server.port,
                authenticationMethod: .passwordBased(username: server.username, password: password),
                hostKeyValidator: .acceptAnything(),
                reconnect: .never
            )
            self.citadelClient = client
            
            isConnected = true
            isConnecting = false
            statusMessage = "Połączono z \(server.name)"
            
            startShellSession(client: client, cols: cols, rows: rows)
            startStatsPolling(client: client)
        } catch {
            isConnecting = false
            statusMessage = "Błąd: \(error.localizedDescription)"
        }
    }
    
    // MARK: - Connect with SSH key
    func connectWithKey(keyName: String, cols: Int = 80, rows: Int = 24) async {
        guard !isConnecting, !isConnected else { return }
        isConnecting = true
        statusMessage = "Łączenie z kluczem SSH…"
        
        do {
            let host = server.host.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !host.isEmpty else { throw SSHClientError.invalidAddress }
            guard let pem = SSHKeychain.loadPrivateKey(name: keyName) else {
                throw SSHClientError.keyImportFailed
            }
            let lines = pem.components(separatedBy: .newlines)
                .filter { !$0.hasPrefix("-----") && !$0.trimmingCharacters(in: .whitespaces).isEmpty }
            guard let b64Data = Data(base64Encoded: lines.joined()),
                  b64Data.count >= 32 else {
                throw SSHClientError.keyImportFailed
            }
            let seed = b64Data.suffix(32)
            let ed25519Key = try Curve25519.Signing.PrivateKey(rawRepresentation: seed)
            
            let client = try await Citadel.SSHClient.connect(
                host: host,
                port: server.port,
                authenticationMethod: .ed25519(username: server.username, privateKey: ed25519Key),
                hostKeyValidator: .acceptAnything(),
                reconnect: .never
            )
            self.citadelClient = client
            
            isConnected = true
            isConnecting = false
            statusMessage = "Połączono z \(server.name)"
            
            startShellSession(client: client, cols: cols, rows: rows)
            startStatsPolling(client: client)
        } catch {
            isConnecting = false
            statusMessage = "Błąd: \(error.localizedDescription)"
        }
    }
    
    // MARK: - Start interactive PTY shell
    private func startShellSession(client: Citadel.SSHClient, cols: Int, rows: Int) {
        sessionTask = Task { [weak self] in
            do {
                try await client.withPTY(
                    SSHChannelRequestEvent.PseudoTerminalRequest(
                        wantReply: true,
                        term: "xterm-256color",
                        terminalCharacterWidth: cols,
                        terminalRowHeight: rows,
                        terminalPixelWidth: 0,
                        terminalPixelHeight: 0,
                        terminalModes: .init([.ECHO: 1])
                    )
                ) { ttyOutput, ttyStdinWriter in
                    await MainActor.run {
                        self?.stdinWriter = ttyStdinWriter
                    }
                    for try await output in ttyOutput {
                        switch output {
                        case .stdout(var buffer):
                            if let bytes = buffer.readBytes(length: buffer.readableBytes) {
                                await MainActor.run {
                                    self?.onDataReceived?(bytes)
                                }
                            }
                        case .stderr(var buffer):
                            if let bytes = buffer.readBytes(length: buffer.readableBytes) {
                                await MainActor.run {
                                    self?.onDataReceived?(bytes)
                                }
                            }
                        }
                    }
                }
            } catch {
                // Session ended
            }
            await MainActor.run {
                self?.isConnected = false
                self?.statusMessage = "Rozłączono"
                self?.onDisconnected?()
            }
        }
    }
    
    // MARK: - Send data from terminal to SSH
    func sendData(_ data: [UInt8]) {
        guard let writer = stdinWriter else { return }
        Task {
            var buffer = ByteBufferAllocator().buffer(capacity: data.count)
            buffer.writeBytes(data)
            try? await writer.write(buffer)
        }
    }
    
    /// Send a string (e.g. control character)
    func sendString(_ string: String) {
        sendData(Array(string.utf8))
    }
    
    /// Send a single control character (e.g. Ctrl+C = 0x03)
    func sendControlChar(_ char: UInt8) {
        sendData([char])
    }
    
    /// Notify server about terminal size change
    func resizeTerminal(cols: Int, rows: Int) {
        guard let writer = stdinWriter else { return }
        Task {
            try? await writer.changeSize(cols: cols, rows: rows, pixelWidth: 0, pixelHeight: 0)
        }
    }
    
    // MARK: - Stats polling (separate executeCommand connection)
    private func startStatsPolling(client: Citadel.SSHClient) {
        statsTask = Task { [weak self] in
            while !Task.isCancelled {
                try? await Task.sleep(for: .seconds(15))
                guard !Task.isCancelled else { break }
                do {
                    let cpuRaw = try await client.executeCommand("top -bn1 2>/dev/null | grep -i 'cpu' | head -1 | grep -oP '[0-9.]+(?=.*id)' || echo '0'")
                    let memRaw = try await client.executeCommand("free -m 2>/dev/null | awk 'NR==2{print $2\" \"$3}' || echo '0 0'")
                    let diskRaw = try await client.executeCommand("df -BG / 2>/dev/null | awk 'NR==2{gsub(/G/,\"\",$2); gsub(/G/,\"\",$3); print $2\" \"$3}' || echo '0 0'")
                    let uptimeRaw = try await client.executeCommand("cat /proc/uptime 2>/dev/null | awk '{print int($1)}' || echo '0'")
                    
                    var cpuBuf = cpuRaw; var memBuf = memRaw; var diskBuf = diskRaw; var uptimeBuf = uptimeRaw
                    let cpuStr = cpuBuf.readString(length: cpuBuf.readableBytes) ?? "0"
                    let memStr = memBuf.readString(length: memBuf.readableBytes) ?? "0 0"
                    let diskStr = diskBuf.readString(length: diskBuf.readableBytes) ?? "0 0"
                    let uptimeStr = uptimeBuf.readString(length: uptimeBuf.readableBytes) ?? "0"
                    
                    let idle = Double(cpuStr.trimmingCharacters(in: .whitespacesAndNewlines)) ?? 0
                    let cpu = max(0, min(100, 100 - idle))
                    let mp = memStr.trimmingCharacters(in: .whitespacesAndNewlines).split(separator: " ")
                    let memTotal = Int(mp.first ?? "0") ?? 0
                    let memUsed = Int(mp.dropFirst().first ?? "0") ?? 0
                    let dp = diskStr.trimmingCharacters(in: .whitespacesAndNewlines).split(separator: " ")
                    let diskTotal = Double(dp.first ?? "0") ?? 0
                    let diskUsed = Double(dp.dropFirst().first ?? "0") ?? 0
                    let uptime = Int(uptimeStr.trimmingCharacters(in: .whitespacesAndNewlines)) ?? 0
                    
                    let newStats = ServerResourceStats(
                        cpuPercent: cpu, memUsedMiB: memUsed, memTotalMiB: memTotal,
                        diskUsedGB: diskUsed, diskTotalGB: diskTotal, uptimeSeconds: uptime
                    )
                    await MainActor.run { self?.stats = newStats }
                } catch {
                    // stats failed silently
                }
            }
        }
    }
    
    // MARK: - Disconnect
    func disconnect() {
        sessionTask?.cancel()
        sessionTask = nil
        statsTask?.cancel()
        statsTask = nil
        stdinWriter = nil
        Task { try? await citadelClient?.close() }
        citadelClient = nil
        isConnected = false
        statusMessage = "Rozłączono"
        onDisconnected?()
    }
    
    deinit {
        sessionTask?.cancel()
        statsTask?.cancel()
    }
}
