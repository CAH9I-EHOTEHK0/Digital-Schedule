package ua.zxcode.digitalschedule.ui.screens

import androidx.compose.runtime.Composable
import ua.zxcode.digitalschedule.model.ScheduleSettings
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import ua.zxcode.digitalschedule.model.AccentColor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun SettingsScreen(
    settings: ScheduleSettings,
    lessonTimeManager: LessonTimeManager,
    context: Context,
    onSaveSettings: (Context, ScheduleSettings) -> Unit
) {
    val scrollState = rememberScrollState()
    var accentColor by remember { mutableStateOf(settings.accentColor) }
    var isDarkTheme by remember { mutableStateOf(settings.isDarkTheme) }
    var scheduleType by remember { mutableStateOf(settings.scheduleType) }
    var saturdayEnabled by remember { mutableStateOf(settings.saturdayEnabled) }
    var saturdayType by remember { mutableStateOf(settings.saturdayType) }
    var lessonsPerDay by remember { mutableStateOf(settings.lessonCount) }
    var lessonTimes by remember { mutableStateOf(settings.lessonTimes.map { it.copy() }.toMutableList()) }
    var startReferenceDate by remember { mutableStateOf(settings.startReferenceDate ?: "") }
    var startReferenceWeekType by remember { mutableStateOf(settings.startReferenceWeekType) }
    var saturdayCycleStartDate by remember { mutableStateOf(settings.saturdayCycleStartDate ?: "") }
    var saturdayCycleStartDayOfWeek by remember { mutableStateOf(settings.saturdayCycleStartDayOfWeek) }
    var saturdayCycleStartWeekType by remember { mutableStateOf(settings.saturdayCycleStartWeekType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)
    ) {
        Text("Налаштування", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Тема:", modifier = Modifier.weight(1f))
            Switch(checked = isDarkTheme, onCheckedChange = { isDarkTheme = it })
            Text(if (isDarkTheme) "Темна" else "Світла")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Колір акценту", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AccentColor.entries.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { accentColor = color }
                        .border(
                            width = 2.dp,
                            color = if (accentColor == color) Color.Black else Color.Transparent
                        )
                        .background(getAccentColor(color))
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Тип розкладу:", style = MaterialTheme.typography.titleMedium)
        Row {
            RadioButton(selected = scheduleType.name == "ONE_WEEK", onClick = { scheduleType = ua.zxcode.digitalschedule.model.ScheduleType.ONE_WEEK })
            Text("Однотижневий", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = scheduleType.name == "TWO_WEEK", onClick = { scheduleType = ua.zxcode.digitalschedule.model.ScheduleType.TWO_WEEK })
            Text("Двотижневий")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Субота у розкладі:", modifier = Modifier.weight(1f))
            Switch(checked = saturdayEnabled, onCheckedChange = { saturdayEnabled = it })
            Text(if (saturdayEnabled) "Так" else "Ні")
        }
        if (saturdayEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Тип суботи:", style = MaterialTheme.typography.titleMedium)
            Row {
                RadioButton(selected = saturdayType == 0, onClick = { saturdayType = 0 })
                Text("Статична", modifier = Modifier.padding(end = 16.dp))
                RadioButton(selected = saturdayType == 1, onClick = { saturdayType = 1 })
                Text("Чергування")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Кількість пар на день:", style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(8) { n ->
                Button(
                    onClick = { lessonsPerDay = n + 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lessonsPerDay == n + 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.padding(end = 4.dp)
                ) { Text((n + 1).toString()) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Час пар:", style = MaterialTheme.typography.titleMedium)
        Column {
            lessonTimes.take(lessonsPerDay).forEachIndexed { idx, time ->
                var start by remember { mutableStateOf(time.start) }
                var end by remember { mutableStateOf(time.end) }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Пара ${idx + 1}", modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = start,
                        onValueChange = {
                            start = it
                            lessonTimes[idx] = lessonTimes[idx].copy(start = it)
                        },
                        label = { Text("Початок (наприклад, 08:30)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = end,
                        onValueChange = {
                            end = it
                            lessonTimes[idx] = lessonTimes[idx].copy(end = it)
                        },
                        label = { Text("Кінець (наприклад, 09:50)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = startReferenceDate,
            onValueChange = { startReferenceDate = it },
            label = { Text("Дата початку першого тижня (рррр-мм-дд)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Тип першого тижня:", modifier = Modifier.weight(1f))
            RadioButton(selected = startReferenceWeekType == 1, onClick = { startReferenceWeekType = 1 })
            Text("1")
            RadioButton(selected = startReferenceWeekType == 2, onClick = { startReferenceWeekType = 2 })
            Text("2")
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (saturdayEnabled && saturdayType == 1) {
            OutlinedTextField(
                value = saturdayCycleStartDate,
                onValueChange = { saturdayCycleStartDate = it },
                label = { Text("Дата початку циклу субот (рррр-мм-дд)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("День тижня для старту суботи:", modifier = Modifier.weight(1f))
                (1..5).forEach { n ->
                    RadioButton(selected = saturdayCycleStartDayOfWeek == n, onClick = { saturdayCycleStartDayOfWeek = n })
                    Text(n.toString())
                }
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Тип тижня для старту суботи:", modifier = Modifier.weight(1f))
                RadioButton(selected = saturdayCycleStartWeekType == 1, onClick = { saturdayCycleStartWeekType = 1 })
                Text("1")
                RadioButton(selected = saturdayCycleStartWeekType == 2, onClick = { saturdayCycleStartWeekType = 2 })
                Text("2")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val updatedSettings = settings.copy(
                    isDarkTheme = isDarkTheme,
                    accentColor = accentColor,
                    scheduleType = scheduleType,
                    saturdayEnabled = saturdayEnabled,
                    lessonCount = lessonsPerDay,
                    lessonTimes = lessonTimes.take(lessonsPerDay).map { it.copy() },
                    startReferenceDate = startReferenceDate,
                    startReferenceWeekType = startReferenceWeekType,
                    saturdayCycleStartDate = saturdayCycleStartDate,
                    saturdayCycleStartDayOfWeek = saturdayCycleStartDayOfWeek,
                    saturdayCycleStartWeekType = saturdayCycleStartWeekType,
                    saturdayType = saturdayType
                )
                lessonTimeManager.updateTimes(updatedSettings.lessonTimes.toMutableList())
                onSaveSettings(context.applicationContext, updatedSettings)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зберегти налаштування")
        }
    }
}

private fun getAccentColor(accent: AccentColor): Color = when (accent) {
    AccentColor.RED -> Color(0xFFFF1744)
    AccentColor.ORANGE -> Color(0xFFFF9100)
    AccentColor.YELLOW -> Color(0xFFFFEA00)
    AccentColor.GREEN -> Color(0xFF00E676)
    AccentColor.BLUE -> Color(0xFF2979FF)
    AccentColor.INDIGO -> Color(0xFF651FFF)
    AccentColor.VIOLET -> Color(0xFFD500F9)
}
