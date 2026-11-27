package com.classsync.app.data.local.dao

import androidx.room.*
import com.classsync.app.data.local.entity.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetable WHERE classId = :classId AND dayOfWeek = :day ORDER BY startTime ASC")
    fun getScheduleForDay(classId: String, day: Int): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE classId = :classId ORDER BY dayOfWeek, startTime ASC")
    fun getFullSchedule(classId: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimetableEntity>)

    @Update
    suspend fun updateSlot(slot: TimetableEntity)

    @Delete
    suspend fun deleteSlot(slot: TimetableEntity)

    @Query("DELETE FROM timetable WHERE classId = :classId")
    suspend fun clearSchedule(classId: String)
}
