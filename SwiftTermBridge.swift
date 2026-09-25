import SwiftUI
import UIKit
import SwiftTerm

// Disambiguate Color between SwiftUI and SwiftTerm
typealias Color = SwiftUI.Color

// MARK: - UIKit TerminalView Wrapper

struct SwiftTermView: UIViewRepresentable {
    @ObservedObject var pty: SSHTerminalPTY
    var fontSize: CGFloat = 13
    var colorScheme: TerminalColorScheme = .dracula
    
    func makeCoordinator() -> Coordinator {
        Coordinator(pty: pty)
    }
    
    func makeUIView(context: Context) -> TerminalView {
        let tv = TerminalView(frame: .zero)
        tv.terminalDelegate = context.coordinator
        context.coordinator.terminalView = tv
        
        // Set font
        let font = UIFont.monospacedSystemFont(ofSize: fontSize, weight: .regular)
        tv.font = font
        
        // Apply color scheme
        applyColorScheme(tv, scheme: colorScheme)
        
        // Allow input
        tv.becomeFirstResponder()
        
        // Hook up PTY data feed
        pty.onDataReceived = { bytes in
            DispatchQueue.main.async {
                tv.feed(byteArray: ArraySlice(bytes))
            }
        }
        
        pty.onDisconnected = {
            DispatchQueue.main.async {
                tv.feed(text: "\r\n\r\n[Sesja zakończona]\r\n")
            }
        }
        
        return tv
    }
    
    func updateUIView(_ uiView: TerminalView, context: Context) {
        // Update font if changed
        let font = UIFont.monospacedSystemFont(ofSize: fontSize, weight: .regular)
        uiView.font = font
        applyColorScheme(uiView, scheme: colorScheme)
    }
    
    private func applyColorScheme(_ tv: TerminalView, scheme: TerminalColorScheme) {
        let bg: UIColor
        let fg: UIColor
        switch scheme {
        case .standard:
            bg = UIColor(red: 0, green: 0, blue: 0, alpha: 1)
            fg = UIColor(red: 0.8, green: 0.8, blue: 0.8, alpha: 1)
        case .dracula:
            bg = UIColor(red: 0.16, green: 0.16, blue: 0.21, alpha: 1)
            fg = UIColor(red: 0.97, green: 0.97, blue: 0.95, alpha: 1)
        case .nord:
            bg = UIColor(red: 0.18, green: 0.20, blue: 0.25, alpha: 1)
            fg = UIColor(red: 0.85, green: 0.87, blue: 0.91, alpha: 1)
        case .solarized:
            bg = UIColor(red: 0.0, green: 0.17, blue: 0.21, alpha: 1)
            fg = UIColor(red: 0.51, green: 0.58, blue: 0.59, alpha: 1)
        case .monokai:
            bg = UIColor(red: 0.15, green: 0.16, blue: 0.13, alpha: 1)
            fg = UIColor(red: 0.97, green: 0.97, blue: 0.95, alpha: 1)
        case .oneDark:
            bg = UIColor(red: 0.16, green: 0.18, blue: 0.22, alpha: 1)
            fg = UIColor(red: 0.67, green: 0.71, blue: 0.78, alpha: 1)
        }
        tv.nativeBackgroundColor = bg
        tv.nativeForegroundColor = fg
    }
    
    @MainActor
    class Coordinator: NSObject, TerminalViewDelegate {
        let pty: SSHTerminalPTY
        weak var terminalView: TerminalView?
        
        init(pty: SSHTerminalPTY) {
            self.pty = pty
        }
        
        // MARK: - Required TerminalViewDelegate Methods
        
        // 1. User typed something -> send to SSH stdin
        func send(source: TerminalView, data: ArraySlice<UInt8>) {
            pty.sendData(Array(data))
        }
        
        // 2. Terminal resized
        func sizeChanged(source: TerminalView, newCols: Int, newRows: Int) {
            pty.resizeTerminal(cols: newCols, rows: newRows)
        }
        
        // 3. Terminal title changed
        func setTerminalTitle(source: TerminalView, title: String) {}
        
        // 4. Scrolled
        func scrolled(source: TerminalView, position: Double) {}
        
        // 5. Host directory update (OSC 7)
        func hostCurrentDirectoryUpdate(source: TerminalView, directory: String?) {}
        
        // 6. Hyperlink clicked/tapped (OSC 8)
        func requestOpenLink(source: TerminalView, link: String, params: [String: String]) {
            if let url = URL(string: link) {
                UIApplication.shared.open(url)
            }
        }
        
        // 7. Bell / beep
        func bell(source: TerminalView) {
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(.warning)
        }
        
        // 8. Clipboard copy (OSC 52)
        func clipboardCopy(source: TerminalView, content: Data) {
            UIPasteboard.general.setData(content, forPasteboardType: "public.utf8-plain-text")
        }
        
        // 9. Clipboard read (OSC 52)
        func clipboardRead(source: TerminalView) -> Data? {
            UIPasteboard.general.data(forPasteboardType: "public.utf8-plain-text")
        }
        
