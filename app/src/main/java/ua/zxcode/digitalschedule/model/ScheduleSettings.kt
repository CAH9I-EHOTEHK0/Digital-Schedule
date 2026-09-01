package ua.zxcode.digitalschedule.model

import androidx.compose.ui.graphics.Color

data class ScheduleSettings(
    var scheduleType: ScheduleType = ScheduleType.ONE_WEEK,
    var saturdayEnabled: Boolean = false,
    var lessonCount: Int = 7,
    var lessonTimes: List<LessonTime> = List(7) { LessonTime.default(it) },
    var breakDuration: Int = 5,
    var isDarkTheme: Boolean = false,
    var accentColorHex: String = "#651FFF",
    var accentColor: AccentColor = AccentColor.INDIGO,
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
) {
    fun getAccentColor(): Color {
        return parseHexColor(accentColorHex)
    }
}

enum class AccentColor(val displayName: String) {
    RED("Червоний"),
    ORANGE("Оранжевий"),
    YELLOW("Жовтий"),
    GREEN("Зелений"),
    BLUE("Синій"),
    INDIGO("Індиго"),
    VIOLET("Фіолетовий")
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFF651FFF)): Color {
    val clean = hex.removePrefix("#").trim()
    return try {
        when (clean.length) {
            6 -> Color(android.graphics.Color.parseColor("#FF$clean"))
            8 -> Color(android.graphics.Color.parseColor("#$clean"))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

fun Color.toHex(): String {
    val redInt = (this.red * 255).toInt().coerceIn(0, 255)
    val greenInt = (this.green * 255).toInt().coerceIn(0, 255)
    val blueInt = (this.blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", redInt, greenInt, blueInt)
}

data class LessonTime(
    val index: Int,
    var start: String,
    var end: String
) {
    companion object {
        fun default(index: Int): LessonTime {
            val times = listOf(
                "08:00" to "09:35",
                "09:50" to "11:25",
                "11:40" to "13:15",
                "13:30" to "15:05",
                "15:20" to "16:55",
                "17:10" to "18:45",
                "19:00" to "20:35"
            )
            val (start, end) = times.getOrElse(index) { "" to "" }
            return LessonTime(index, start, end)
        }
    }
}