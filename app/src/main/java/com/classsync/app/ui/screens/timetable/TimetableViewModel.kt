package com.classsync.app.ui.screens.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.local.entity.TimetableEntity
import com.classsync.app.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TimetableUiState(
    val selectedDay: Int = run {
        val dow = LocalDate.now().dayOfWeek.value
        if (dow == 7) 1 else dow  // Sunday → Monday
    },
    val slots: List<TimetableEntity> = emptyList()
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: TimetableRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    // Reactive: whenever selectedDay changes, re-fetch the schedule
    private val _classId = MutableStateFlow("default")

    init {
        viewModelScope.launch {
            prefs.classId.collect { id -> _classId.value = id }
        }
        // React to both classId and selectedDay changes
        viewModelScope.launch {
            _classId.combine(_uiState.map { it.selectedDay }.distinctUntilChanged()) { classId, day ->
                classId to day
            }.collectLatest { (classId, day) ->
                repository.getScheduleForDay(classId, day).collectLatest { slots ->
                    _uiState.update { it.copy(slots = slots) }
                }
            }
        }
        viewModelScope.launch { seedDemoSchedule() }
    }

    fun selectDay(day: Int) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun addSlot(slot: TimetableEntity) {
        viewModelScope.launch {
            repository.addSlot(slot.copy(classId = _classId.value))
        }
    }

    fun updateSlot(slot: TimetableEntity) {
        viewModelScope.launch { repository.updateSlot(slot) }
    }

    fun deleteSlot(slot: TimetableEntity) {
        viewModelScope.launch { repository.deleteSlot(slot) }
    }

    private suspend fun seedDemoSchedule() {
        val classId = _classId.value
        if (repository.getFullSchedule(classId).first().isNotEmpty()) return
        val demoSlots = listOf(
            TimetableEntity(dayOfWeek = 1, startTime = "09:30", endTime = "10:30", subject = "Data Structures",   room = "Room 201", professor = "Dr. Reddy",   classId = classId),
            TimetableEntity(dayOfWeek = 1, startTime = "11:00", endTime = "12:00", subject = "Operating Systems", room = "Room 302", professor = "Dr. Sharma",  classId = classId),
            TimetableEntity(dayOfWeek = 1, startTime = "14:00", endTime = "15:00", subject = "Algorithms",        room = "Lab 104",  professor = "Prof. Mehta", classId = classId),
            TimetableEntity(dayOfWeek = 2, startTime = "09:00", endTime = "10:00", subject = "DBMS",              room = "Room 105", professor = "Dr. Nair",    classId = classId),
            TimetableEntity(dayOfWeek = 2, startTime = "10:30", endTime = "11:30", subject = "Data Structures",   room = "Room 201", professor = "Dr. Reddy",   classId = classId),
            TimetableEntity(dayOfWeek = 3, startTime = "09:30", endTime = "10:30", subject = "Algorithms",        room = "Lab 104",  professor = "Prof. Mehta", classId = classId),
            TimetableEntity(dayOfWeek = 4, startTime = "11:00", endTime = "12:00", subject = "Operating Systems", room = "Room 302", professor = "Dr. Sharma",  classId = classId),
            TimetableEntity(dayOfWeek = 5, startTime = "09:00", endTime = "10:00", subject = "DBMS",              room = "Room 105", professor = "Dr. Nair",    classId = classId),
        )
        repository.importSchedule(demoSlots)
    }
}