        // 10. iTerm content (OSC 1337)
        func iTermContent(source: TerminalView, content: ArraySlice<UInt8>) {}
        
        // 11. Range changed
        func rangeChanged(source: TerminalView, startY: Int, endY: Int) {}
    }
}

// MARK: - Special Keys Toolbar

struct TerminalSpecialKeysToolbar: View {
    let pty: SSHTerminalPTY
    
    // All the special key definitions
    private struct SpecialKey: Identifiable {
        let id = UUID()
        let label: String
        let data: [UInt8]
        let color: SwiftUI.Color
    }
    
    private var controlKeys: [SpecialKey] {
        [
            SpecialKey(label: "Ctrl+C", data: [0x03], color: .red),
            SpecialKey(label: "Ctrl+D", data: [0x04], color: .orange),
            SpecialKey(label: "Ctrl+Z", data: [0x1A], color: .yellow),
            SpecialKey(label: "Ctrl+L", data: [0x0C], color: .cyan),
            SpecialKey(label: "Ctrl+A", data: [0x01], color: .blue),
            SpecialKey(label: "Ctrl+E", data: [0x05], color: .blue),
            SpecialKey(label: "Ctrl+R", data: [0x12], color: .purple),
            SpecialKey(label: "Ctrl+W", data: [0x17], color: .pink),
            SpecialKey(label: "Ctrl+U", data: [0x15], color: .pink),
            SpecialKey(label: "Ctrl+K", data: [0x0B], color: .pink),
        ]
    }
    
    private var controlKeys2: [SpecialKey] {
        [
            SpecialKey(label: "Ctrl+T", data: [0x14], color: .teal),
            SpecialKey(label: "Ctrl+P", data: [0x10], color: .indigo),
            SpecialKey(label: "Ctrl+N", data: [0x0E], color: .indigo),
            SpecialKey(label: "Ctrl+B", data: [0x02], color: .mint),
            SpecialKey(label: "Ctrl+F", data: [0x06], color: .mint),
            SpecialKey(label: "Ctrl+H", data: [0x08], color: .gray),
            SpecialKey(label: "Ctrl+Y", data: [0x19], color: .green),
            SpecialKey(label: "Ctrl+O", data: [0x0F], color: .teal),
            SpecialKey(label: "Ctrl+X", data: [0x18], color: .red),
            SpecialKey(label: "Ctrl+\\", data: [0x1C], color: .red),
        ]
    }
    
    private var navigationKeys: [SpecialKey] {
        [
            SpecialKey(label: "Tab", data: [0x09], color: .green),
            SpecialKey(label: "Esc", data: [0x1B], color: .orange),
            // Arrow keys (ANSI escape sequences)
            SpecialKey(label: "↑", data: [0x1B, 0x5B, 0x41], color: .blue),     // \e[A
            SpecialKey(label: "↓", data: [0x1B, 0x5B, 0x42], color: .blue),     // \e[B
            SpecialKey(label: "←", data: [0x1B, 0x5B, 0x44], color: .blue),     // \e[D
            SpecialKey(label: "→", data: [0x1B, 0x5B, 0x43], color: .blue),     // \e[C
            SpecialKey(label: "Home", data: [0x1B, 0x5B, 0x48], color: .teal),   // \e[H
            SpecialKey(label: "End", data: [0x1B, 0x5B, 0x46], color: .teal),    // \e[F
            SpecialKey(label: "PgUp", data: [0x1B, 0x5B, 0x35, 0x7E], color: .purple), // \e[5~
            SpecialKey(label: "PgDn", data: [0x1B, 0x5B, 0x36, 0x7E], color: .purple), // \e[6~
            SpecialKey(label: "Ins", data: [0x1B, 0x5B, 0x32, 0x7E], color: .gray),   // \e[2~
            SpecialKey(label: "Del", data: [0x1B, 0x5B, 0x33, 0x7E], color: .red),     // \e[3~
        ]
    }
    
    // Alt combinations (ESC + char)
    private var altKeys: [SpecialKey] {
        [
            SpecialKey(label: "Alt+B", data: [0x1B, 0x62], color: .cyan),
            SpecialKey(label: "Alt+F", data: [0x1B, 0x66], color: .cyan),
            SpecialKey(label: "Alt+D", data: [0x1B, 0x64], color: .pink),
            SpecialKey(label: "Alt+.", data: [0x1B, 0x2E], color: .orange),
            SpecialKey(label: "Alt+U", data: [0x1B, 0x75], color: .green),
            SpecialKey(label: "Alt+L", data: [0x1B, 0x6C], color: .green),
            SpecialKey(label: "Alt+T", data: [0x1B, 0x74], color: .teal),
            SpecialKey(label: "Alt+R", data: [0x1B, 0x72], color: .purple),
            SpecialKey(label: "Alt+H", data: [0x1B, 0x68], color: .gray),
            SpecialKey(label: "Alt+⌫", data: [0x1B, 0x7F], color: .red),
        ]
    }
    
