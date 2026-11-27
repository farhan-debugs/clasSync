package com.classsync.app.data.repository

import com.classsync.app.data.local.dao.TimetableDao
import com.classsync.app.data.local.entity.TimetableEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val dao: TimetableDao
) {
    fun getScheduleForDay(classId: String, day: Int): Flow<List<TimetableEntity>> =
        dao.getScheduleForDay(classId, day)

    fun getFullSchedule(classId: String): Flow<List<TimetableEntity>> =
        dao.getFullSchedule(classId)

    suspend fun addSlot(slot: TimetableEntity): Long = dao.insertSlot(slot)
    suspend fun importSchedule(slots: List<TimetableEntity>) = dao.insertSlots(slots)
    suspend fun updateSlot(slot: TimetableEntity) = dao.updateSlot(slot)
    suspend fun deleteSlot(slot: TimetableEntity) = dao.deleteSlot(slot)
    suspend fun clearSchedule(classId: String) = dao.clearSchedule(classId)
}
