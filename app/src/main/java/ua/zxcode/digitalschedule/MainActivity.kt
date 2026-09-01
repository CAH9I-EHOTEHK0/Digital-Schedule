package ua.zxcode.digitalschedule

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.gson.Gson
import ua.zxcode.digitalschedule.data.LessonStore
import ua.zxcode.digitalschedule.manager.LessonTimeManager
import ua.zxcode.digitalschedule.model.ScheduleSettings
import ua.zxcode.digitalschedule.ui.theme.DigitalScheduleTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.zxcode.digitalschedule.ui.screens.HomeScreenContent
import ua.zxcode.digitalschedule.ui.screens.EditScreen
import ua.zxcode.digitalschedule.ui.screens.SettingsScreen

import ua.zxcode.digitalschedule.data.GradeStore
import ua.zxcode.digitalschedule.ui.screens.GradesScreen

class MainActivity : ComponentActivity() {
    private val SETTINGS_KEY = "schedule_settings"
    private val gson = Gson()
    private val PREFS_NAME = "digital_schedule_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = this
            val lessonStore = remember { LessonStore(context) }
            val gradeStore = remember { GradeStore(context) }
            val noteStore = remember { ua.zxcode.digitalschedule.data.NoteStore(context) }
            val lessonsState = lessonStore.lessons.collectAsState()
            val allLessons = lessonsState.value
            val settingsState = remember { mutableStateOf(loadSettings(context)) }
            val lessonTimeManager = remember { LessonTimeManager(settingsState.value.lessonTimes.toMutableList()) }
            val selectedTab = remember { mutableStateOf(0) }
            val isEditingScreenOpen = remember { mutableStateOf(false) }

            LaunchedEffect(settingsState) {
                saveSettings(context, settingsState.value)
            }

            DigitalScheduleTheme(
                darkTheme = settingsState.value.isDarkTheme,
                accentColorHex = settingsState.value.accentColorHex
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isEditingScreenOpen.value) {
                                EditScreen(
                                    allLessons = allLessons,
                                    lessonTimeManager = lessonTimeManager,
                                    scheduleSettings = settingsState.value,
                                    lessonStore = lessonStore,
                                    onNavigateBack = { isEditingScreenOpen.value = false }
                                )
                            } else {
                                when (selectedTab.value) {
                                    0 -> HomeScreenContent(
                                        allLessons = allLessons,
                                        lessonTimeManager = lessonTimeManager,
                                        scheduleSettings = settingsState.value,
                                        lessonStore = lessonStore,
                                        noteStore = noteStore,
                                        onNavigateToEdit = { isEditingScreenOpen.value = true }
                                    )
                                    1 -> GradesScreen(
                                        gradeStore = gradeStore,
                                        scheduleSettings = settingsState.value
                                    )
                                    2 -> SettingsScreen(
                                        settings = settingsState.value,
                                        lessonTimeManager = lessonTimeManager,
                                        context = context,
                                        onSaveSettings = { ctx, newSettings ->
                                            settingsState.value = newSettings.copy()
                                            lessonTimeManager.updateTimes(newSettings.lessonTimes.toMutableList())
                                            saveSettings(ctx, newSettings)
                                        }
                                    )
                                }
                            }
                        }

                        if (!isEditingScreenOpen.value) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ) {
                                ua.zxcode.digitalschedule.ui.BottomNavigationBar(
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    selectedIndex = selectedTab.value,
                                    onItemSelected = { selectedTab.value = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveSettings(context: Context, settings: ScheduleSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SETTINGS_KEY, gson.toJson(settings)).apply()
    }

    private fun loadSettings(context: Context): ScheduleSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(SETTINGS_KEY, null) ?: return ScheduleSettings()
        return try { gson.fromJson(json, ScheduleSettings::class.java) } catch (_: Exception) { ScheduleSettings() }
    }
}