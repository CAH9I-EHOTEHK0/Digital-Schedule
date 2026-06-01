package ua.zxcode.digitalschedule.parser

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.threeten.bp.DayOfWeek
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.model.LessonType

object ScheduleParser {

    private const val LOGIN_URL    = "https://cabinet.nau.edu.ua/login"
    private const val SCHEDULE_URL = "https://cabinet.nau.edu.ua/student/schedule"
    private const val USER_AGENT   =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0 Safari/537.36"

    // повертає список Lesson або кидає Exception з описом помилки
    fun fetchLessons(username: String, password: String): List<Lesson> {
        val cookies = login(username, password)
            ?: throw Exception("Невірний логін або пароль")

        val doc = Jsoup.connect(SCHEDULE_URL)
            .userAgent(USER_AGENT)
            .cookies(cookies)
            .get()

        return parseDoc(doc)
    }

    private fun login(username: String, password: String): Map<String, String>? {
        val getResp = Jsoup.connect(LOGIN_URL)
            .userAgent(USER_AGENT)
            .method(Connection.Method.GET)
            .execute()

        val csrf = getResp.parse()
            .selectFirst("meta[name=csrf-token]")
            ?.attr("content") ?: return null

        val postResp = Jsoup.connect(LOGIN_URL)
            .userAgent(USER_AGENT)
            .cookies(getResp.cookies())
            .header("Referer", LOGIN_URL)
            .data("_csrf-frontend", csrf)
            .data("LoginForm[username]", username)
            .data("LoginForm[password]", password)
            .method(Connection.Method.POST)
            .followRedirects(true)
            .execute()

        if (postResp.url().path.contains("login")) return null
        return getResp.cookies() + postResp.cookies()
    }

    private fun parseDoc(doc: org.jsoup.nodes.Document): List<Lesson> {
        val lessons = mutableListOf<Lesson>()

        // Перший пейн = тиждень 1, другий = тиждень 2
        val panes = doc.select("div[id^=week-pane-]")

        panes.forEachIndexed { paneIdx, weekPane ->
            val weekNumber = paneIdx + 1 // 1 або 2

            // Заголовки колонок з днями тижня
            val dayHeaders = weekPane.select("div.grid-header-row div.grid-cell")
                .map { cell ->
                    val dayText = cell.ownText().trim()
                    dayText
                }

            weekPane.select("div.grid-row").forEach { row ->
                val timeCell = row.selectFirst("div.grid-cell.bg-white") ?: return@forEach
                val spans = timeCell.select("span.text-md")
                if (spans.size < 2) return@forEach

                // Час: "8" + наступний sibling "30" → "08:30"
                fun buildTime(spanIdx: Int): String {
                    val h = spans[spanIdx].text().trim().padStart(2, '0')
                    val m = spans[spanIdx].nextElementSibling()?.text()?.trim()?.padStart(2, '0') ?: "00"
                    return "$h:$m"
                }
                val timeStart = buildTime(0)
                val timeEnd   = buildTime(1)

                val dayCells = row.select("div.grid-cell.border-top")
                dayCells.forEachIndexed { cellIdx, cell ->
                    val dayText = dayHeaders.getOrElse(cellIdx) { "" }
                    val dow = parseDayOfWeek(dayText) ?: return@forEachIndexed

                    cell.select("div.pair-card").forEach { card ->
                        val subject = card.selectFirst("div.font-weight-bold")
                            ?.text()?.trim() ?: return@forEach

                        val lessonTypeRaw = card.selectFirst("span.badge")
                            ?.text()?.trim() ?: ""

                        var teacher = ""
                        var room = ""
                        card.select("div.text-secondary").forEach { div ->
                            val icon = div.selectFirst("i") ?: return@forEach
                            val text = div.selectFirst("span")?.text()?.trim() ?: return@forEach
                            when {
                                icon.hasClass("fa-person")   -> teacher = text
                                icon.hasClass("fa-building") -> room    = text
                            }
                        }

                        // Група з бейджів
                        val badges = card.select("div.card-top-badge div")
                            .map { it.text().trim() }.filter { it.isNotBlank() }
                        val group = badges.firstOrNull { it.startsWith("Потік") || it.startsWith("Група") }

                        lessons.add(
                            Lesson(
                                id         = null,
                                dayOfWeek  = dow,
                                weekType   = weekNumber.toString(),
                                startTime  = timeStart,
                                endTime    = timeEnd,
                                subject    = subject,
                                teacher    = teacher,
                                lessonType = mapLessonType(lessonTypeRaw),
                                room       = room,
                                group      = group
                            )
                        )
                    }
                }
            }
        }

        return lessons
    }

    private fun parseDayOfWeek(text: String): DayOfWeek? = when {
        text.startsWith("Пн") || text.startsWith("Пон") -> DayOfWeek.MONDAY
        text.startsWith("Вт")                            -> DayOfWeek.TUESDAY
        text.startsWith("Ср")                            -> DayOfWeek.WEDNESDAY
        text.startsWith("Чт") || text.startsWith("Чет") -> DayOfWeek.THURSDAY
        text.startsWith("Пт") || text.startsWith("Пʼ") || text.startsWith("Пя") -> DayOfWeek.FRIDAY
        text.startsWith("Сб") || text.startsWith("Суб") -> DayOfWeek.SATURDAY
        else -> null
    }

    private fun mapLessonType(raw: String): LessonType = when {
        raw.contains("Лек", ignoreCase = true) -> LessonType.LECTURE
        raw.contains("Лаб", ignoreCase = true) -> LessonType.LAB
        else                                    -> LessonType.PRACTICE
    }
}