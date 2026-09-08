# Технічна документація — Digital Schedule

Архітектурний та технічний опис мобільного застосунку Digital Schedule для інженерів і розробників.

---

## 1. Загальна архітектура

Застосунок реалізовано за архітектурним підходом **Single Activity Architecture** з декларативним інтерфейсом користувача на основі **Jetpack Compose**:

```
                  ┌────────────────────────┐
                  │      MainActivity      │
                  └───────────┬────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
    [HomeScreen]        [GradesScreen]      [SettingsScreen]
          │ (onNavigateToEdit)
          ▼
    [EditScreen]
```

- **Керування станом:** `LessonStore` реалізує доступ до локальної бази даних SQLite через **Room**. `GradeStore`, `NoteStore` та користувацькі конфігурації збережено в `SharedPreferences` із серіалізацією JSON за допомогою бібліотеки `Gson`.
- **Фонова обробка:** Мережеві запити та HTML-парсинг даних виконуються асинхронно в корутинах на пулі потоків `Dispatchers.IO`.

---

## 2. Структура директорій та компонентів

```
app/src/main/java/ua/zxcode/digitalschedule/
├── DigitalScheduleApp.kt       # Application-клас (ініціалізація ThreeTenABP)
├── MainActivity.kt             # Головна точка входу, стан вкладок та теми
│
├── data/                       # Рівень персистентності даних
│   ├── AppDatabase.kt          # Синглтон бази даних Room
│   ├── GradeStore.kt           # Сховище оцінок сесії (SharedPreferences)
│   ├── LessonDao.kt            # Інтерфейс доступу до даних Room (CRUD операції)
│   ├── LessonEntity.kt         # Сутність таблиці 'lessons' та функції-мапери
│   ├── LessonRepository.kt     # Репозиторій для роботи з розкладом
│   ├── LessonStore.kt          # Управління списком занять у Room
│   └── NoteStore.kt            # Сховище нотаток до пар за датами (SharedPreferences)
│
├── manager/                    # Бізнес-логіка
│   └── LessonTimeManager.kt    # Управління часовими інтервалами занять
│
├── model/                      # Доменні моделі даних
│   ├── Grade.kt                # Модель оцінки дисципліни (бали, викладач, контроль)
│   ├── LessonTypeExt.kt        # Розширення для локалізації типів занять
│   ├── ScheduleModels.kt       # Моделі Lesson, DaySchedule, WeekSchedule, LessonType, ScheduleType
│   └── ScheduleSettings.kt     # Модель налаштувань, параметри акцентів, LessonTime
│
├── parser/                     # Мережеві клієнти та парсери
│   ├── NauCabinetClient.kt     # Авторизація та завантаження результатів сесії
│   └── ScheduleParser.kt       # Парсинг персонального розкладу занять
│
└── ui/                         # Рівень користувацького інтерфейсу
    ├── BottomNavigationBar.kt  # Нижня панель навігації
    ├── components/
    │   ├── AddLessonDialog.kt      # Діалогове вікно створення нового заняття
    │   ├── GradeCard.kt            # Компонент відображення оцінки за предмет
    │   ├── HomeLessonCard.kt       # Картка заняття з індикатором та прев'ю нотатки
    │   ├── LessonNoteDialog.kt     # Діалогове вікно створення та редагування приміток
    │   ├── RatingScoreDialog.kt    # Діалогове вікно розрахунку рейтингового бала
    │   └── ScheduleGridOverlay.kt  # Інтерактивна оверлей-сітка розкладу з жестами
    ├── screens/
    │   ├── HomeScreen.kt       # Головний екран розкладу (свайпи, автооновлення)
    │   ├── GradesScreen.kt     # Екран оцінок сесії (семестри, оновлення)
    │   ├── SettingsScreen.kt   # Екран параметрів облікового запису, теми та розкладу
    │   └── EditScreen.kt       # Повноекранний інтерфейс редагування занять
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 3. Шар даних (Data Layer)

### База даних Room (`digital_schedule.db`)

Структура таблиці `lessons`:
```kotlin
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,          // 1..7 (відповідно до ISO-8601 DayOfWeek)
    val weekType: String?,       // "1", "2" або null (для обох тижнів)
    val startTime: String,       // формат "HH:mm" (наприклад, "08:00")
    val endTime: String,         // формат "HH:mm" (наприклад, "09:35")
    val subject: String,         // назва навчальної дисципліни
    val teacher: String,         // прізвище та ініціали викладача
    val lessonType: String,      // значення переліку: "LECTURE", "LAB", "PRACTICE"
    val room: String,            // номер навчальної аудиторії
    val group: String?           // номер підгрупи (опціонально)
)
```

`LessonDao` надає реактивні потоки `Flow<List<LessonEntity>>` та методи CRUD-маніпуляцій (`insert`, `insertAll`, `update`, `delete`, `deleteAll`).

---

## 4. Шар доменних моделей (Model Layer)

### `ScheduleSettings`
Агрегує параметри користувацької конфігурації:
- `Username`, `Password` — облікові дані кабінету студента.
- `scheduleType` — режим чергування тижнів (`ONE_WEEK` або `TWO_WEEK`).
- `saturdayEnabled`, `saturdayType` — параметри активності та чергування суботніх занять.
- `accentColorHex` — колір оформлення у шістнадцятковому форматі (`#RRGGBB`).
- `isDarkTheme` — прапорець активації темного режиму.
- `lessonTimes` — список часових слотів `LessonTime` для пар 1..7.

