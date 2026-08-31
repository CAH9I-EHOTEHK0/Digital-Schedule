package ua.zxcode.digitalschedule.ui.components

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import ua.zxcode.digitalschedule.model.Lesson
import ua.zxcode.digitalschedule.model.displayNameUa
import androidx.compose.runtime.Composable

@Composable
fun HomeLessonCard(
    lesson: Lesson,
    accentColor: Color
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .width(320.dp)
                .border(
                    width = 2.dp,
                    color = accentColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
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
            }
        }
    }
}
