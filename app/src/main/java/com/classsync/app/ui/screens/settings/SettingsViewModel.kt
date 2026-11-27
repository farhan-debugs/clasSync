package com.classsync.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classsync.app.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String   = "",
    val userEmail: String  = "",
    val className: String  = "Class 10-B",
    val linkedSheetId: String? = null,
    val notificationsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.userName,
                prefs.userEmail,
                prefs.className,
                prefs.linkedSheetId
            ) { name, email, cls, sheetId ->
                SettingsUiState(
                    userName       = name,
                    userEmail      = email,
                    className      = cls,
                    linkedSheetId  = sheetId
                )
            }.collectLatest { _uiState.value = it }
        }
    }

    fun updateClassName(name: String) {
        viewModelScope.launch { prefs.setClassName(name) }
    }

    fun unlinkSheet() {
        viewModelScope.launch { prefs.setLinkedSheetId("") }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            prefs.clearSession()
            onComplete()
        }
    }
}