    private var functionKeys: [SpecialKey] {
        [
            SpecialKey(label: "F1", data: [0x1B, 0x4F, 0x50], color: .indigo),
            SpecialKey(label: "F2", data: [0x1B, 0x4F, 0x51], color: .indigo),
            SpecialKey(label: "F3", data: [0x1B, 0x4F, 0x52], color: .indigo),
            SpecialKey(label: "F4", data: [0x1B, 0x4F, 0x53], color: .indigo),
            SpecialKey(label: "F5", data: [0x1B, 0x5B, 0x31, 0x35, 0x7E], color: .indigo),
            SpecialKey(label: "F6", data: [0x1B, 0x5B, 0x31, 0x37, 0x7E], color: .indigo),
            SpecialKey(label: "F7", data: [0x1B, 0x5B, 0x31, 0x38, 0x7E], color: .indigo),
            SpecialKey(label: "F8", data: [0x1B, 0x5B, 0x31, 0x39, 0x7E], color: .indigo),
            SpecialKey(label: "F9", data: [0x1B, 0x5B, 0x32, 0x30, 0x7E], color: .indigo),
            SpecialKey(label: "F10", data: [0x1B, 0x5B, 0x32, 0x31, 0x7E], color: .indigo),
            SpecialKey(label: "F11", data: [0x1B, 0x5B, 0x32, 0x33, 0x7E], color: .indigo),
            SpecialKey(label: "F12", data: [0x1B, 0x5B, 0x32, 0x34, 0x7E], color: .indigo),
        ]
    }
    
    var body: some View {
        VStack(spacing: 4) {
            // Row 1: Signals & editing
            keyRow(keys: controlKeys, label: "Ctrl")
            // Row 2: More ctrl
            keyRow(keys: controlKeys2, label: "Ctrl+")
            // Row 3: Navigation
            keyRow(keys: navigationKeys, label: "Nav")
            // Row 4: Alt combinations
            keyRow(keys: altKeys, label: "Alt")
            // Row 5: Function keys
            keyRow(keys: functionKeys, label: "Fn")
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 6)
        .background(SwiftUI.Color.black.opacity(0.85))
    }
    
    @ViewBuilder
    private func keyRow(keys: [SpecialKey], label: String) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 4) {
                Text(label)
                    .font(.system(size: 9, weight: .bold, design: .monospaced))
                    .foregroundColor(.gray)
                    .frame(width: 28)
                
                ForEach(keys) { key in
                    Button(action: {
                        pty.sendData(key.data)
                        // Haptic feedback
                        let generator = UIImpactFeedbackGenerator(style: .light)
                        generator.impactOccurred()
                    }) {
                        Text(key.label)
                            .font(.system(size: 10, weight: .semibold, design: .monospaced))
                            .foregroundColor(key.color)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 5)
                            .background(SwiftUI.Color.white.opacity(0.08))
                            .cornerRadius(5)
                            .overlay(
                                RoundedRectangle(cornerRadius: 5)
                                    .stroke(key.color.opacity(0.3), lineWidth: 0.5)
                            )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 4)
        }
    }
}

// MARK: - Full Terminal Screen (Terminal + Toolbar)

struct InteractiveTerminalView: View {
    @ObservedObject var pty: SSHTerminalPTY
    var fontSize: CGFloat = 13
    var colorScheme: TerminalColorScheme = .dracula
    
    @State private var showingKeys = true
    
    var body: some View {
        VStack(spacing: 0) {
            // Terminal view fills all available space
            SwiftTermView(pty: pty, fontSize: fontSize, colorScheme: colorScheme)
                .ignoresSafeArea(.keyboard)
            
            // Toggle button for special keys
            HStack {
                Button(action: { withAnimation(.easeInOut(duration: 0.2)) { showingKeys.toggle() } }) {
                    HStack(spacing: 4) {
                        Image(systemName: showingKeys ? "keyboard.chevron.compact.down" : "keyboard")
                        Text(showingKeys ? "Ukryj klawisze" : "Klawisze specjalne")
                            .font(.system(size: 10, weight: .medium))
                    }
                    .foregroundColor(.blue)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                }
                
                Spacer()
                
                // Connection status pill
                HStack(spacing: 4) {
                    Circle()
                        .fill(pty.isConnected ? SwiftUI.Color.green : SwiftUI.Color.red)
                        .frame(width: 6, height: 6)
                    Text(pty.statusMessage)
                        .font(.system(size: 9, weight: .medium, design: .monospaced))
                        .foregroundColor(.gray)
                        .lineLimit(1)
                }
                .padding(.trailing, 10)
            }
            .background(SwiftUI.SwiftUI.Color.black.opacity(0.9))
            
            // Special keys toolbar (collapsible)
            if showingKeys {
                TerminalSpecialKeysToolbar(pty: pty)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .background(SwiftUI.Color.black)
    }
}
