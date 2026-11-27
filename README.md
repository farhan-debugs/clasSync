# ClassSync 📚

> Modern Android class management app — Attendance · Timetable · Assignments — synced with Google Sheets.

## Overview

ClassSync is a native Android application built for Class Representatives (CRs) and students to streamline classroom management. Its core feature is **real-time, two-way synchronization** between the app's offline-capable attendance tracker and a linked Google Sheet.

---

## Screenshots

| Attendance | Analytics | Timetable | Tasks |
|:---:|:---:|:---:|:---:|
| Student carousel, P/A/L quick-mark, sync badge | 60-tick radial ring, subject breakdown | Day-strip, live-class indicator | Assignment checklist, study materials |

---

## Features

- **Google Sign-In** via Credential Manager (OAuth 2.0)
- **Student Roster Import** from Google Sheets "Roster" tab
- **Quick Attendance Marking** (Present / Absent / Late) with bulk "Mark All Present"
- **Google Sheets Sync** via WorkManager — works offline, syncs automatically
- **60-tick Radial Analytics** — attendance by subject with real Room DB data
- **Dynamic Timetable** — full CRUD (add/edit/delete), live-class highlight
- **Assignments To-Do** — priority, deadline badges, 3h/24h push notifications
- **Study Materials** — subject-wise file cards (PDF, Slide, Link)
- **Daily Digest** — morning notification with today's classes + pending tasks
- **Settings** — class name, sheet link, notification preferences, sign-out

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Dagger Hilt |
| Local DB | Room (SQLite) |
| Preferences | Jetpack DataStore |
| Background | WorkManager |
| Auth | Credential Manager + Google Identity |
| API | Google Sheets API v4, Drive API v3 |
| Async | Kotlin Coroutines + StateFlow |

---

## Project Structure

```
app/src/main/java/com/classsync/app/
├── data/
│   ├── auth/           GoogleAuthManager (Credential Manager sign-in)
│   ├── local/          Room DB, DAOs, Entities, DataStore
│   ├── remote/         SheetsApiService (Google Sheets API v4)
│   └── repository/     Single source of truth repositories
├── di/                 Hilt modules (DB, Auth, Network, WorkManager)
├── ui/
│   ├── navigation/     NavGraph + Screen routes
│   ├── screens/        6 screens, each with ViewModel
│   └── theme/          OLED dark colour palette + typography
├── util/               NotificationUtils (channels, permission)
└── worker/             SheetsSyncWorker, AssignmentReminderWorker, DailyDigestWorker
```

---

## Setup

### Prerequisites

- Android Studio Ladybug (2024.2+)
- JDK 17
- Google Cloud Console project

### Steps

1. **Google Cloud Console**
   - Create a project
   - Enable **Google Sheets API v4** and **Google Drive API v3**
   - Create OAuth 2.0 credentials:
     - Web client → copy the Client ID
     - Android client → package `com.classsync.app`, add your SHA-1

2. **Firebase**
   - Create a Firebase project, add your Android app (`com.classsync.app`)
   - Download `google-services.json` → place in `app/`

3. **strings.xml**
   ```xml
   <string name="default_web_client_id">YOUR_OAUTH_WEB_CLIENT_ID</string>
   ```

4. **Google Sheet format**
   - Create a "Roster" tab with columns: `A=Name`, `B=Roll Number`, `C=Email`

5. **Build**
   ```bash
   ./gradlew assembleDebug
   ```

---

## Colour Palette

| Token | Hex | Role |
|-------|-----|------|
| `PitchBlack` | `#0A0A0A` | Canvas |
| `ObsidianGray` | `#161618` | Cards |
| `Graphite` | `#222225` | Chips, icons |
| `SageGreen` | `#529471` | Present, Synced |
| `WarmTaupe` | `#9E826C` | Late, Pending |
| `CoralRose` | `#D96B6B` | Absent, Overdue |
| `CreamWhite` | `#F5F5F7` | Active chart ticks |

---

## Development Milestones

- [x] Phase 1 — Foundation, Auth & Google Sign-In
- [x] Phase 2 — Google Sheets Import & Attendance Sync
- [x] Phase 3 — Timetable CRUD & Study Materials
- [x] Phase 4 — Assignments, Notifications & Daily Digest
- [x] Phase 5 — Settings, Offline Queueing & Polish
- [ ] Phase 6 — Firebase Firestore (multi-user shared timetable)
- [ ] Phase 7 — Crashlytics, Analytics, Play Store release

---

## License

MIT © ClassSync Contributors
