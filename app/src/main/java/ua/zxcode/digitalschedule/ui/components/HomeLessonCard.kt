package ua.zxcode.digitalschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.model.displayNameUa

@Composable
fun HomeLessonCard(
    lesson: Lesson,
    accentColor: Color,
    note: String? = null,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .border(
                    width = 2.dp,
                    color = accentColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (!note.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 14.dp, end = 14.dp)
                            .size(10.dp)
                            .background(accentColor, CircleShape)
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${lesson.startTime} - ${lesson.endTime}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = lesson.teacher,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = lesson.lessonType.displayNameUa(), style = MaterialTheme.typography.bodySmall)
                    Text(text = lesson.room, style = MaterialTheme.typography.bodySmall)
                    lesson.group?.let {
                        Text(text = "Група: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    lesson.weekType?.let {
                        Text(
                            text = "Тиждень: $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }

                    if (!note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = accentColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = " $note",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

