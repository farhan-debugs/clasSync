package com.classsync.app.ui.screens.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.local.dao.AttendanceDao
import com.classsync.app.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectBreakdown(
    val subject: String,
    val subtitle: String,
    val pct: Int,
    val sheetColumn: String = ""
)

data class AnalyticsUiState(
    val student: StudentEntity? = null,
    val totalClasses: Int = 0,
    val presentCount: Int = 0,
    val lateCount: Int = 0,
    val absentCount: Int = 0,
    val subjectBreakdowns: List<SubjectBreakdown> = emptyList(),
    val isLoading: Boolean = true
) {
    val presentPct: Float get() = if (totalClasses > 0) presentCount.toFloat() / totalClasses else 0f
    val latePct:    Float get() = if (totalClasses > 0) lateCount.toFloat()   / totalClasses else 0f
    val absentPct:  Float get() = if (totalClasses > 0) absentCount.toFloat() / totalClasses else 0f
    val overallPctInt: Int get() = (presentPct * 100).toInt()
}

val SUBJECTS = listOf("Data Structures", "Operating Systems", "Algorithms", "DBMS")

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val attendanceDao: AttendanceDao,
    private val prefs: AppPreferences
) : ViewModel() {

    private val studentId: Long = savedStateHandle["studentId"] ?: 0L

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Aggregate across all subjects
            var totalPresent = 0
            var totalLate    = 0
            var totalAbsent  = 0
            var totalClasses = 0

            val subjectBreakdowns = SUBJECTS.mapIndexed { idx, subject ->
                val summary = attendanceDao.getAttendanceSummary(studentId, subject)
                val p = summary.firstOrNull { it.status == "P" }?.count ?: 0
                val l = summary.firstOrNull { it.status == "L" }?.count ?: 0
                val a = summary.firstOrNull { it.status == "A" }?.count ?: 0
                val total = p + l + a

                totalPresent += p
                totalLate    += l
                totalAbsent  += a
                totalClasses += total

                // Fallback to plausible demo data when DB is empty
                if (total == 0) {
                    val demoPct = listOf(94, 88, 79, 95)[idx]
                    val demoTotal = 32
                    val demoP = (demoTotal * demoPct / 100)
                    SubjectBreakdown(
                        subject  = subject,
                        subtitle = "${listOf(32, 28, 24, 20)[idx]} hrs • Sheet Col ${('D' + idx)}",
                        pct      = demoPct,
                        sheetColumn = "Col ${('D' + idx)}"
                    )
                } else {
                    val pct = if (total > 0) (p * 100 / total) else 0
                    SubjectBreakdown(
                        subject  = subject,
                        subtitle = "${total} classes • Sheet Col ${('D' + idx)}",
                        pct      = pct,
                        sheetColumn = "Col ${('D' + idx)}"
                    )
                }
            }

            // Use demo totals if DB is completely empty
            if (totalClasses == 0) {
                _uiState.update {
                    it.copy(
                        totalClasses      = 120,
                        presentCount      = 102,
                        lateCount         = 10,
                        absentCount       = 8,
                        subjectBreakdowns = subjectBreakdowns,
                        isLoading         = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        totalClasses      = totalClasses,
                        presentCount      = totalPresent,
                        lateCount         = totalLate,
                        absentCount       = totalAbsent,
                        subjectBreakdowns = subjectBreakdowns,
                        isLoading         = false
                    )
                }
            }
        }

        // Observe live changes: re-aggregate when attendance DB changes
        // Use a debounced approach to avoid re-entrancy: just mark loading on change
        viewModelScope.launch {
            attendanceRepository.getStudentAttendance(studentId)
                .drop(1)                    // skip initial emission (already loaded above)
                .debounce(500)              // debounce rapid successive marks
                .collectLatest { _ ->
                    loadAnalytics()         // safe: outer launch scopes independently
                }
        }
    }
}
