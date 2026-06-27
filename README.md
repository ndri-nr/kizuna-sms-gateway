# Kizuna: SMS Gateway

Kizuna: SMS Gateway is a robust Android-based bridge that enables seamless communication between mobile SMS services and external web applications. It serves as a dual-purpose gateway: relaying incoming SMS messages to webhooks (Inbound) and providing a REST API to send SMS from external systems (Outbound).

---

## 🚀 Key Features

### 📥 Inbound Gateway (SMS to Webhook)
- **Rule-Based Routing**: Define complex logic to forward SMS based on Sender Regex or keyword "Contains" patterns.
- **Reliable Background Processing**: Uses Android `WorkManager` to ensure SMS messages are processed and delivered even if the app is not in the foreground.
- **Customizable Webhooks**: 
  - Supports `POST`, `PUT`, and `PATCH` methods.
  - Custom JSON templates using `{{placeholder}}` syntax (e.g., `{{message}}`, `{{sender}}`, `{{receivedAt}}`).
  - Webhook-specific and global headers for authentication and tracking.
- **Auto-Reply**: Automatically send a pre-configured SMS response back to the sender upon successful webhook delivery.
- **Catch-all Logic**: Configure rules to handle messages that don't match specific patterns.

### 📤 Outbound Gateway (API to SMS)
- **Local REST API Server**: An embedded HTTP server running as a foreground service on the device.
- **API Key Security**: Manage multiple API keys for different applications. Keys are masked in the UI after creation for enhanced security.
- **Rate Limiting**: Configurable SMS-per-hour limits per API Key to prevent carrier-level spam detection or abuse.
- **Global Webhook Callback**: Notify external systems about the delivery status (Sent, Failed, Delivered) of outbound messages.
- **WebSocket Tunneling**: Supports tunneling for remote access without complex port forwarding (in development).

### 🛠 System & Management
- **Full Data Backup & Restore**: Export all configurations, webhooks, rules, API keys, and logs into a single JSON file.
- **Battery Optimization Handling**: Built-in prompts to ensure the app runs reliably in the background by bypassing battery restrictions.
- **Gateway Identification**: Unique Gateway ID and Device Secret Token for identifying the source device in a multi-gateway setup.
- **Action Notifications**: Toast feedback for every critical system toggle (Enabling/Disabling services, rules, or settings).

---

## 🏗 Technical Architecture

The project follows **Clean Architecture** principles and is organized into a **Multi-Module** structure:

### Module Overview
- `:app`: The entry point, Hilt dependency injection setup, and Main Activity.
- `:domain`: Pure Kotlin module containing Business Models, Repository Interfaces, and UseCases.
- `:core`:
  - `database`: Room DB implementation, Entities, and DAOs.
  - `data`: Repository implementations and DataStore preferences.
  - `network`: HTTP Server (Ktor/OkHttp), Webhook delivery clients, and WebSocket tunneling.
  - `service`: Foreground services for Outbound SMS processing and Notifications.
  - `ui`: Shared UI components (e.g., `PowerSwitch`), Theme (`KizunaColors`), and reusable layouts.
  - `worker`: WorkManager tasks for background SMS processing and rule matching.
- `:feature`: Functional modules containing ViewModels and Compose Screens (e.g., `:feature:outbound`, `:feature:webhook`, `:feature:rules`).
  - `feature:sms`: Contains `InboundSmsReceiver` for intercepting system SMS broadcasts.

### Tech Stack
- **UI**: Jetpack Compose (Material 3)
- **DI**: Hilt (Dagger)
- **Persistence**: Room (SQL), DataStore (Preferences)
- **Networking**: Ktor (Server), OkHttp/Retrofit (Client)
- **Concurrency**: Kotlin Coroutines & Flow
- **Serialization**: Kotlinx Serialization (JSON)
- **Background**: WorkManager & Foreground Services

---

## 🎨 Design System

### Kizuna Colors
The app uses a specific color palette defined in `KizunaColors`:
- **Primary**: `#00A884` (WhatsApp-style Green)
- **Background**: `#0F172A` (Deep Navy/Dark Mode)
- **Surface**: `#1E293B` (Lighter Navy for Cards)
- **Error**: `#F87171` (Pastel Red)

### Reusable Components
- **PowerSwitch**: A large, animated, glowing circular toggle used for core service controls.
- **Action Cards**: Standardized card layout with a scale-down effect for interactive elements.

---

## 🔒 Security Specifications
1. **Masked API Keys**: API Key secrets are only visible during creation. In the list view, they are permanently masked (`••••••••`) to prevent side-shoulder leaks.
2. **Device Secret Token**: A unique UUID generated on first run, sent in headers as `X_HEADER_TOKEN` to verify the sender device at the webhook endpoint.
3. **Outbound Debouncing**: Webhook URL updates are debounced (500ms) to ensure smooth UI interaction while maintaining data persistence.

---

## 💾 Data Schema (Backup/Restore)
The backup JSON includes:
- `gatewayConfig`: Full system preferences.
- `webhooks`, `headers`, `templates`: Inbound configurations.
- `rules`: Routing logic.
- `variables`: Global custom variables.
- `apiKeys`: Outbound credentials.
- `smsMessages`, `deliveryLogs`, `outboundSms`: Complete historical logs.

---

## 👨‍💻 Development Guide
1. **Environment**: Android Studio Jellyfish+ (Java 17).
2. **Build**: Run `./gradlew assembleDebug` to build.
3. **Adding Features**: Create a new module in `:feature` and register it in `settings.gradle.kts` and `:app`.
4. **Dependencies**: Managed via `libs.versions.toml`.

## 📄 License & Dual Licensing

This project is dual-licensed under:
1. **GNU Affero General Public License v3.0 (AGPL-3.0)**: Free for individuals, personal projects, open-source projects, and educational/testing purposes. If you use, modify, or host this software as a network service, you must release your modified source code under the same license.
2. **Paid Commercial License**: If your company/organization wants to use this project for commercial purposes without the restrictions of the AGPL-3.0 (e.g., keeping your proprietary code private), you must purchase a **one-time payment commercial license**.

For commercial licensing inquiries, please contact the repository owner.

---
*Created with ❤️ by the Kizuna Team.*
