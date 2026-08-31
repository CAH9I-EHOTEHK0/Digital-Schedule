package ua.zxcode.digitalschedule.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.zxcode.digitalschedule.data.GradeStore
import ua.zxcode.digitalschedule.model.AccentColor
import ua.zxcode.digitalschedule.model.Grade
import ua.zxcode.digitalschedule.model.ScheduleSettings
import ua.zxcode.digitalschedule.parser.NauCabinetClient
import ua.zxcode.digitalschedule.ui.components.GradeCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GradesScreen(
    gradeStore: GradeStore,
    scheduleSettings: ScheduleSettings
) {
    val gradesState = gradeStore.grades.collectAsState()
    val allGrades = gradesState.value
    val availableSemesters = remember(allGrades) { allGrades.keys.toList() }

    var syncState by remember { mutableStateOf<GradeSyncState>(GradeSyncState.Idle) }
    val scope = rememberCoroutineScope()

    val accentColorValue = when (scheduleSettings.accentColor) {
        AccentColor.RED    -> Color(0xFFFF1744)
        AccentColor.ORANGE -> Color(0xFFFF9100)
        AccentColor.YELLOW -> Color(0xFFFFEA00)
        AccentColor.GREEN  -> Color(0xFF00E676)
        AccentColor.BLUE   -> Color(0xFF2979FF)
        AccentColor.INDIGO -> Color(0xFF651FFF)
        AccentColor.VIOLET -> Color(0xFFD500F9)
    }

    fun doSync() {
        if (scheduleSettings.Username.isBlank()) {
            syncState = GradeSyncState.Error("Вкажіть логін і пароль у Налаштуваннях")
            return
        }
        scope.launch {
            syncState = GradeSyncState.Loading
            try {
                val fetched = withContext(Dispatchers.IO) {
                    val client = NauCabinetClient()
                    val loggedIn = client.login(scheduleSettings.Username, scheduleSettings.Password)
                    if (!loggedIn) throw Exception("Не вдалося залогінитись у кабінет НАУ")
                    client.fetchAllGrades()
                }
                gradeStore.saveGrades(fetched)
                syncState = GradeSyncState.Success("Оцінки успішно оновлено")
            } catch (e: Exception) {
                syncState = GradeSyncState.Error(e.message ?: "Помилка оновлення")
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (availableSemesters.isEmpty()) 1 else availableSemesters.size }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Верхня непрозора шапка з вирівняним по центру заголовком ─────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Лівий невидимий плейсхолдер для ідеального центрування заголовка
                    Box(modifier = Modifier.size(40.dp))

                    // Заголовок по центру
                    Text(
                        text = "Оцінки",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Права кнопка оновлення з фіолетовим бекграундом
                    IconButton(
                        onClick = { doSync() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(accentColorValue, shape = androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Оновити оцінки",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Перемикач семестрів (Tabs)
                if (availableSemesters.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage.coerceIn(0, availableSemesters.size - 1),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent
                    ) {
                        availableSemesters.forEachIndexed { index, semester ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = { Text(semester) }
                            )
                        }
                    }
                }

                // Повідомлення про результат синхронізації
                when (val s = syncState) {
                    is GradeSyncState.Success -> Text(
                        s.message,
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color(0xFF00C853),
                        style = MaterialTheme.typography.bodySmall
                    )
                    is GradeSyncState.Error -> Text(
                        "⚠ ${s.message}",
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color(0xFFD50000),
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> {}
                }
            }
        }

        // ── Основний вміст (HorizontalPager для свайпів) ──────────────────────────────────
        if (syncState is GradeSyncState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = accentColorValue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Завантаження оцінок з кабінету НАУ...")
                }
            }
        } else if (allGrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Оцінки відсутні.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (scheduleSettings.Username.isBlank())
                            "Вкажіть ваш логін та пароль в Налаштуваннях і натисніть кнопку оновити."
                        else "Натисніть кнопку оновити щоб завантажити оцінки.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { doSync() },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColorValue)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Оновити", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Оновити оцінки")
                    }
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val semesterKey = availableSemesters.getOrNull(pageIndex)
                val semesterGrades = semesterKey?.let { allGrades[it] } ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp)
                ) {
                    if (semesterGrades.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Оцінок у цьому семестрі ще немає")
                            }
                        }
                    } else {
                        items(semesterGrades.size) { index ->
                            GradeCard(grade = semesterGrades[index], accentColor = accentColorValue)
                        }
                    }
                }
            }
        }
    }
}

private sealed class GradeSyncState {
    object Idle : GradeSyncState()
    object Loading : GradeSyncState()
    data class Success(val message: String) : GradeSyncState()
    data class Error(val message: String) : GradeSyncState()
}
