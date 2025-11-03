package ua.zxcode.digitalschedule.manager

import ua.zxcode.digitalschedule.model.LessonTime

class LessonTimeManager(var lessonTimes: MutableList<LessonTime>) {
    fun updateLessonTime(index: Int, start: String, end: String) {
        if (index in lessonTimes.indices) {
            lessonTimes[index] = lessonTimes[index].copy(start = start, end = end)
        }
    }
    fun getLessonTime(index: Int): LessonTime? = lessonTimes.getOrNull(index)
    fun setLessonCount(count: Int) {
        if (count > lessonTimes.size) {
            for (i in lessonTimes.size until count) {
                lessonTimes.add(LessonTime.default(i))
            }
        } else if (count < lessonTimes.size) {
            lessonTimes = lessonTimes.take(count).toMutableList()
        }
    }
    fun allLessonTimes(): List<LessonTime> = lessonTimes

    fun updateTimes(newTimes: MutableList<ua.zxcode.digitalschedule.model.LessonTime>) {
        lessonTimes.clear()
        lessonTimes.addAll(newTimes)
    }
}
