package ua.zxcode.digitalschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.threeten.bp.DayOfWeek
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.model.ScheduleSettings
import ua.zxcode.digitalschedule.model.ScheduleType
import ua.zxcode.digitalschedule.model.displayNameUa

@Composable
fun ScheduleGridOverlay(
    allLessons: List<Lesson>,
    lessonTimeManager: LessonTimeManager,
    scheduleSettings: ScheduleSettings,
    initialWeek: Int = 1,
    onDismiss: () -> Unit
) {
    val isTwoWeek = scheduleSettings.scheduleType == ScheduleType.TWO_WEEK
    var selectedWeek by remember { mutableStateOf(if (isTwoWeek) initialWeek else 1) }

    val days = if (scheduleSettings.saturdayEnabled) {
        listOf(
            DayOfWeek.MONDAY to "Понеділок",
            DayOfWeek.TUESDAY to "Вівторок",
            DayOfWeek.WEDNESDAY to "Середа",
            DayOfWeek.THURSDAY to "Четвер",
            DayOfWeek.FRIDAY to "П'ятниця",
            DayOfWeek.SATURDAY to "Субота"
        )
    } else {
        listOf(
            DayOfWeek.MONDAY to "Понеділок",
            DayOfWeek.TUESDAY to "Вівторок",
            DayOfWeek.WEDNESDAY to "Середа",
            DayOfWeek.THURSDAY to "Четвер",
            DayOfWeek.FRIDAY to "П'ятниця"
        )
    }

    val lessonTimes = lessonTimeManager.allLessonTimes().take(scheduleSettings.lessonCount.coerceAtLeast(5))
    val accentColor = scheduleSettings.getAccentColor()

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val isDark = scheduleSettings.isDarkTheme
    val crossColor = if (isDark) Color.White else Color.Black
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor.copy(alpha = 0.98f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .padding(horizontal = 24.dp, vertical = 90.dp)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, outlineColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Пара",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }

                        days.forEach { (_, name) ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }

                    lessonTimes.forEachIndexed { pairIndex, lt ->
                        val pairNum = pairIndex + 1
                        val timeSlotText = if (lt.start.isNotBlank() && lt.end.isNotBlank()) {
                            "${lt.start}\n${lt.end}"
                        } else {
                            "№$pairNum"
                        }

                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(88.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(0.5.dp, outlineColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$pairNum пара",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = timeSlotText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 13.sp),
                                        color = onSurfaceColor.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            days.forEach { (dayOfWeek, _) ->
                                Spacer(modifier = Modifier.width(6.dp))

                                val lesson = allLessons.firstOrNull { l ->
                                    l.dayOfWeek == dayOfWeek &&
                                    (if (isTwoWeek) l.weekType?.toIntOrNull() == selectedWeek || l.weekType == null else true) &&
                                    matchesTimeSlot(l.startTime, lt.start, pairNum)
                                }

                                GridCell(
                                    lesson = lesson,
                                    accentColor = accentColor,
                                    outlineColor = outlineColor
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTwoWeek) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { selectedWeek = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedWeek == 1) accentColor else Color.Transparent,
                                contentColor = if (selectedWeek == 1) crossColor else onSurfaceColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("1 тиждень", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedWeek = 2 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedWeek == 2) accentColor else Color.Transparent,
                                contentColor = if (selectedWeek == 2) crossColor else onSurfaceColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("2 тиждень", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { scale = (scale - 0.25f).coerceAtLeast(0.5f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Зменшити", modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "${(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(3.0f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Збільшити", modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Скинути", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 48.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрити таблицю",
                        tint = crossColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    lesson: Lesson?,
    accentColor: Color,
    outlineColor: Color
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (lesson != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
            .border(
                width = if (lesson != null) 1.dp else 0.5.dp,
                color = if (lesson != null) accentColor.copy(alpha = 0.4f) else outlineColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.TopStart
    ) {
        if (lesson != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lesson.lessonType.displayNameUa(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1
                        )
                        if (lesson.room.isNotBlank()) {
                            Text(
                                text = "ауд. ${lesson.room}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 13.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (lesson.teacher.isNotBlank()) {
                    Text(
                        text = lesson.teacher,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun matchesTimeSlot(lessonStartTime: String, slotStart: String, pairNum: Int): Boolean {
    if (lessonStartTime.isNotBlank() && slotStart.isNotBlank() && lessonStartTime == slotStart) {
        return true
    }

    val defaultStarts = listOf("08:00", "09:50", "11:40", "13:30", "15:20", "17:10", "19:00")
    val defaultSlot = defaultStarts.getOrNull(pairNum - 1) ?: ""
    return lessonStartTime == defaultSlot
}
