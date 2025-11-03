package ua.zxcode.digitalschedule.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.zxcode.digitalschedule.model.Lesson

class LessonStore(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val repo = LessonRepository(db.lessonDao())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val lessons: StateFlow<List<Lesson>> =
        repo.getAllLessons().stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun addLesson(lesson: Lesson) {
        scope.launch { repo.insertLesson(lesson) }
    }
    fun updateLesson(lesson: Lesson) {
        scope.launch { repo.updateLesson(lesson) }
    }
    fun deleteLesson(lesson: Lesson) {
        scope.launch { repo.deleteLesson(lesson) }
    }
    fun clearAll() {
        scope.launch { repo.deleteAll() }
    }
    fun addLessons(list: List<Lesson>) {
        scope.launch { repo.insertLessons(list) }
    }
}

