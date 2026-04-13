# VoltWatch Pro - Premium Battery Monitoring & Alert System

VoltWatch is a high-fidelity Android application designed to provide users with real-time battery insights, persistent logging, and customizable alerts. Built with modern Android technologies, it offers a sleek, glassmorphic UI and robust background services.

## ✨ Features

- **Real-Time Monitoring**: Track battery level, temperature, voltage, and health with instant updates.
- **Custom Alert System**: 
    - Set target battery percentages for notifications.
    - Robust detection logic handles exact hits and level crossings.
    - Works in both charging and discharging modes.
- **Battery Analytics**:
    - Automatic history logging every 15 minutes.
    - Detailed charging speed estimation.
    - Estimates for time-to-full charge.
- **Premium UI/UX**:
    - Modern Dark Mode design.
    - Glassmorphic UI components.
    - Interactive 4-color gradient circular progress meter.
- **Background Redundancy**: Uses WorkManager to ensure battery data is logged and alerts are checked even when the app is closed.

## 🛠️ Technology Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) for battery history storage.
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable background execution.
- **State Management**: Kotlin Coroutines & Flow with ViewModel (MVVM Architecture).
- **Styling**: Material 3 with custom glassmorphism and gradient effects.

## 📱 Permissions

- `POST_NOTIFICATIONS`: For battery level alerts (Android 13+).
- `VIBRATE`: For haptic feedback on plug-in events.
- `RECEIVE_BOOT_COMPLETED`:  To resume monitoring after device restart.

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/JauharAnsari/VoltWatch.git
   ```
2. **Open in Android Studio**:
   Ensure you have the latest version of Android Studio (Koala or newer).
3. **Build & Run**:
   Select your device and click **Run**.
4. **Enable Alerts**:
   - Go to the Alert Card.
   - Set your target percentage.
   - Toggle "Alert System" ON and grant notification permissions.

## 📸 Screenshots

| Monitor Screen | History Screen | Alert System |
| :---: | :---: | :---: |
| ![Monitor](https://via.placeholder.com/300x600?text=Battery+Monitor) | ![History](https://via.placeholder.com/300x600?text=Battery+History) | ![Alerts](https://via.placeholder.com/300x600?text=Custom+Alerts) |

## 🛡️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Developed with ❤️ by Jauhar Ansari*