---

## 5. Шар представлення (Presentation Layer)

### Навігація в застосунку
- **Нижня навігаційна панель (`BottomNavigationBar`)** забезпечує перемикання між розділами:
  - `Home` (Головна) — логічний індекс 0 у `MainActivity`.
  - `Grades` (Оцінки) — логічний індекс 1 у `MainActivity`.
  - `Settings` (Налаштування) — логічний індекс 2 у `MainActivity`.
- **Режим редагування розкладу (`EditScreen`)** відкривається з `HomeScreen` за допомогою кнопки редагування у верхній панелі. Під час редагування нижня панель приховується для забезпечення повноекранної взаємодії.

### Графічне оформлення
- **Динамічна палітра кольорів:** обраний користувачем акцентний колір динамічно інтегрується в колірну схему `ColorScheme` стандарту Material 3.
- **Ефекти розмиття:** панелі застосунку підтримують розмиття фону та напівпрозорі шари оформлення.

---

## 6. Мережева взаємодія та парсинг

### Клієнти та модулі синхронізації
- `NauCabinetClient`: забезпечує взаємодію з сервером `https://cabinet.nau.edu.ua`, зберігає сесійні cookie за допомогою `CookieManager`, здійснює автентифікацію студента та виконує парсинг семестрових відомостей.
- `ScheduleParser`: завантажує сторінку персонального розкладу занять та трансформує HTML-елементи у доменні структури `Lesson`.

---

## 7. Конфігурація та персистентність

- Ідентифікатори файлів SharedPreferences:
  - `digital_schedule_prefs`
  - `digital_schedule_grade_prefs`
  - `digital_schedule_notes_prefs`
- Ключі збереження:
  - `schedule_settings` — серіалізований об'єкт `ScheduleSettings`.
  - `grades_json` — кешована структура оцінок `Map<String, List<Grade>>`.
  - `notes_json` — асоціативний масив нотаток `Map<String, String>` з ключами формату `YYYY-MM-DD#subject#startTime#dayOfWeek#weekType`.

---

## 8. Залежності проєкту

| Бібліотека | Версія | Цільове призначення |
|---|---|---|
| `androidx.compose.material3:material3` | BOM 2024.09.00 | Компоненти користувацького інтерфейсу Material 3 |
| `androidx.room:*` | 2.6.1 | Локальна реляційна база даних |
| `com.squareup.okhttp3:okhttp` | 4.12.0 | HTTP-клієнт та мережеві запити |
| `org.jsoup:jsoup` | 1.17.2 | Синтаксичний аналіз HTML-сторінок |
| `com.google.code.gson:gson` | 2.10.1 | Серіалізація та десеріалізація JSON |
| `com.jakewharton.threetenabp:threetenabp` | 1.4.5 | Зворотна сумісність Java Time API (JSR-310) |

---

*Версія документації відповідає збірці: **4.0.69**.*

