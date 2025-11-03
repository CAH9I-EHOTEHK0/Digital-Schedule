package ua.zxcode.digitalschedule.model

fun LessonType.displayNameUa(): String = when (this) {
    LessonType.LECTURE -> "Лекція"
    LessonType.LAB -> "Лабораторна"
    LessonType.PRACTICE -> "Практика"
}
