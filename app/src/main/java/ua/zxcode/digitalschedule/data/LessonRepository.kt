package ua.zxcode.digitalschedule.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.zxcode.digitalschedule.model.Lesson

class LessonRepository(private val dao: LessonDao) {
    fun getAllLessons(): Flow<List<Lesson>> =
        dao.getAll().map { list -> list.map { it.toLesson() } }

    suspend fun insertLesson(lesson: Lesson) =
        dao.insert(LessonEntity.fromLesson(lesson))

    suspend fun insertLessons(lessons: List<Lesson>) =
        dao.insertAll(lessons.map { LessonEntity.fromLesson(it) })

    suspend fun updateLesson(lesson: Lesson) =
        dao.update(LessonEntity.fromLesson(lesson))

    suspend fun deleteLesson(lesson: Lesson) =
        dao.delete(LessonEntity.fromLesson(lesson))

    suspend fun deleteAll() = dao.deleteAll()
}
