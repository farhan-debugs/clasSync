package com.classsync.app.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.local.entity.StudentEntity
import com.classsync.app.data.repository.AttendanceRepository
import com.classsync.app.worker.SheetsSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AttendanceUiState(
    val className: String = "Class 10-B",
    val students: List<StudentEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedSubject: String = "All",
    val syncState: SyncState = SyncState.SYNCED,
    val todayDate: String = LocalDate.now().toString()
) {
    val currentStudent: StudentEntity? get() = students.getOrNull(currentIndex)
}

enum class SyncState { SYNCING, SYNCED, OFFLINE_QUEUED }

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val prefs: AppPreferences,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private val _percentages = MutableStateFlow<Map<Long, Float>>(emptyMap())
    val percentages: StateFlow<Map<Long, Float>> = _percentages.asStateFlow()

    val subjects = listOf("All", "Data Structures", "Operating Systems", "Algorithms", "DBMS")

    init {
        loadClassName()
        loadStudents()
        viewModelScope.launch { seedDemoData() }
    }

    private fun loadClassName() {
        viewModelScope.launch {
            prefs.className.collectLatest { name ->
                _uiState.update { it.copy(className = name) }
            }
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            prefs.classId.collectLatest { classId ->
                repository.getStudents(classId).collectLatest { students ->
                    _uiState.update { it.copy(students = students) }
                    refreshPercentages(students)
                }
            }
        }
    }

    private suspend fun refreshPercentages(students: List<StudentEntity>) {
        val subject = _uiState.value.selectedSubject
        val map = students.associate { s ->
            s.id to if (subject == "All") {
                // Compute overall average across all subjects
                val pcts = subjects.drop(1).map { subj ->
                    repository.getAttendancePercent(s.id, subj)
                }
                if (pcts.any { it > 0 }) pcts.filter { it > 0 }.average().toFloat()
                else 88f
            } else {
                val real = repository.getAttendancePercent(s.id, subject)
                if (real > 0) real else 88f
            }
        }
        _percentages.update { map }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
        viewModelScope.launch { refreshPercentages(_uiState.value.students) }
    }

    fun nextStudent()     { _uiState.update { it.copy(currentIndex = (it.currentIndex + 1).coerceAtMost(it.students.lastIndex.coerceAtLeast(0))) } }
    fun previousStudent() { _uiState.update { it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0)) } }
    fun selectStudent(index: Int) { _uiState.update { it.copy(currentIndex = index) } }

    fun markAttendance(status: String) {
        val state   = _uiState.value
        val student = state.currentStudent ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = SyncState.SYNCING) }
            repository.markAttendance(
                studentId = student.id,
                subject   = if (state.selectedSubject == "All") subjects[1] else state.selectedSubject,
                date      = state.todayDate,
                status    = status
            )
            // Trigger WorkManager to sync to Sheets when network is available
            workManager.enqueue(SheetsSyncWorker.buildRequest())
            _uiState.update { it.copy(syncState = SyncState.OFFLINE_QUEUED) }
            nextStudent()
        }
    }

    /** Called by WorkManager observer to update sync badge */
    fun onSyncSuccess() = _uiState.update { it.copy(syncState = SyncState.SYNCED) }
    fun onSyncFailed()  = _uiState.update { it.copy(syncState = SyncState.OFFLINE_QUEUED) }

    /** Mark all students present for the current subject/date */
    fun markAllPresent() {
        val state = _uiState.value
        val subject = if (state.selectedSubject == "All") subjects[1] else state.selectedSubject
        viewModelScope.launch {
            _uiState.update { it.copy(syncState = SyncState.SYNCING) }
            state.students.forEach { student ->
                repository.markAttendance(student.id, subject, state.todayDate, "P")
            }
            workManager.enqueue(SheetsSyncWorker.buildRequest())
            _uiState.update { it.copy(syncState = SyncState.OFFLINE_QUEUED) }
        }
    }

    private suspend fun seedDemoData() {
        val classId = prefs.classId.first()
        repository.getStudents(classId).first().ifEmpty {
            val demoStudents = listOf(
                StudentEntity(rollNumber = "01", name = "Aisha Kapoor",      classId = classId),
                StudentEntity(rollNumber = "02", name = "Rahul Verma",       classId = classId),
                StudentEntity(rollNumber = "03", name = "Karina Harrington", classId = classId),
                StudentEntity(rollNumber = "04", name = "Dev Sharma",        classId = classId),
                StudentEntity(rollNumber = "05", name = "Priya Nair",        classId = classId),
                StudentEntity(rollNumber = "06", name = "Arjun Mehta",       classId = classId),
            )
            repository.importStudents(demoStudents)
        }
    }
}
