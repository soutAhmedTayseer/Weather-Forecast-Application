<div align="center">

# 🕹️ 8-Bit Weather Forecast ☁️
**"Catching Clouds & Sunshine! Your Pocket Sky Buddy!"**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-1.5.0-4285F4.svg?style=for-the-badge&logo=android)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean-success.svg?style=for-the-badge)]()
[![MVVM](https://img.shields.io/badge/Pattern-MVVM-ff69b4.svg?style=for-the-badge)]()
[![Room](https://img.shields.io/badge/Database-Room-yellow.svg?style=for-the-badge)]()
[![Testing](https://img.shields.io/badge/Testing-MockK%20%7C%20JUnit4-red.svg?style=for-the-badge)]()

<br>

<img src="[https://via.placeholder.com/800x400.png?text=](https://via.placeholder.com/800x400.png?text=)[+INSERT+EPIC+APP+BANNER+OR+GIF+HERE+]" alt="App Banner">

<br>

> **A beautifully nostalgic, offline-first weather companion built entirely from scratch.** <br>
> Designed, engineered, tested, and shipped by **Ahmed Tayseer**.

</div>

---

## 👾 The Solo Studio (Developer Credentials)
This project is not just a codebase; it is a full-scale product built by a single developer acting as an entire studio. Every aspect of this repository was created from the ground up:
* **💻 Architecture & Engineering:** Implemented strict Clean Architecture, MVVM, and offline-first reactive data streams using Kotlin Coroutines and `StateFlow`.
* **🎨 UI/UX & Asset Design:** Built a completely bespoke Jetpack Compose "Retro" Design System. Hand-curated fonts, designed pixel-art layouts, and integrated seamless transparent GIFs and Lottie animations.
* **🎬 Media & Production:** Handled all video editing, GIF processing, and asset generation for the application's unique splash screens and loading states.
* **🧪 QA & Documentation:** Wrote comprehensive unit tests across all layers (DAO, Repositories, ViewModels) using JUnit4 and MockK, and authored this documentation.

---

## 📸 Gameplay Screenshots

| Home Base (Live Weather) | The Map (Explore) | Alerts (The Final Boss) |
| :---: | :---: | :---: |
| <br> <img src="[https://via.placeholder.com/250x500.png?text=Home+Screen](https://via.placeholder.com/250x500.png?text=Home+Screen)" width="250"> | <br> <img src="[https://via.placeholder.com/250x500.png?text=Map+Screen](https://via.placeholder.com/250x500.png?text=Map+Screen)" width="250"> | <br> <img src="[https://via.placeholder.com/250x500.png?text=Alerts+Screen](https://via.placeholder.com/250x500.png?text=Alerts+Screen)" width="250"> |

| Settings (Options) | Favorites (Save Points) | Loading (Easter Eggs) |
| :---: | :---: | :---: |
| <br> <img src="[https://via.placeholder.com/250x500.png?text=Settings](https://via.placeholder.com/250x500.png?text=Settings)" width="250"> | <br> <img src="[https://via.placeholder.com/250x500.png?text=Favorites](https://via.placeholder.com/250x500.png?text=Favorites)" width="250"> | <br> <img src="[https://via.placeholder.com/250x500.png?text=Finn+Jake+Loading](https://via.placeholder.com/250x500.png?text=Finn+Jake+Loading)" width="250"> |

---

## 📜 The Quest Log (Core Features)

* **🌍 Hyper-Accurate Weather:** Real-time atmospheric data fetched via OpenWeatherMap API, displaying temperature, humidity, wind, pressure, and dynamic day/night cycles.
* **📡 Map Exploration:** Integrated `OSMdroid` with Reverse Geocoding. Tap anywhere on the map to pinpoint a location, translate its coordinates into a real city name, and save it to your Favorites.
* **💾 Offline-First (No Internet? No Problem):** The app never leaves the user hanging. It utilizes a Single Source of Truth architecture—instantly loading cached data from a local `Room` database while silently syncing with the network in the background.
* **🚨 Lock-Screen Breaking Alarms:** Utilizing `AlarmManager` and `BroadcastReceiver` (`goAsync`), the app wakes up, fetches live data, and triggers a `FullScreenIntent` that breaks through the Android lock screen to warn you of severe weather—complete with custom user-selected ringtones.
* **⚙️ Dynamic Localization & Settings:** Switch between English/Arabic, Metric/Imperial/Standard units, and GPS/Map locations instantly. UI reacts to `DataStore` changes in real-time via `StateFlow`.
* **✨ The 8-Bit Engine:** A totally custom Jetpack Compose UI library (`RetroComponents`), featuring auto-scrolling carousels, smooth `animateContentSize` morphing, and zero native Android loading spinners.

---

## ⚙️ The Game Engine (Architecture & Tech Stack)

The codebase strictly adheres to **Clean Architecture** and **Package-by-Feature** to ensure absolute scalability and separation of concerns.

### 🧰 Tech Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel), Clean Architecture
* **Concurrency:** Kotlin Coroutines & Flow (`StateFlow`, `SharedFlow`, `combine`)
* **Dependency Injection:** Manual Constructor Injection via `ViewModelProvider.Factory`
* **Network:** Retrofit 2, OkHttp, Gson Converter
* **Local Persistence:** Room Database (SQLite), Preferences DataStore
* **Background Tasks:** `AlarmManager`, `BroadcastReceiver`
* **Location:** Google Play Services FusedLocationProvider, OSMdroid
* **Media & Animations:** Coil (GIF decoding), Lottie Compose

### 🏗️ Package Structure
```text
com.example.weatherforecastapplication
│
├── core                        // App-wide utilities, theme, and background workers
│   ├── navigation              // Sealed classes for routing & BottomNavBar
│   ├── theme                   // The Custom 8-Bit Design System (RetroComponents)
    ├── utils
│   └── worker                  // AlarmScheduler, WeatherAlarmReceiver, AlarmActivity
│
├── data                        // The Single Source of Truth
│   ├── local                   // Room DB, DAOs, Custom TypeConverters
│   ├── remote                  // Retrofit API Service
    ├── model
│   └── repository              // Data merging & offline-first logic
│
├── domain                      // Business Logic Contracts
│   └── repository              // WeatherRepository Interfaces
├── di                          //dependency injection
│
└── presentation                // Package-by-Feature (High Cohesion)
    ├── alerts                  // AlertsScreen, AlertsViewModel
    ├── favorites               // FavoritesScreen, Details, FavoritesViewModel
    ├── home                    // HomeScreen, HomeViewModel
    ├── map                     // MapSelectionScreen, MapViewModel
    ├── settings                // SettingsScreen, SettingsViewModel
    └── splash                  // Custom Lottie Splash Screen
└── Main Activity                      // Entry Point
```

---

## 🎨 The Pixels (Bespoke UI Design System)
Instead of relying on generic Material components, I engineered a highly reusable, completely custom 8-bit UI library built on the **DRY (Don't Repeat Yourself)** principle.

* **`RetroComponents.kt`:** Contains wrapper components like `RetroCard`, `RetroTopAppBar`, and `RetroSwipeToDeleteContainer`. If the app ever needs a theme overhaul, changing this single file updates the entire application instantly.
* **Typography:** Integrated the `Handjet` custom font with extensive typographic scaling (`displayLarge` down to `labelSmall`).
* **Animations:** Stripped out generic Android progress bars entirely. Replaced them with a custom `SolidSwipeRefreshLayout` wrapper that triggers Coil-decoded transparent GIFs (featuring Finn & Jake!) and seamless Lottie transitions.

---

## 🧪 The Debugger (Testing Strategy)
To ensure maximum stability, quality, and accuracy, this project features a robust Unit Testing suite that verifies the behavior of the MVVM / Clean Architecture layers using the **Arrange, Act, Assert (AAA)** pattern.

* **Frameworks Used:** `JUnit4`, `MockK`, `kotlinx-coroutines-test`, `room-testing`.
* **Presentation Layer:** Verified `HomeViewModel`'s reactive `StateFlow` transitions (Loading -> Success) and dependency interactions using a `StandardTestDispatcher` to predictably control virtual coroutine time.
* **Domain/Data Source Layer:** Utilized **MockK Test Doubles** to isolate network and repository logic. Verified `WeatherRemoteDataSourceImpl` handles Retrofit Success and 404 Error responses gracefully without hitting actual APIs.
* **Local DAO Layer:** Wrote Android Instrumented Tests utilizing Room's `inMemoryDatabaseBuilder`. This allowed real SQLite queries (Insert, Delete, Read) to be tested at lightning speed purely in the device's RAM, completely protecting actual user data from corruption.

---

## 🚀 Installation & Setup

1. **Clone the repository:**
```bash
git clone https://github.com/your-username/WeatherForecastApplication.git
```
2. **Open the project** in Android Studio (Iguana or newer).
3. **Open** `WeatherRepositoryImpl.kt` and insert your OpenWeatherMap API Key:
```kotlin
private val API_KEY = "YOUR_API_KEY_HERE"
```
4. **Sync Gradle** and hit Run on an emulator or physical device (API 24+).

---

<br>

<div align="center">
<p><i>"Developed with ❤️, ☕, and a whole lot of Coroutines by Ahmed Tayseer."</i></p>
    
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/ahmed-tayseer-b734a7241/)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=for-the-badge&logo=github)](https://github.com/soutAhmedTayseer)
</div>
