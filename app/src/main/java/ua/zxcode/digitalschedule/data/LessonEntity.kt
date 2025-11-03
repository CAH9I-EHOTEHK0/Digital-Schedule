package ua.zxcode.digitalschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.DayOfWeek
import ua.zxcode.digitalschedule.model.LessonType

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,
    val weekType: String?,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacher: String,
    val lessonType: String,
    val room: String,
    val group: String?
) {
    fun toLesson(): ua.zxcode.digitalschedule.model.Lesson = ua.zxcode.digitalschedule.model.Lesson(
        id = id,
        dayOfWeek = DayOfWeek.of(dayOfWeek),
        weekType = weekType,
        startTime = startTime,
        endTime = endTime,
        subject = subject,
        teacher = teacher,
        lessonType = LessonType.valueOf(lessonType),
        room = room,
        group = group
    )

    companion object {
        fun fromLesson(lesson: ua.zxcode.digitalschedule.model.Lesson): LessonEntity = LessonEntity(
            id = lesson.id ?: 0,
            dayOfWeek = lesson.dayOfWeek.value,
            weekType = lesson.weekType,
            startTime = lesson.startTime,
            endTime = lesson.endTime,
            subject = lesson.subject,
            teacher = lesson.teacher,
            lessonType = lesson.lessonType.name,
            room = lesson.room,
            group = lesson.group
        )
    }
}
