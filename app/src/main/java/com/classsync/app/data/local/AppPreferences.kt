package com.classsync.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "classsync_prefs")

@Singleton
class AppPreferences @Inject constructor(private val context: Context) {

    companion object {
        val KEY_SHEET_ID       = stringPreferencesKey("linked_sheet_id")
        val KEY_CLASS_ID       = stringPreferencesKey("class_id")
        val KEY_CLASS_NAME     = stringPreferencesKey("class_name")
        val KEY_USER_NAME      = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL     = stringPreferencesKey("user_email")
        val KEY_AUTH_TOKEN     = stringPreferencesKey("auth_token")
        val KEY_DARK_MODE      = booleanPreferencesKey("dark_mode")
        val KEY_NOTIF_ENABLED  = booleanPreferencesKey("notifications_enabled")
    }

    val linkedSheetId: Flow<String?> = context.dataStore.data.map {
        it[KEY_SHEET_ID]?.takeIf { id -> id.isNotBlank() }
    }
    val classId: Flow<String>        = context.dataStore.data.map { it[KEY_CLASS_ID] ?: "default" }
    val className: Flow<String>      = context.dataStore.data.map { it[KEY_CLASS_NAME] ?: "Class 10-B" }
    val userName: Flow<String>       = context.dataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val userEmail: Flow<String>      = context.dataStore.data.map { it[KEY_USER_EMAIL] ?: "" }

    suspend fun setLinkedSheetId(id: String) {
        if (id.isBlank()) {
            context.dataStore.edit { it.remove(KEY_SHEET_ID) }
        } else {
            context.dataStore.edit { it[KEY_SHEET_ID] = id }
        }
    }
    suspend fun setClassName(name: String) {
        context.dataStore.edit { it[KEY_CLASS_NAME] = name }
    }
    suspend fun setUser(name: String, email: String) {
        context.dataStore.edit {
            it[KEY_USER_NAME]  = name
            it[KEY_USER_EMAIL] = email
        }
    }
    suspend fun setAuthToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }
    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(KEY_AUTH_TOKEN)
            it.remove(KEY_USER_NAME)
            it.remove(KEY_USER_EMAIL)
        }
    }
}
