package ua.zxcode.digitalschedule.model

data class ScheduleSettings(
    var scheduleType: ScheduleType = ScheduleType.ONE_WEEK,
    var saturdayEnabled: Boolean = false,
    var lessonCount: Int = 7,
    var lessonTimes: List<LessonTime> = List(7) { LessonTime.default(it) },
    var breakDuration: Int = 5, // minutes
    var isDarkTheme: Boolean = false, // нове поле для теми
    var accentColor: AccentColor = AccentColor.BLUE, // нове поле для акценту
    var firstWeekStartDate: String? = null, // дата початку першого тижня у форматі yyyy-MM-dd
    var startReferenceDate: String? = null, // дата, з якої починається відлік (yyyy-MM-dd)
    var startReferenceDayOfWeek: Int = 4, // 1=Пн ... 7=Нд (за замовчуванням четвер)
    var startReferenceWeekType: Int = 1, // 1 або 2 (за замовчуванням 1 тиждень)
    // --- нові поля для 10-денного циклу субот ---
    var saturdayCycleStartDate: String? = null, // дата першої суботи циклу (yyyy-MM-dd)
    var saturdayCycleStartDayOfWeek: Int = 1, // 1=Пн ... 5=Пт
    var saturdayCycleStartWeekType: Int = 1, // 1 або 2
    var saturdayType: Int = 0 // 0 - статична, 1 - чергування
)

// Додаємо enum для акцентних кольорів веселки
enum class AccentColor(val displayName: String) {
    RED("Червоний"),
    ORANGE("Оранжевий"),
    YELLOW("Жовтий"),
    GREEN("Зелений"),
    BLUE("Синій"),
    INDIGO("Індиго"),
    VIOLET("Фіолетовий")
}

data class LessonTime(
    val index: Int,
    var start: String,
    var end: String
) {
    companion object {
        fun default(index: Int): LessonTime {
            val times = listOf(
                "08:30" to "10:00",
                "10:15" to "11:45",
                "12:00" to "13:30",
                "13:45" to "15:15",
                "15:30" to "17:00",
                "17:15" to "18:45",
                "19:00" to "20:30"
            )
            val (start, end) = times.getOrElse(index) { "" to "" }
            return LessonTime(index, start, end)
        }
    }
}
