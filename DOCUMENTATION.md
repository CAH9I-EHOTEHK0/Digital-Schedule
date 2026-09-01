# 🔧 Технічна документація — Digital Schedule

> Детальний технічний опис архітектури, бази даних та структури проєкту для розробників.

---

## 📋 Зміст

1. [Загальна архітектура](#1-загальна-архітектура)
2. [Структура директорій](#2-структура-директорій)
3. [Шар даних (Data Layer)](#3-шар-даних-data-layer)
4. [Шар моделей (Model Layer)](#4-шар-моделей-model-layer)
5. [Шар UI (Presentation Layer)](#5-шар-ui-presentation-layer)
6. [Мережевий шар та парсери](#6-мережевий-шар-та-парсери)
7. [Конфігурація та SharedPreferences](#7-конфігурація-та-sharedpreferences)
8. [Зовнішні залежності](#8-зовнішні-залежності)

---

## 1. Загальна архітектура

Додаток побудований за принципом **Single Activity Architecture** з використанням **Jetpack Compose**:

```
                  ┌────────────────────────┐
                  │      MainActivity      │
                  └───────────┬────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
    [GradesScreen]      [HomeScreen]      [SettingsScreen]
                              │ (onNavigateToEdit)
                              ▼
                        [EditScreen]
```

- **Стан та сховище:** `LessonStore` взаємодіє з SQLite через **Room**, `GradeStore` та налаштування зберігаються в `SharedPreferences` (JSON через Gson).
- **Парсинг:** Запити до кабінету НАУ реалізовані через `OkHttp` + `Jsoup` у фоновому пулі корутин (`Dispatchers.IO`).

---

## 2. Структура директорій

```
app/src/main/java/ua/zxcode/digitalschedule/
├── DigitalScheduleApp.kt       # Application-клас (ініціалізація ThreeTenABP)
├── MainActivity.kt             # Головна точка входу, стан вкладок та теми
│
├── data/                       # Робота з персистентними даними
│   ├── AppDatabase.kt          # Room Database (Singleton)
│   ├── GradeStore.kt           # Кешування сесійних оцінок (SharedPreferences)
│   ├── LessonDao.kt            # Room DAO (CRUD операції з парами)
│   ├── LessonEntity.kt         # Таблиця 'lessons' та мапери
│   ├── LessonRepository.kt     # Репозиторій пар
│   ├── LessonStore.kt          # Управління списком пар у Room
│   └── NoteStore.kt            # Збереження нотаток до конкретних дат і пар (SharedPreferences)
│
├── manager/                    # Бізнес-логіка
│   └── LessonTimeManager.kt    # Управління часовими інтервалами пар
│
├── model/                      # Доменні моделі даних
│   ├── Grade.kt                # Оцінка за предмет (бали, викладач, тип контролю)
│   ├── LessonTypeExt.kt        # Extension-властивості для типів пар (іконки, назви)
│   ├── ScheduleModels.kt       # Lesson, DaySchedule, WeekSchedule, LessonType, ScheduleType
│   └── ScheduleSettings.kt     # Модель налаштувань, колір акценту, LessonTime
│
├── parser/                     # Мережеві клієнти та парсери кабінету НАУ
│   ├── NauCabinetClient.kt     # Авторизація та отримання оцінок сесії
│   └── ScheduleParser.kt       # Парсинг розкладу занять студента
│
└── ui/                         # Користувацький інтерфейс
    ├── BottomNavigationBar.kt  # Нижня плаваюча панель (3 вкладки)
    ├── components/
    │   ├── AddLessonDialog.kt      # Діалог створення нової пари
    │   ├── GradeCard.kt            # Картка відображення оцінки
    │   ├── HomeLessonCard.kt       # Картка пари на головному екрані з прев'ю нотатки та крапочкою
    │   ├── LessonNoteDialog.kt     # Діалог створення/редагування нотатки до пари
    │   ├── RatingScoreDialog.kt    # Діалог розрахунку рейтингового балу
    │   └── ScheduleGridOverlay.kt  # Інтерактивна таблиця-сітка розкладу з жестами
    ├── screens/
    │   ├── HomeScreen.kt       # Головний екран розкладу (свайпи, автооновлення)
    │   ├── GradesScreen.kt     # Екран оцінок сесії (семестри, оновлення)
    │   ├── SettingsScreen.kt   # Екран налаштувань акаунта, теми, розкладу, очищення
    │   └── EditScreen.kt       # Повноекранний режим редагування пар
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 3. Шар даних (Data Layer)

### База даних Room (`digital_schedule.db`)

Таблиця `lessons`:
```kotlin
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,          // 1..7 (ISO DayOfWeek)
    val weekType: String?,       // "1", "2" або null (для обох тижнів)
    val startTime: String,       // наприклад "08:00"
    val endTime: String,         // наприклад "09:35"
    val subject: String,         // Назва дисципліни
    val teacher: String,         // ПІБ викладача
    val lessonType: String,      // "LECTURE", "LAB", "PRACTICE"
    val room: String,            // Номер аудиторії
    val group: String?           // Підгрупа (опціонально)
)
```

`LessonDao` підтримує реактивні потоки через Kotlin `Flow<List<LessonEntity>>` та повні CRUD-операції (`insert`, `insertAll`, `update`, `delete`, `deleteAll`).

---

## 4. Шар моделей (Model Layer)

### `ScheduleSettings`
Містить усі користувацькі налаштування:
- `Username`, `Password` — облікові дані кабінету НАУ.
- `scheduleType` — `ONE_WEEK` або `TWO_WEEK`.
- `saturdayEnabled`, `saturdayType` — параметри суботніх пар.
- `accentColorHex` — колір теми у форматі HEX (`#RRGGBB`).
- `isDarkTheme` — прапорець темної теми.
- `lessonTimes` — список часових слотів `LessonTime` (1..7 пари).

---

## 5. Шар UI (Presentation Layer)

### Навігація в додатку
- **Нижня панель (`BottomNavigationBar`)** має 3 вкладки:
  1. `Grades` (Оцінки) — індекс 1 у `MainActivity`.
  2. `Home` (Головна) — індекс 0 у `MainActivity`.
  3. `Settings` (Налаштування) — індекс 2 у `MainActivity`.
- **Режим редагування розкладу (`EditScreen`)** відкривається з `HomeScreen` через кнопку ✏️ у верхній панелі, тимчасово приховуючи нижню навігаційну панель для повноекранного комфортного редагування.

### Дизайн та кастомізація
- **Dynamic Accent:** колір акценту динамічно підставляється в Material3 палітру `ColorScheme`.
- **Blur & Glassmorphism:** верхній та нижній бари використовують прозорі фони з розмиттям та обводкою в тон акцентного кольору.

---

## 6. Мережевий шар та парсери

### Авторизація та синхронізація
- `NauCabinetClient`: працює з `https://cabinet.nau.edu.ua`, зберігає Cookie сесії (`CookieManager`), виконує автентифікацію студента та парсить HTML/JSON структури семестрових відомостей.
- `ScheduleParser`: завантажує персональний розклад занять студента та конвертує його в список об'єктів `Lesson`.

---

## 7. Конфігурація та SharedPreferences

- Назва преференсів: `digital_schedule_prefs`, `digital_schedule_grade_prefs`, `digital_schedule_notes_prefs`
- Ключі:
  - `schedule_settings` — серіалізований JSON конфігурації `ScheduleSettings`.
  - `grades_json` — кеш оцінок `Map<String, List<Grade>>`.
  - `notes_json` — мапа нотаток `Map<String, String>` з ключами формату `YYYY-MM-DD#subject#startTime#dayOfWeek#weekType`.

---

## 8. Зовнішні залежності

| Бібліотека | Версія | Призначення |
|---|---|---|
| `androidx.compose.material3` | BOM | Інтерфейс Material 3 |
| `androidx.room:*` | 2.6.1 | Локальна SQLite база даних |
| `com.squareup.okhttp3:okhttp` | 4.12.0 | HTTP клієнт для зв'язку з кабінетом НАУ |
| `org.jsoup:jsoup` | 1.17.2 | Парсинг HTML сторінок кабінету |
| `com.google.code.gson:gson` | 2.10.1 | Серіалізація/десеріалізація моделей |
| `com.jakewharton.threetenabp:threetenabp` | 1.4.5 | Робота з датами на старіших версіях Android |

---

*Версія документації: **3.0.69**.*
