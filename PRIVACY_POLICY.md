# Privacy Policy for SSH Mobile

**Last updated:** September 26, 2026

**SSH Mobile** ("the Application") is developed and maintained by **marekfx77** ("we", "our", or "us"). We are committed to protecting your privacy and ensuring the security of your data. This Privacy Policy explains our practices regarding user information.

---

### 1. Zero Personal Data Collection
SSH Mobile **does not collect, store, transmit, or share any personal information**. We do not require registration, accounts, email addresses, phone numbers, or any identifying user data.

### 2. Local Credential Storage & Encryption
- All server configurations, hostnames, IP addresses, usernames, passwords, private SSH keys (Ed25519, RSA), and command snippets are stored **strictly locally on your device**.
- Sensitive data (passwords, private keys, authentication PIN) is encrypted using the Android Keystore system and Android Jetpack `EncryptedSharedPreferences`.
- Your credentials never leave your device, except when transmitted directly over an encrypted SSH connection to the destination server specified by you.

### 3. No Third-Party Analytics or Advertising
- SSH Mobile contains **no third-party advertising SDKs**.
- SSH Mobile contains **no tracking, telemetry, or analytics frameworks** (such as Google Firebase Analytics, Facebook SDK, or third-party crash reporters).

### 4. Permissions Used & Rationale
SSH Mobile requests only the minimum necessary permissions required for core functionality:
- `android.permission.INTERNET`: Required solely to establish secure direct SSH and SFTP connections to the servers you configure.
- `android.permission.ACCESS_NETWORK_STATE`: Used to detect active Wi-Fi or mobile data connections before attempting connection.
- `android.permission.VIBRATE`: Provides subtle haptic feedback when pressing terminal and keypad buttons.

### 5. Security
We value your trust in providing us your information. We implement industry-standard cryptographic practices (SSHv2 protocol, Modern JSch crypto engine with ChaCha20-Poly1305, Ed25519, AES-256) to ensure your remote connections remain secure and tamper-proof.

### 6. Children's Privacy
The Application does not address anyone under the age of 13 and does not knowingly collect personal identifiable information from children.

### 7. Changes to This Privacy Policy
We may update our Privacy Policy from time to time. Any changes will be posted on this page with an updated revision date.

### 8. Contact Us
If you have any questions or suggestions about this Privacy Policy, do not hesitate to contact us at:  
**Email:** support.sshmobile@proton.me
