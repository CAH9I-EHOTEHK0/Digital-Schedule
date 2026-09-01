package ua.zxcode.digitalschedule.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.zxcode.digitalschedule.model.Lesson

class NoteStore(context: Context) {
    private val prefs = context.getSharedPreferences("digital_schedule_notes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Key format: "YYYY-MM-DD#subject#startTime#dayOfWeek#weekType" -> note text
    private val _notes = MutableStateFlow<Map<String, String>>(loadNotes())
    val notes: StateFlow<Map<String, String>> = _notes

    private fun loadNotes(): Map<String, String> {
        val json = prefs.getString("notes_json", null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun makeNoteKey(dateStr: String, lesson: Lesson): String {
        // Формуємо стійкий ідентифікатор пари, незалежний від автоінкрементного ID в базі
        val cleanSubject = lesson.subject.trim()
        val cleanStartTime = lesson.startTime.trim()
        val dayOfWeek = lesson.dayOfWeek.value
        val weekType = lesson.weekType ?: "all"
        return "$dateStr#$cleanSubject#$cleanStartTime#$dayOfWeek#$weekType"
    }

    fun getNote(dateStr: String, lesson: Lesson): String? {
        val key = makeNoteKey(dateStr, lesson)
        // Також перевіряємо старий формат ключа на випадок збережених раніше даних
        val note = _notes.value[key]
        if (note != null) return note

        val oldKeyWithId = lesson.id?.let { "$dateStr#$it" }
        if (oldKeyWithId != null && _notes.value.containsKey(oldKeyWithId)) {
            return _notes.value[oldKeyWithId]
        }
        val fallbackOldKey = "$dateStr#${lesson.subject}_${lesson.startTime}_${lesson.dayOfWeek.value}"
        return _notes.value[fallbackOldKey]
    }

    fun saveNote(dateStr: String, lesson: Lesson, noteText: String) {
        val key = makeNoteKey(dateStr, lesson)
        val updated = _notes.value.toMutableMap()
        val trimmed = noteText.trim()
        if (trimmed.isEmpty()) {
            updated.remove(key)
            // Також видаляємо можливі старі формати ключів
            lesson.id?.let { updated.remove("$dateStr#$it") }
            updated.remove("$dateStr#${lesson.subject}_${lesson.startTime}_${lesson.dayOfWeek.value}")
        } else {
            updated[key] = trimmed
        }
        _notes.value = updated
        prefs.edit().putString("notes_json", gson.toJson(updated)).apply()
    }

    fun deleteNote(dateStr: String, lesson: Lesson) {
        saveNote(dateStr, lesson, "")
    }

    fun clearAll() {
        _notes.value = emptyMap()
        prefs.edit().remove("notes_json").apply()
    }
}

