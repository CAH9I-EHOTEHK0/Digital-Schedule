package ua.zxcode.digitalschedule.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import ua.zxcode.digitalschedule.model.LessonType
import org.threeten.bp.DayOfWeek
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import ua.zxcode.digitalschedule.model.ScheduleSettings
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun AddLessonDialog(
    onAdd: (Lesson) -> Unit,
    onCancel: () -> Unit,
    dayOfWeek: Int,
    lessonTimeManager: LessonTimeManager,
    scheduleSettings: ScheduleSettings,
    selectedWeekType: String? = null,
    initialLesson: Lesson? = null,
    isSaturdayCycled: Boolean = false
) {
    if (isSaturdayCycled) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Неможливо додати/редагувати пари для суботи з чергуванням") },
            text = { Text("Редагування пар для суботи з чергуванням відбувається автоматично. Додавання або зміна вручну недоступні.") },
            confirmButton = {
                Button(onClick = onCancel) { Text("OK") }
            },
            dismissButton = {}
        )
        return
    }

    var subject by remember { mutableStateOf(initialLesson?.subject ?: "") }
    var teacher by remember { mutableStateOf(initialLesson?.teacher ?: "") }
    var room by remember { mutableStateOf(initialLesson?.room ?: "") }
    var group by remember { mutableStateOf(initialLesson?.group ?: "") }
    var lessonType by remember { mutableStateOf(initialLesson?.lessonType ?: LessonType.LECTURE) }
    var timeIdx by remember { mutableStateOf(initialLesson?.let { lessonTimeManager.allLessonTimes().indexOfFirst { t -> t.start == it.startTime && t.end == it.endTime } }.takeIf { it != -1 } ?: 0) }
    var weekType by remember { mutableStateOf(initialLesson?.weekType ?: selectedWeekType) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (initialLesson == null) "Додати пару" else "Редагувати пару") },
        text = {
            Column {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Дисципліна") })
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Викладач") })
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Аудиторія") })
                OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Група") })
                Text("Тип заняття:")
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    item {
                        RadioButton(selected = lessonType == LessonType.LECTURE, onClick = { lessonType = LessonType.LECTURE })
                        Text("Лекція", modifier = Modifier.padding(end = 8.dp))
                    }
                    item {
                        RadioButton(selected = lessonType == LessonType.LAB, onClick = { lessonType = LessonType.LAB })
                        Text("Лабораторна", modifier = Modifier.padding(end = 8.dp))
                    }
                    item {
                        RadioButton(selected = lessonType == LessonType.PRACTICE, onClick = { lessonType = LessonType.PRACTICE })
                        Text("Практика")
                    }
                }
                Text("Час пари:")
                val times = lessonTimeManager.allLessonTimes()
                LazyRow {
                    items(times.size) { idx ->
                        Button(
                            onClick = { timeIdx = idx },
                            enabled = true,
                            content = { Text("${times[idx].start}-${times[idx].end}") },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                if (scheduleSettings.scheduleType == ua.zxcode.digitalschedule.model.ScheduleType.TWO_WEEK) {
                    Text("Тиждень:")
                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        item {
                            RadioButton(selected = weekType == "1", onClick = { weekType = "1" })
                            Text("1", modifier = Modifier.padding(end = 8.dp))
                        }
                        item {
                            RadioButton(selected = weekType == "2", onClick = { weekType = "2" })
                            Text("2", modifier = Modifier.padding(end = 8.dp))
                        }
                        item {
                            RadioButton(selected = weekType == null, onClick = { weekType = null })
                            Text("Обидва")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val lesson = Lesson(
                    id = initialLesson?.id,
                    dayOfWeek = DayOfWeek.of(dayOfWeek + 1),
                    weekType = weekType,
                    startTime = lessonTimeManager.allLessonTimes()[timeIdx].start,
                    endTime = lessonTimeManager.allLessonTimes()[timeIdx].end,
                    subject = subject,
                    teacher = teacher,
                    lessonType = lessonType,
                    room = room,
                    group = group.takeIf { it.isNotBlank() }
                )
                onAdd(lesson)
            }) { Text(if (initialLesson == null) "Додати" else "Зберегти") }
        },
        dismissButton = {
            Button(onClick = onCancel) { Text("Скасувати") }
        }
    )
}
