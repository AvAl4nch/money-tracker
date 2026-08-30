# Money-Tracker

Android app that tracks your spending automatically by reading incoming bank SMS messages and categorizing them with an LLM. Built for Jordanian banks (Arabic + English SMS), works with any bank whose messages contain amounts and balances.

## How it works

```
incoming SMS ──▶ regex heuristic (bank SMS? ≥2 of 5 patterns, AR+EN)
                      │ yes
                      ▼
                LLM categorization (OpenAI-compatible API, default OpenRouter)
                      │
            ┌─────────┴──────────┐
            ▼                    ▼
   confidence ≥ 0.7       low confidence / API down
   auto-categorized       saved + notification with
                          category quick-actions
```

Transactions are **never dropped** — if the AI is unreachable, the amount is extracted locally by regex and the transaction is stored uncategorized for you to fix with one tap.

## Features

- **Automatic SMS capture** — Arabic and English bank message formats, multipart SMS reassembly, duplicate detection
- **AI categorization** — 12 categories, any OpenAI-compatible endpoint (OpenRouter, OpenAI, Groq, Together, local Ollama), bring your own key
- **Notification quick-categorize** — uncertain transactions ask you via notification action buttons
- **Budgets** — per-category monthly limits with progress bars, over-limit warnings
- **Monthly summary** — donut chart, category breakdown, month navigation, 6-month trends comparison
- **Manual entry** — cash expenses via FAB
- **Edit any record** — correct the amount, type, date, merchant, description, category or note when the AI got something wrong; the original SMS is kept untouched as the source of truth
- **Ask your data** — a chat tab that answers questions like "what did I spend on health and education last month" by generating a read-only SQL query over your own records; every answer shows the query it ran
- **Search + filters** — text search, date-range presets and custom ranges, sort modes, category filter
- **Data import** — restore from a previous database file via the system file picker (atomic, dedup-safe)
- **Privacy-minded** — API key and financial DB excluded from cloud backups; response logging disabled in release builds. Two things leave the device: the single SMS being categorized, and — only when you use the **Ask** tab — your question, the database schema, and the rows your question matched (merchant names, amounts, dates)

## Setup

### 1. Install

Grab the APK from [Releases](https://github.com/AvAl4nch/money-tracker/releases) and install it (you may need to allow "install from unknown sources"), or build from source (below).

### 2. Grant permissions

On first launch the app asks for:

| Permission | Why |
|---|---|
| Receive/Read SMS | detect incoming bank messages — nothing else is read or sent |
| Notifications | the "Categorize Transaction" prompt when the AI is unsure |

### 3. Get an API key

The app needs an LLM for categorization. Easiest path — OpenRouter:

1. Create an account at [openrouter.ai](https://openrouter.ai) and add a few dollars of credit (classification costs fractions of a cent per SMS).
2. Create an API key at **openrouter.ai → Keys**.

Any OpenAI-compatible endpoint works too (OpenAI, Groq, Together, or a local [Ollama](https://ollama.com) server — preset URLs are built in).

### 4. Configure

Open **Settings** in the app:

| Field | Value |
|---|---|
| Base URL | `https://openrouter.ai/api` (preset, default) |
| API key | your key |
| Model name | e.g. `anthropic/claude-haiku-4.5` — fast, cheap, accurate for classification |
| Currency | your currency code, e.g. `JOD` |

Tap **Save**. Done — the next bank SMS shows up categorized in the Transactions tab.

### 5. (Optional) Import existing data

Settings → **Import old database** → pick a previously exported `money_tracker.db` file. Import is atomic and skips duplicates.

### 6. (Optional) Set budgets

Summary → **Edit budgets** → enter monthly limits per category. Progress bars turn red when you overshoot.

## Build from source

```bash
git clone git@github.com:AvAl4nch/money-tracker.git
cd money-tracker
./gradlew :app:assembleDebug      # or :app:installDebug with a device attached
./gradlew :app:testDebugUnitTest  # 30 unit tests
./gradlew :app:connectedDebugAndroidTest  # Room migration test (needs device/emulator)
```

Requires JDK 17+ and the Android SDK (compileSdk 36, minSdk 33).

## Tech stack

Kotlin · Jetpack Compose (Material 3) · Hilt · Room (versioned migrations + instrumented migration tests) · Preferences DataStore · OkHttp + Gson · clean architecture (`ai` / `data` / `domain` / `notification` / `sms` / `ui`)

## Contributing

Contributions are welcome — bug reports, feature ideas, and pull requests alike.

- **Found a bug / want a feature?** [Open an issue](https://github.com/AvAl4nch/money-tracker/issues).
- **Sending a PR?** Fork, branch, and make sure `./gradlew :app:testDebugUnitTest` passes (and `:app:connectedDebugAndroidTest` if you touch the database layer). Add tests for new logic where it makes sense.
- New bank SMS formats are especially appreciated — add a sample (with account numbers and balances anonymized) to `SmsParserTest` so detection keeps working for everyone.

By contributing you agree your work is licensed under the project's [MIT license](LICENSE).

## License

[MIT](LICENSE)
