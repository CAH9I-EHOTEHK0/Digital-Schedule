package ua.zxcode.digitalschedule.model

data class Grade(
    val subject: String,
    val teacher: String,
    val examType: String,            // "Екзамен", "Диференційований залік" тощо
    val componentTypeAbbr: String?,  // "Д", "КР", "П" тощо
    val componentTypeName: String?,  // "Навчальна дисципліна", "Курсова робота", "Практична підготовка"
    val isSelective: Boolean = false,// вибіркова дисципліна
    val points: Int?,                // 0..100, якщо вже виставлено
    val letterGrade: String?,        // A, B, C...
    val verbalGrade: String?,        // "Відмінно", "Добре" тощо
    val date: String?,
    val approved: Boolean            // чи є погодження (agreement) для цієї оцінки
)
