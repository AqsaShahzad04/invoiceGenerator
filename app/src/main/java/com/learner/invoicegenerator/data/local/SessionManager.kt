package com.learner.invoicegenerator.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _activeWorkspaceId = MutableStateFlow(prefs.getInt(KEY_ACTIVE_WORKSPACE_ID, 1))
    val activeWorkspaceId: StateFlow<Int> = _activeWorkspaceId

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACTIVE_WORKSPACE_ID = "active_workspace_id"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun createLoginSession(userId: Int) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun setActiveWorkspace(workspaceId: Int) {
        prefs.edit().putInt(KEY_ACTIVE_WORKSPACE_ID, workspaceId).apply()
        _activeWorkspaceId.value = workspaceId
    }

    fun getActiveWorkspaceId(): Int = prefs.getInt(KEY_ACTIVE_WORKSPACE_ID, 1)

    fun logout() {
        prefs.edit().clear().apply()
        _activeWorkspaceId.value = 1
    }
}