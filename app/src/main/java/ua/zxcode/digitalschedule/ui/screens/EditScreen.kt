package ua.zxcode.digitalschedule.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.threeten.bp.DayOfWeek
import org.threeten.bp.format.TextStyle
import java.util.Locale
import ua.zxcode.digitalschedule.data.LessonStore
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.model.ScheduleSettings
import ua.zxcode.digitalschedule.model.displayNameUa
import ua.zxcode.digitalschedule.ui.components.AddLessonDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    allLessons: List<Lesson>,
    lessonTimeManager: LessonTimeManager,
    scheduleSettings: ScheduleSettings,
    lessonStore: LessonStore
) {
    val showAddDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf<Lesson?>(null) }
    val selectedDay = remember { mutableStateOf(DayOfWeek.MONDAY) }
    val selectedWeekType = remember { mutableStateOf<String?>(null) }
    val lessonsByDay = allLessons.groupBy { it.dayOfWeek }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Редагувати розклад") },
            actions = {
                IconButton(onClick = { showAddDialog.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Додати урок")
                }
            }
        )

        val daysToShow = DayOfWeek.values().filter {
            if (!scheduleSettings.saturdayEnabled && it == DayOfWeek.SATURDAY) return@filter false
            if (it == DayOfWeek.SUNDAY) return@filter false
            true
        }

        val isSaturdayCycledCurrent = scheduleSettings.saturdayEnabled && scheduleSettings.saturdayType == 1
        ScrollableTabRow(
            selectedTabIndex = daysToShow.indexOf(selectedDay.value),
            edgePadding = 0.dp
        ) {
            daysToShow.forEach { day ->
                Tab(
                    selected = selectedDay.value == day,
                    onClick = { selectedDay.value = day },
                    text = { Text(day.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("uk"))) }
                )
            }
        }

        if (scheduleSettings.scheduleType == ua.zxcode.digitalschedule.model.ScheduleType.TWO_WEEK) {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                item {
                    RadioButton(selected = selectedWeekType.value == "1", onClick = { selectedWeekType.value = "1" })
                    Text("1 тиждень", modifier = Modifier.padding(end = 8.dp))
                }
                item {
                    RadioButton(selected = selectedWeekType.value == "2", onClick = { selectedWeekType.value = "2" })
                    Text("2 тиждень", modifier = Modifier.padding(end = 8.dp))
                }
                item {
                    RadioButton(selected = selectedWeekType.value == null, onClick = { selectedWeekType.value = null })
                    Text("Обидва")
                }
            }
        }

        val lessons = lessonsByDay[selectedDay.value]?.filter {
            if (scheduleSettings.scheduleType == ua.zxcode.digitalschedule.model.ScheduleType.TWO_WEEK) {
                selectedWeekType.value == null || it.weekType == null || it.weekType == selectedWeekType.value
            } else true
        } ?: emptyList()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, start = 8.dp, end = 8.dp)
        ) {
            items(lessons.size) { index ->
                val lesson = lessons[index]
                Card(modifier = Modifier.padding(bottom = 8.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        Text(text = "${lesson.startTime} - ${lesson.endTime}")
                        Text(text = lesson.subject)
                        Text(text = lesson.teacher)
                        Text(text = lesson.lessonType.displayNameUa())
                        Text(text = lesson.room)
                        lesson.group?.let { Text(text = "Група: $it") }
                        Row(Modifier.fillMaxWidth(), Arrangement.End) {
                            Button(onClick = {
                                showEditDialog.value = lesson.copy(dayOfWeek = selectedDay.value)
                            }, modifier = Modifier.padding(end = 8.dp), enabled = !(selectedDay.value == DayOfWeek.SATURDAY && isSaturdayCycledCurrent)) { Text("Редагувати") }
                            Button(onClick = {
                                lessonStore.deleteLesson(lesson)
                            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), enabled = !(selectedDay.value == DayOfWeek.SATURDAY && isSaturdayCycledCurrent)) { Text("Видалити") }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { showAddDialog.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 26.dp),
                    enabled = !(selectedDay.value == DayOfWeek.SATURDAY && isSaturdayCycledCurrent)
                ) {
                    Text("Додати пару")
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }

    if (showAddDialog.value || showEditDialog.value != null) {
        AddLessonDialog(
            onAdd = { lesson ->
                val weekType = if (scheduleSettings.scheduleType == ua.zxcode.digitalschedule.model.ScheduleType.TWO_WEEK) selectedWeekType.value else null
                if (showEditDialog.value != null) {
                    lessonStore.updateLesson(lesson.copy(id = showEditDialog.value!!.id, dayOfWeek = selectedDay.value, weekType = weekType))
                    showEditDialog.value = null
                } else {
                    lessonStore.addLesson(lesson.copy(dayOfWeek = selectedDay.value, weekType = weekType))
                    showAddDialog.value = false
                }
            },
            onCancel = {
                showAddDialog.value = false
                showEditDialog.value = null
            },
            dayOfWeek = selectedDay.value.ordinal,
            lessonTimeManager = lessonTimeManager,
            scheduleSettings = scheduleSettings,
            selectedWeekType = selectedWeekType.value,
            initialLesson = showEditDialog.value,
            isSaturdayCycled = (scheduleSettings.saturdayEnabled && scheduleSettings.saturdayType == 1 && selectedDay.value == DayOfWeek.SATURDAY)
        )
    }
}