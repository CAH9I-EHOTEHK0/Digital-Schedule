package ua.zxcode.digitalschedule.model

import java.time.DayOfWeek

enum class ScheduleType {
    ONE_WEEK, TWO_WEEK
}

enum class LessonType {
    LECTURE, LAB, PRACTICE
}

data class Lesson(
    val id: Long? = null,
    val dayOfWeek: org.threeten.bp.DayOfWeek,
    val weekType: String?,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacher: String,
    val lessonType: LessonType,
    val room: String,
    val group: String? = null
)

data class DaySchedule(
    val dayOfWeek: DayOfWeek,
    val lessons: List<Lesson>
)

data class WeekSchedule(
    val weekType: String?,
    val days: List<DaySchedule>
)
