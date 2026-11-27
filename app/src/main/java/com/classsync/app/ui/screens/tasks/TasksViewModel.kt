package com.classsync.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.classsync.app.data.local.entity.AssignmentEntity
import com.classsync.app.data.local.entity.StudyMaterialEntity
import com.classsync.app.data.repository.AssignmentRepository
import com.classsync.app.data.repository.StudyMaterialRepository
import com.classsync.app.worker.AssignmentReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class TasksUiState(
    val assignments: List<AssignmentEntity> = emptyList(),
    val materials: List<StudyMaterialEntity> = emptyList(),
    val selectedTab: TaskTab = TaskTab.ASSIGNMENTS,
    val selectedSubjectFilter: String = "All"
)

enum class TaskTab { ASSIGNMENTS, MATERIALS }

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val studyMaterialRepository: StudyMaterialRepository,  // ← fixed: use repo not DAO
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    val subjects = listOf("All", "Data Structures", "Operating Systems", "Algorithms", "DBMS")

    init {
        viewModelScope.launch { seedDemoData() }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            assignmentRepository.getAllAssignments().collectLatest { assignments ->
                _uiState.update { it.copy(assignments = assignments) }
            }
        }
        viewModelScope.launch {
            studyMaterialRepository.getAllMaterials().collectLatest { materials ->
                _uiState.update { it.copy(materials = materials) }
            }
        }
    }

    fun selectTab(tab: TaskTab) = _uiState.update { it.copy(selectedTab = tab) }
    fun selectSubject(s: String) = _uiState.update { it.copy(selectedSubjectFilter = s) }

    fun addAssignment(title: String, subject: String, dueMillis: Long, priority: String = "MEDIUM") {
        viewModelScope.launch {
            val id = assignmentRepository.addAssignment(
                AssignmentEntity(
                    title       = title,
                    subject     = subject,
                    dueDateTime = dueMillis,
                    priority    = priority
                )
            )
            scheduleReminder(id, title, subject, dueMillis)
        }
    }

    fun toggleComplete(id: Long, done: Boolean) {
        viewModelScope.launch { assignmentRepository.toggleComplete(id, done) }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch { assignmentRepository.deleteAssignment(assignment) }
    }

    fun addMaterial(material: StudyMaterialEntity) {
        viewModelScope.launch { studyMaterialRepository.addMaterial(material) }
    }

    fun deleteMaterial(material: StudyMaterialEntity) {
        viewModelScope.launch { studyMaterialRepository.deleteMaterial(material) }
    }

    private fun scheduleReminder(id: Long, title: String, subject: String, dueMillis: Long) {
        val delay = dueMillis - System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3)
        if (delay <= 0) return
        val request = OneTimeWorkRequestBuilder<AssignmentReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    AssignmentReminderWorker.KEY_ASSIGNMENT_ID    to id,
                    AssignmentReminderWorker.KEY_ASSIGNMENT_TITLE to title,
                    AssignmentReminderWorker.KEY_SUBJECT          to subject
                )
            )
            .addTag("reminder_$id")
            .build()
        workManager.enqueue(request)
    }

    private suspend fun seedDemoData() {
        if (assignmentRepository.getAllAssignments().first().isNotEmpty()) return
        val now = System.currentTimeMillis()
        listOf(
            AssignmentEntity(title = "Lab Report – DS",          subject = "Data Structures",   dueDateTime = now + 3 * 3600_000L,  priority = "HIGH"),
            AssignmentEntity(title = "Process Scheduling Notes", subject = "Operating Systems", dueDateTime = now + 24 * 3600_000L, priority = "MEDIUM"),
            AssignmentEntity(title = "Graph Algorithms HW",      subject = "Algorithms",        dueDateTime = now + 48 * 3600_000L, priority = "LOW"),
        ).forEach { assignmentRepository.addAssignment(it) }

        if (studyMaterialRepository.getAllMaterials().first().isNotEmpty()) return
        listOf(
            StudyMaterialEntity(subject = "Data Structures",   title = "Trees & Graphs – Lecture Slides", fileType = "SLIDE", fileSize = 2_400_000),
            StudyMaterialEntity(subject = "Operating Systems", title = "Chapter 4 – Deadlocks.pdf",        fileType = "PDF",   fileSize = 1_800_000),
            StudyMaterialEntity(subject = "Algorithms",        title = "Dynamic Programming Cheatsheet",   fileType = "PDF",   fileSize = 320_000),
        ).forEach { studyMaterialRepository.addMaterial(it) }
    }
}
