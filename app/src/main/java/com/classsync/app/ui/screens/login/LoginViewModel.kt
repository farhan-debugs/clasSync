package com.classsync.app.ui.screens.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classsync.app.R
import com.classsync.app.data.auth.GoogleAuthManager
import com.classsync.app.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle    : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val displayName: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: GoogleAuthManager,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /**
     * Triggers Google Sign-In via Credential Manager.
     * [activity] must be the current Activity context (required by Credential Manager).
     */
    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            val webClientId = activity.getString(R.string.default_web_client_id)
            authManager.signIn(activity, webClientId)
                .onSuccess { result ->
                    prefs.setUser(result.displayName, result.email)
                    prefs.setAuthToken(result.idToken)
                    _state.value = LoginUiState.Success(result.displayName)
                }
                .onFailure { e ->
                    _state.value = LoginUiState.Error(
                        e.message ?: "Sign-in failed. Please try again."
                    )
                }
        }
    }

    fun clearError() { _state.value = LoginUiState.Idle }
}
