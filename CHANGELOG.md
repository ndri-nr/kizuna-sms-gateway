# Changelog

All notable changes to the **Kizuna: SMS Gateway** project will be documented in this file.

---

## [1.0.0] - 2026-06-27

This is the initial release of **Kizuna: SMS Gateway**, a robust, Clean-Architecture-based Android application that bridges mobile SMS services with external web applications. It serves as both an inbound gateway (relaying received SMS to webhooks) and an outbound gateway (providing a local REST API to send SMS).

### 🚀 Key Features

#### 📥 Inbound Gateway (SMS to Webhook)
- **Rule-Based Routing**: Match incoming SMS messages using Sender Regex patterns or keyword "Contains" queries. Supports rule priorities to determine execution order.
- **Reliable Delivery WorkManager**: Processes SMS in the background using Android's `WorkManager` to guarantee webhook execution even when the device or app is idle.
- **Customizable Webhooks**:
  - Supports `POST`, `PUT`, and `PATCH` HTTP methods.
  - Template interpolation engine allows dynamic JSON bodies using placeholders like `{{message}}`, `{{sender}}`, and `{{receivedAt}}`.
  - Supports custom and global HTTP headers for authentication and metadata tracking.
- **Auto-Reply**: Configure rules to automatically send a pre-defined SMS reply to the sender upon successful webhook delivery.
- **Catch-All Logic**: Support for wildcard/catch-all fallback rules when no specific pattern matches an incoming SMS.

#### 📤 Outbound Gateway (API to SMS)
- **Local Ktor REST Server**: An embedded HTTP server running as an Android Foreground Service to accept outbound SMS requests locally.
- **API Key Management**: Supports creating multiple API keys with configurable rate limits (SMS-per-hour limit) to prevent carrier spam detection.
- **Global Webhook Callback**: Configurable URL callback to notify external systems of delivery outcomes (`Sent`, `Failed`, or `Delivered`).
- **WebSocket Tunneling**: Initial framework integration supporting remote access without requiring complex port forwarding (in development).

#### 🛠 System & Configuration
- **Full Data Backup & Restore**: Standardized backup utility that exports/imports all rules, webhooks, API keys, configs, and message/delivery logs as a single JSON payload.
- **Device Identification**: Generation of unique Device IDs and Device Secret Tokens, automatically attached to webhook headers (as `X_HEADER_TOKEN`) to authenticate requests.
- **Battery Optimization Handling**: Prompt systems guiding users to disable Android battery optimizations, ensuring background services run uninterrupted.
- **Debounced Settings Sync**: UI inputs like webhook URL updates are debounced (500ms) before committing to persistent storage, ensuring smooth typing and stable persistence.

#### 🎨 Design & UI/UX
- **Modern Jetpack Compose & Material 3**: Beautifully designed dark-mode UI customized with WhatsApp-style branding.
  - **Primary Color**: `#00A884` (WhatsApp Green)
  - **Background**: `#0F172A` (Deep Navy)
  - **Surface**: `#1E293B` (Slate Card backgrounds)
- **PowerSwitch Component**: Custom animated, glowing circular toggle component built for major service activations.
- **API Key Masking**: Sensitives keys are permanently masked in the UI using `••••••••` to prevent shoulder-surfing leaks, displaying keys only upon initial creation.
- **Tactile Cards**: Scale-down press feedback on action cards for premium tactile interactivity.

---

### 🏗 Technical Stack & Architecture

- **Architecture**: Multi-module Clean Architecture (`:domain`, `:core`, `:feature`, `:app`).
- **Dependency Injection**: Hilt (Dagger).
- **Persistence**: Room Database (for SMS logs and rules) & DataStore (for configuration preferences).
- **Networking**: Ktor Server (for local REST API) & OkHttp/Retrofit (for webhook HTTP clients).
- **Asynchrony**: Kotlin Coroutines & StateFlow.
