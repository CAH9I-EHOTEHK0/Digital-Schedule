package ua.zxcode.digitalschedule.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.zxcode.digitalschedule.model.Grade

class GradeStore(context: Context) {
    private val prefs = context.getSharedPreferences("digital_schedule_grade_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _grades = MutableStateFlow<Map<String, List<Grade>>>(loadGrades())
    val grades: StateFlow<Map<String, List<Grade>>> = _grades

    private fun loadGrades(): Map<String, List<Grade>> {
        val json = prefs.getString("grades_json", null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, List<Grade>>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveGrades(newGrades: Map<String, List<Grade>>) {
        _grades.value = newGrades
        prefs.edit().putString("grades_json", gson.toJson(newGrades)).apply()
    }

    fun clearAll() {
        _grades.value = emptyMap()
        prefs.edit().remove("grades_json").apply()
    }
}
