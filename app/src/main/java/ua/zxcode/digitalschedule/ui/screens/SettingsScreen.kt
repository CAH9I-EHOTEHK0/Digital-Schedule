package ua.zxcode.digitalschedule.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import ua.zxcode.digitalschedule.model.ScheduleSettings
import ua.zxcode.digitalschedule.model.parseHexColor
import ua.zxcode.digitalschedule.model.toHex

@Composable
fun SettingsScreen(
    settings: ScheduleSettings,
    lessonTimeManager: LessonTimeManager,
    context: Context,
    onSaveSettings: (Context, ScheduleSettings) -> Unit
) {
    val scrollState = rememberScrollState()
    var accentColorHex by remember { mutableStateOf(settings.accentColorHex) }
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
    var Username by remember { mutableStateOf(settings.Username) }
    var Password by remember { mutableStateOf(settings.Password) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 96.dp)
    ) {
        Text(
            text = "Налаштування",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ── NAU авторизація ──────────────────────────────────────────────────
        Text("Кабінет студента", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = Username,
            onValueChange = { Username = it },
            label = { Text("Логін (номер залікової)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = Password,
            onValueChange = { Password = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ── Тема ─────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Тема:", modifier = Modifier.weight(1f))
            Switch(checked = isDarkTheme, onCheckedChange = { isDarkTheme = it })
            Text(if (isDarkTheme) "Темна" else "Світла")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Колір акценту (Палітра, HEX, RGB) ─────────────────────────────────
        AccentColorPicker(
            currentHex = accentColorHex,
            onColorChanged = { newHex -> accentColorHex = newHex }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ── Тип розкладу ──────────────────────────────────────────────────────
        Text("Тип розкладу:", style = MaterialTheme.typography.titleMedium)
        Row {
            RadioButton(
                selected = scheduleType.name == "ONE_WEEK",
                onClick = { scheduleType = ua.zxcode.digitalschedule.model.ScheduleType.ONE_WEEK }
            )
            Text("Однотижневий", modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = scheduleType.name == "TWO_WEEK",
                onClick = { scheduleType = ua.zxcode.digitalschedule.model.ScheduleType.TWO_WEEK }
            )
            Text("Двотижневий")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
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
        LazyRow(modifier = Modifier.fillMaxWidth()) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Пара ${idx + 1}", modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = start,
                        onValueChange = {
                            start = it
                            lessonTimes[idx] = lessonTimes[idx].copy(start = it)
                        },
                        label = { Text("Початок") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = end,
                        onValueChange = {
                            end = it
                            lessonTimes[idx] = lessonTimes[idx].copy(end = it)
                        },
                        label = { Text("Кінець") },
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

        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("День тижня для старту суботи:", modifier = Modifier.weight(1f))
                (1..5).forEach { n ->
                    RadioButton(selected = saturdayCycleStartDayOfWeek == n, onClick = { saturdayCycleStartDayOfWeek = n })
                    Text(n.toString())
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    accentColorHex = accentColorHex,
                    scheduleType = scheduleType,
                    saturdayEnabled = saturdayEnabled,
                    lessonCount = lessonsPerDay,
                    lessonTimes = lessonTimes.take(lessonsPerDay).map { it.copy() },
                    startReferenceDate = startReferenceDate,
                    startReferenceWeekType = startReferenceWeekType,
                    saturdayCycleStartDate = saturdayCycleStartDate,
                    saturdayCycleStartDayOfWeek = saturdayCycleStartDayOfWeek,
                    saturdayCycleStartWeekType = saturdayCycleStartWeekType,
                    saturdayType = saturdayType,
                    Username = Username,
                    Password = Password
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

@Composable
private fun AccentColorPicker(
    currentHex: String,
    onColorChanged: (String) -> Unit
) {
    var hexInput by remember(currentHex) { mutableStateOf(currentHex) }
    val currentColor = remember(currentHex) { parseHexColor(currentHex) }

    val presetColors = listOf(
        "#FF1744", "#FF9100", "#FFEA00", "#00E676",
        "#00E5FF", "#2979FF", "#651FFF", "#D500F9", "#FF4081"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Колір акценту", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Готові шаблони кольорів (Палітра кружечків)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presetColors.forEach { hex ->
                val color = parseHexColor(hex)
                val isSelected = currentHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            hexInput = hex
                            onColorChanged(hex)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Інтерактивна Спектр-палітра (Hue Gradient Slider)
        Text("Палітра спектру кольорів:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        val spectrumBrush = Brush.horizontalGradient(
            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(spectrumBrush)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val hue = fraction * 360f
                        val newColorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                        val newHex = Color(newColorInt).toHex()
                        hexInput = newHex
                        onColorChanged(newHex)
                    }
                }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Поле для вводу HEX (з прев'ю блоком кольору)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(currentColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    hexInput = input
                    val clean = input.trim()
                    if (clean.matches(Regex("^#?[0-9a-fA-F]{6}$")) || clean.matches(Regex("^#?[0-9a-fA-F]{8}$"))) {
                        val formattedHex = if (clean.startsWith("#")) clean else "#$clean"
                        onColorChanged(formattedHex)
                    }
                },
                label = { Text("HEX код (напр. #651FFF)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Слайдери RGB (Red, Green, Blue)
        val r = (currentColor.red * 255).toInt().coerceIn(0, 255)
        val g = (currentColor.green * 255).toInt().coerceIn(0, 255)
        val b = (currentColor.blue * 255).toInt().coerceIn(0, 255)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            RgbRow(label = "R", value = r, barColor = Color.Red) { newR ->
                val newHex = Color(newR, g, b).toHex()
                hexInput = newHex
                onColorChanged(newHex)
            }
            RgbRow(label = "G", value = g, barColor = Color.Green) { newG ->
                val newHex = Color(r, newG, b).toHex()
                hexInput = newHex
                onColorChanged(newHex)
            }
            RgbRow(label = "B", value = b, barColor = Color.Blue) { newB ->
                val newHex = Color(r, g, newB).toHex()
                hexInput = newHex
                onColorChanged(newHex)
            }
        }
    }
}

@Composable
private fun RgbRow(
    label: String,
    value: Int,
    barColor: Color,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(color = barColor),
            modifier = Modifier.width(20.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = barColor,
                activeTrackColor = barColor
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(32.dp)
        )
    }
}