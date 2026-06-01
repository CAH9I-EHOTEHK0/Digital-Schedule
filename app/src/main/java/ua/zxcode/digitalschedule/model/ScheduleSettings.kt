package ua.zxcode.digitalschedule.model

data class ScheduleSettings(
    var scheduleType: ScheduleType = ScheduleType.ONE_WEEK,
    var saturdayEnabled: Boolean = false,
    var lessonCount: Int = 7,
    var lessonTimes: List<LessonTime> = List(7) { LessonTime.default(it) },
    var breakDuration: Int = 5,
    var isDarkTheme: Boolean = false,
    var accentColor: AccentColor = AccentColor.BLUE,
    var firstWeekStartDate: String? = null,
    var startReferenceDate: String? = null,
    var startReferenceDayOfWeek: Int = 4,
    var startReferenceWeekType: Int = 1,
    var saturdayCycleStartDate: String? = null,
    var saturdayCycleStartDayOfWeek: Int = 1,
    var saturdayCycleStartWeekType: Int = 1,
    var saturdayType: Int = 0,
    // NAU credentials
    var Username: String = "",
    var Password: String = ""
)

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