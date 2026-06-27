# AI Context: Kizuna: SMS Gateway

This document provides a token-efficient summary of the project structure and logic for AI models to understand the codebase context quickly.

## 🧱 Module Structure & Responsibilities

- **`:domain`**: Truth source.
  - `model/`: `Rule`, `Webhook`, `OutboundSms`, `ApiKey`, `GatewayConfig`.
  - `repository/`: Interfaces for all data operations.
  - `usecase/`: Logic for `ExportBackup`, `ImportBackup`, `DeliverWebhook`, `MatchRule`.
- **`:core:database`**: Persistence.
  - Entities (Room) mapping 1:1 with Domain models where possible.
  - `KizunaDatabase`: Root Room database.
- **`:core:data`**: Implementation.
  - `GatewayConfigRepositoryImpl`: Uses `DataStore` for preferences.
  - `OutboundRepositoryImpl`, `WebhookRepositoryImpl`: Room-backed implementations.
- **`:core:service`**: Active Processes.
  - `OutboundSmsService`: Foreground service. Monitors `OutboundSms` table for `PENDING` items.
  - `NotificationServiceImpl`: Centralized `Toast` and Status Bar notification handler.
- **`:core:ui`**: Visuals.
  - `KizunaColors`: Primary `#00A884`, Background `#0F172A`.
  - `PowerSwitch`: Custom `@Composable` for major service toggles.
- **`:feature:*`**: MVVM UI modules.
  - Pattern: `Screen.kt` (UI) + `ViewModel.kt` (State/Logic).

## 🔄 Core Logic Flows

### Inbound Flow (Receive SMS)
1. `InboundSmsReceiver` (System Broadcast) intercepts incoming SMS.
2. Triggers `ProcessSmsWorker` (via `WorkManager`) for background execution.
3. `MatchRuleUseCase` finds enabled `Rule` where `senderRegex` or `containsText` matches.
4. `DeliverWebhookUseCase` renders `WebhookTemplate` with SMS variables.
5. `WebhookHttpClient` sends `POST/PUT/PATCH`.
6. If `autoReplyMessage` exists, triggers `SendAutoReplyUseCase`.

### Outbound Flow (Send SMS)
1. `HttpGatewayServer` (Ktor) receives request with `ApiKey`.
2. `OutboundRepository` saves `OutboundSms` as `PENDING`.
3. `OutboundSmsService` detects new entry -> calls `SmsSender`.
4. Result (Sent/Failed) triggers global status webhook callback.

## 💾 Data Models Summary

- **`BackupPayload`**: Envelopes all tables + `GatewayConfig` for full state migration.
- **`ApiKey`**: Includes `key` (UUID), `smsPerHour` (limit), and `isActive`.
- **`Rule`**: Links patterns to `webhookId`. `priority` determines execution order.
- **`GatewayConfig`**: Stores `deviceSecret`, `gatewayName`, and `deleteUntrackedSms` flag.

## 🛠 Coding Standards

- **State Management**: `StateFlow` in ViewModels, `collectAsState()` in Compose.
- **Dependency Injection**: Constructor injection via Hilt.
- **Async**: Coroutines for all IO/Database work.
- **UI Architecture**: Single Activity, multi-feature navigation.
- **Masking**: Sensitive data (API Keys) are permanently masked in UI: `"•".repeat(length)`.

## 📌 Keywords for Search
`InboundRulesViewModel`, `OutboundViewModel`, `DeliverWebhookUseCase`, `ExportBackupUseCase`, `PowerSwitch`, `KizunaColors`, `GatewayConfigRepository`.
