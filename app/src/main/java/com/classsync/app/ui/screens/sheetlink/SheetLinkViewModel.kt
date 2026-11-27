package com.classsync.app.ui.screens.sheetlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classsync.app.data.local.AppPreferences
import com.classsync.app.data.remote.SheetsApiService
import com.classsync.app.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SheetLinkUiState(
    val spreadsheetUrl: String = "",
    val sheetTitle: String     = "",
    val studentCount: Int      = 0,
    val step: SheetLinkStep    = SheetLinkStep.INPUT,
    val error: String?         = null
)

enum class SheetLinkStep { INPUT, VALIDATING, IMPORTING, DONE, ERROR }

@HiltViewModel
class SheetLinkViewModel @Inject constructor(
    private val sheetsApiService: SheetsApiService,
    private val attendanceRepository: AttendanceRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SheetLinkUiState())
    val uiState: StateFlow<SheetLinkUiState> = _uiState.asStateFlow()

    fun onUrlChange(url: String) = _uiState.update { it.copy(spreadsheetUrl = url, error = null) }

    fun linkAndImport() {
        val url   = _uiState.value.spreadsheetUrl.trim()
        val sheetId = extractSheetId(url)
        if (sheetId == null) {
            _uiState.update { it.copy(error = "Invalid Google Sheets URL. Please paste the full URL.", step = SheetLinkStep.ERROR) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(step = SheetLinkStep.VALIDATING, error = null) }
            val email = prefs.userEmail.first()

            // 1. Validate spreadsheet access
            sheetsApiService.validateSpreadsheet(sheetId, email)
                .onFailure { e ->
                    _uiState.update { it.copy(step = SheetLinkStep.ERROR, error = "Cannot access sheet: ${e.message}") }
                    return@launch
                }
                .onSuccess { title ->
                    _uiState.update { it.copy(sheetTitle = title, step = SheetLinkStep.IMPORTING) }
                }

            // 2. Import student roster
            val classId = prefs.classId.first()
            sheetsApiService.fetchRoster(sheetId, email, classId)
                .onFailure { e ->
                    _uiState.update { it.copy(step = SheetLinkStep.ERROR, error = "Could not read Roster tab: ${e.message}") }
                    return@launch
                }
                .onSuccess { students ->
                    attendanceRepository.importStudents(students)
                    prefs.setLinkedSheetId(sheetId)
                    _uiState.update {
                        it.copy(
                            studentCount = students.size,
                            step         = SheetLinkStep.DONE
                        )
                    }
                }
        }
    }

    fun reset() = _uiState.update { SheetLinkUiState() }

    // Extract the spreadsheet ID from various Google Sheets URL formats
    private fun extractSheetId(url: String): String? {
        val regex = Regex("/spreadsheets/d/([a-zA-Z0-9_-]+)")
        return regex.find(url)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
    }
}
