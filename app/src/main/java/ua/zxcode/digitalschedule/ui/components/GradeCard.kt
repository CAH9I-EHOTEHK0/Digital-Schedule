package ua.zxcode.digitalschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.zxcode.digitalschedule.model.Grade

@Composable
fun GradeCard(
    grade: Grade,
    accentColor: Color
) {
    val ratingColor = when {
        grade.points == null -> Color(0xFF94A3B8)
        grade.points >= 90 -> Color(0xFF22C55E)
        grade.points >= 75 -> Color(0xFF4ADE80)
        grade.points >= 60 -> Color(0xFFFACC15)
        else -> Color(0xFFEF4444)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = accentColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181825)),
            elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // ── Верхній рядок з бейджами (Форма контролю + Абревіатура типу ОК + Дата) ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Форма контролю (жовто-оранжевий бейдж)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD97706))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = grade.examType,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            // Абревіатура типу компонента (Д - Дисципліна, КР - Курсова, П - Практика)
                            grade.componentTypeAbbr?.let { abbr ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFB45309))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = abbr,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Бейдж "Вибіркова"
                            if (grade.isSelective) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF7C3AED))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Вибіркова",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }

                        // Дата бейдж
                        grade.date?.let { dateStr ->
                            val isLate = dateStr.contains("невчасно")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLate) Color(0xFF991B1B) else Color(0xFF1E293B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dateStr,
                                    color = if (isLate) Color(0xFFFCA5A5) else Color(0xFFCBD5E1),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Основний блок: Назва предмета/викладач зліва + Кільце оцінки справа ────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ліва частина (Назва та Викладач)
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = grade.subject.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                ),
                                color = Color.White
                            )
                            if (grade.teacher.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = grade.teacher,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Права частина (Круговий індикатор оцінки + Словесна оцінка)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Фонове кільце
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFF334155),
                                    strokeWidth = 4.dp
                                )
                                // Прогрес кільце
                                CircularProgressIndicator(
                                    progress = { (grade.points ?: 0) / 100f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = ratingColor,
                                    strokeWidth = 4.dp
                                )
                                // Текст всередині кільця (Бал зверху, Літера A/B/C знизу)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = grade.points?.toString() ?: "—",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    grade.letterGrade?.let { letter ->
                                        Text(
                                            text = letter,
                                            color = ratingColor,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            grade.verbalGrade?.let { verbal ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = verbal,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }

                // ── Нижня плашка "Погоджено оцінку" (якщо approved == true) ─────────────────
                if (grade.approved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF15803D))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Погоджено оцінку",
                            color = Color(0xFFDCFCE7),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
