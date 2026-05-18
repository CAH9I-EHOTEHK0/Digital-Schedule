package ua.zxcode.digitalschedule.ui.screens

import androidx.compose.runtime.Composable
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import ua.zxcode.digitalschedule.model.ScheduleSettings
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import org.threeten.bp.DayOfWeek
import ua.zxcode.digitalschedule.model.AccentColor
import ua.zxcode.digitalschedule.model.ScheduleType
import ua.zxcode.digitalschedule.ui.components.HomeLessonCard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun HomeScreenContent(
    allLessons: List<Lesson>,
    lessonTimeManager: LessonTimeManager,
    scheduleSettings: ScheduleSettings
) {
    val allowedDays = if (scheduleSettings.saturdayEnabled)
        setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    else
        setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
    val dayNames = listOf("Понеділок", "Вівторок", "Середа", "Четвер", "Пʼятниця", "Субота", "Неділя")
    val today = org.threeten.bp.LocalDate.now(org.threeten.bp.ZoneId.of("Europe/Kyiv"))
    val shownDate = remember { mutableStateOf(today) }
    val density = LocalDensity.current
    val threshold = with(density) { 80.dp.toPx() }
    fun nextAllowedDate(from: org.threeten.bp.LocalDate, forward: Boolean): org.threeten.bp.LocalDate {
        var date = from
        do {
            date = if (forward) date.plusDays(1) else date.minusDays(1)
        } while (date.dayOfWeek !in allowedDays)
        return date
    }
    val isTwoWeek = scheduleSettings.scheduleType == ScheduleType.TWO_WEEK
    val refDateRaw = scheduleSettings.startReferenceDate?.let {
        try { org.threeten.bp.LocalDate.parse(it) } catch (_: Exception) { null }
    } ?: org.threeten.bp.LocalDate.of(2025, 11, 1)
    val refMonday = refDateRaw.minusDays(((refDateRaw.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())
    val daysBetweenMonday = org.threeten.bp.temporal.ChronoUnit.DAYS.between(refMonday, shownDate.value).toInt()
    val weekIndex = if (isTwoWeek) daysBetweenMonday / 7 else 0
    val weekNumber = if (!isTwoWeek) 1 else ((scheduleSettings.startReferenceWeekType - 1 + (weekIndex % 2 + 2) % 2) % 2) + 1
    val currentWeekLabel = if (isTwoWeek) {
        "Тиждень: $weekNumber"
    } else null
    val isSaturdayCycle = isTwoWeek && scheduleSettings.saturdayEnabled && shownDate.value.dayOfWeek == DayOfWeek.SATURDAY && scheduleSettings.saturdayCycleStartDate != null
    var saturdayCycleDay: Int? = null
    var saturdayCycleWeek: Int? = null
    if (isSaturdayCycle) {
        val startDate = try { org.threeten.bp.LocalDate.parse(scheduleSettings.saturdayCycleStartDate) } catch (_: Exception) { null }
        if (startDate != null && !shownDate.value.isBefore(startDate)) {
            if (startDate.dayOfWeek == DayOfWeek.SATURDAY && shownDate.value.dayOfWeek == DayOfWeek.SATURDAY) {
                val subotaCount = org.threeten.bp.temporal.ChronoUnit.WEEKS.between(startDate, shownDate.value).toInt()
                val cycleOptions = (1..5).map { Pair(it, 1) } + (1..5).map { Pair(it, 2) }
                val startIdx = cycleOptions.indexOfFirst { it.first == scheduleSettings.saturdayCycleStartDayOfWeek && it.second == scheduleSettings.saturdayCycleStartWeekType }.let { if (it == -1) 0 else it }
                val idx = (startIdx + subotaCount) % 10
                saturdayCycleDay = cycleOptions[idx].first
                saturdayCycleWeek = cycleOptions[idx].second
            }
        }
    }
    val lessonsForDay = if (isSaturdayCycle && saturdayCycleDay != null && saturdayCycleWeek != null) {
        allLessons.filter {
            it.dayOfWeek.value == saturdayCycleDay &&
            it.weekType?.toIntOrNull() == saturdayCycleWeek
        }
    } else {
        allLessons.filter {
            it.dayOfWeek.value == shownDate.value.dayOfWeek.value &&
            (scheduleSettings.scheduleType == ScheduleType.ONE_WEEK || it.weekType?.toIntOrNull() == weekNumber)
            && (scheduleSettings.saturdayEnabled || it.dayOfWeek.value < 6)
        }
    }
    val dragAccum = remember { mutableStateOf(0f) }
    val swipeHandled = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(shownDate.value) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragAccum.value = 0f
                        swipeHandled.value = false
                    },
                    onHorizontalDrag = { _: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float ->
                        dragAccum.value += dragAmount
                        if (!swipeHandled.value) {
                            if (dragAccum.value > threshold) {
                                shownDate.value = nextAllowedDate(shownDate.value, false)
                                swipeHandled.value = true
                            } else if (dragAccum.value < -threshold) {
                                shownDate.value = nextAllowedDate(shownDate.value, true)
                                swipeHandled.value = true
                            }
                        }
                    },
                    onDragEnd = {
                        dragAccum.value = 0f
                        swipeHandled.value = false
                    },
                    onDragCancel = {
                        dragAccum.value = 0f
                        swipeHandled.value = false
                    }
                )
            }
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = dayNames[shownDate.value.dayOfWeek.value - 1] + " (" + shownDate.value.toString() + ")",
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.CenterHorizontally)
                .padding(top = 32.dp),
            style = MaterialTheme.typography.titleLarge
        )
        if (isTwoWeek) {
            Text(
                text = currentWeekLabel ?: "",
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        if (lessonsForDay.isEmpty()) {
            if (isSaturdayCycle && saturdayCycleDay != null && saturdayCycleWeek != null) {
                val dayNamesShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт")
                Text("Субота: це ${dayNamesShort[saturdayCycleDay-1]} $saturdayCycleWeek тиждень. Пари для цього дня відсутні.", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            } else {
                Text("Розклад відсутній. Додайте пари.", modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 90.dp
                )) {
                items(lessonsForDay.size) { idx ->
                    val lesson = lessonsForDay[idx]
                    HomeLessonCard(
                        lesson = lesson,
                        accentColor = when (scheduleSettings.accentColor) {
                            AccentColor.RED -> Color(0xFFFF1744)
                            AccentColor.ORANGE -> Color(0xFFFF9100)
                            AccentColor.YELLOW -> Color(0xFFFFEA00)
                            AccentColor.GREEN -> Color(0xFF00E676)
                            AccentColor.BLUE -> Color(0xFF2979FF)
                            AccentColor.INDIGO -> Color(0xFF651FFF)
                            AccentColor.VIOLET -> Color(0xFFD500F9)
                        }
                    )
                }
            }
        }
    }
}
