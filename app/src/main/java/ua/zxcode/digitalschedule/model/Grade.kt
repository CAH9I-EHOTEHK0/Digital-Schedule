package ua.zxcode.digitalschedule.model

data class Grade(
    val subject: String,
    val teacher: String,
    val examType: String,
    val componentTypeAbbr: String?,
    val componentTypeName: String?,
    val isSelective: Boolean = false,
    val points: Int?,
    val letterGrade: String?,
    val verbalGrade: String?,
    val date: String?,
    val approved: Boolean
)
