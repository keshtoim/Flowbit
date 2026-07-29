<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="120" alt="Flowbit logo"/>

# Flowbit

### A habit tracker that stays out of your way

[![Android](https://img.shields.io/badge/Android-5.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![CI](https://img.shields.io/github/actions/workflow/status/keshtoim/Flowbit/build.yml?style=for-the-badge&label=CI)](https://github.com/keshtoim/Flowbit/actions)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**[🇷🇺 Русский](README.md) · [🇬🇧 English](README.en.md)**

</div>

---

## Why another habit tracker?

Most habit apps are either too basic or bloated with paid features. Flowbit hits the sweet spot: **everything you need for serious habit tracking** — no subscriptions, no ads, no cloud required.

- 📴 **100% offline** — your data lives only on your device
- 🎨 **Material You** — widgets and UI adapt to your wallpaper colors
- ⚡ **Fast** — one tap to check off, widget right on your home screen
- 🔓 **Open source** — no hidden trackers or analytics

---

## Features

<table>
<tr>
<td width="50%">

### ✅ Flexible habits
- Repetition counter (+ / −) with daily goal
- Frequency: every day or specific weekdays
- **Numeric goals** — "drink 2 L of water", "run 5 km"
- **Built-in timer** — counts down, auto-completes when done
- Period goals — "4 times a week"
- Color-coded tags and grouping

</td>
<td width="50%">

### 📊 Analytics
- 🔥 Current and best streak
- 📈 30-day activity chart
- 🗓️ Year heatmap
- ✅ Completion rate by period

</td>
</tr>
<tr>
<td>

### 🔔 Smart reminders
- Precise scheduled notifications
- **"Done ✓"** action button in the notification shade — no need to open the app
- Survives device reboots

</td>
<td>

### 🪟 Home screen widgets
| | |
|---|---|
| Weekly | all habits for 7 days |
| Day summary | circular progress |
| Single habit | counter + **"+"** button |

All widgets adapt to Material You dynamic color.

</td>
</tr>
<tr>
<td>

### 💡 Details that matter
- ⏭ Intentional day skip — with confirmation to undo
- 📝 Per-day notes
- 🖼️ Photo banner (with crop)
- 🎵 Audio motivation
- ↕️ Drag-and-drop card reordering
- 🗑️ Delete with confirmation

</td>
<td>

### 🌗 Personalization
- Dark / light theme
- 12 accent colors per habit
- Emoji avatar
- Group by tag / frequency / status
- 💾 JSON export & import (including reminders)

</td>
</tr>
</table>

---

## Tech stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material3](https://img.shields.io/badge/Material_3-757575?logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-v9-3DDC84?logo=android&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt_DI-F6891F)
![Glance](https://img.shields.io/badge/Glance_Widgets-4285F4?logo=android&logoColor=white)

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Widgets | Jetpack Glance 1.1 · `PreferencesGlanceStateDefinition` |
| DI | Hilt |
| Database | Room 2.6 · SQLite · migrations v1→v9 |
| Images | Coil 2.6 + android-image-cropper |
| Background | AlarmManager · WorkManager |
| Preferences | DataStore |
| Navigation | Navigation Compose 2.7 |
| Drag-and-drop | sh.calvin.reorderable 2.4 |
| CI/CD | GitHub Actions · auto versionName from commit count |

---

## Quick start

```bash
git clone https://github.com/keshtoim/Flowbit.git
cd Flowbit
./gradlew assembleDebug
```

> Requirements: Android Studio Ladybug+, JDK 17, Android 5.0+ (API 21)

APK → `app/build/outputs/apk/debug/app-debug.apk`

---

## Architecture

```
app/
├── data/
│   ├── database/       # Room: entities, DAOs, migrations (v1→v9)
│   ├── receiver/       # AlarmManager BroadcastReceivers
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Habit, HabitEntry, HabitTag…
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic
├── presentation/
│   ├── habits/         # List · editor · detail
│   ├── statistics/     # Charts and heatmap
│   ├── settings/       # Settings · backup · language
│   └── theme/          # Material You + dark theme
└── widget/             # 3 Glance widgets + IncrementHabitAction
```

Clean Architecture: `presentation → domain ← data`. ViewModels don't know about the DB; UseCases don't know about Compose.

---

## Permissions

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Reminder notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Precise reminder timing |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after reboot |
| `READ_MEDIA_IMAGES` | Pick photo from gallery |
| `READ_MEDIA_AUDIO` | Pick audio file |

---

<div align="center">

MIT License · made with ❤️ by [Keshtoim](https://github.com/keshtoim)

</div>
