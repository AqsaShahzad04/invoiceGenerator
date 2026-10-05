package com.learner.invoicegenerator.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _activeWorkspaceId = MutableStateFlow(prefs.getInt(KEY_ACTIVE_WORKSPACE_ID, -1))
    private val _darkModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE_ENABLED, false))

    val activeWorkspaceId: StateFlow<Int> = _activeWorkspaceId
    val darkModeEnabledFlow: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()


    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACTIVE_WORKSPACE_ID = "active_workspace_id"

        private const val KEY_SELECTED_CURRENCY="selected_currency"
        private const val KEY_CURRENCY_SELECTED_BY_USER="Currency_selected_by_user"

        private const val KEY_INITIAL_STEPS_COMPLETED="initial_steps_completed"
        private const val KEY_DARK_MODE_ENABLED="dark_mode_enabled"
        private const val KEY_HOME_HEADER_STYLE="home_header_style"

        private const val KEY_WELCOME_NOTIFICATION_SENT = "welcome_notification_sent"
        private const val KEY_AUTO_REMINDERS_ENABLED = "auto_reminders_enabled"
        private const val KEY_REMINDER_DAYS = "reminder_days"
        private const val KEY_LAST_NEW_MONTH_NOTIFICATION = "last_new_month_notification"
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

    private val _currencyCode=MutableStateFlow(
        prefs.getString(KEY_SELECTED_CURRENCY,"USD")?:"USD"
    )
    val currencyCode: StateFlow<String> =_currencyCode.asStateFlow()

    fun hasCurrencySelectedByUser(): Boolean{
        return prefs.getBoolean(KEY_CURRENCY_SELECTED_BY_USER,false)
    }
    fun setCurrency(currencyCode:String){
        prefs.edit().putString(KEY_SELECTED_CURRENCY,currencyCode)
            .putBoolean(KEY_CURRENCY_SELECTED_BY_USER,true)
            .apply()

        _currencyCode.value=currencyCode

    }

    fun getCurrencyCode():String?=prefs.getString(KEY_SELECTED_CURRENCY,"USD")

    fun getActiveWorkspaceId(): Int = prefs.getInt(KEY_ACTIVE_WORKSPACE_ID, -1)

    fun isInitialSetupCompleted(): Boolean {
        return prefs.getBoolean(KEY_INITIAL_STEPS_COMPLETED, false)
    }

    fun setInitialStepsCompleted() {
        prefs.edit().putBoolean(KEY_INITIAL_STEPS_COMPLETED, true).apply()
    }

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
        _darkModeEnabled.value = enabled
    }

    fun getHomeHeaderStyle(): String = prefs.getString(KEY_HOME_HEADER_STYLE, "Compact") ?: "Compact"

    fun setHomeHeaderStyle(style: String) {
        prefs.edit().putString(KEY_HOME_HEADER_STYLE, style).apply()
    }

    fun isWelcomeNotificationSent(): Boolean = prefs.getBoolean(KEY_WELCOME_NOTIFICATION_SENT, false)

    fun setWelcomeNotificationSent() {
        prefs.edit().putBoolean(KEY_WELCOME_NOTIFICATION_SENT, true).apply()
    }

    fun isAutoRemindersEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_REMINDERS_ENABLED, true)

    fun setAutoRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_REMINDERS_ENABLED, enabled).apply()
    }

    fun getReminderDays(): Int = prefs.getInt(KEY_REMINDER_DAYS, 3)

    fun setReminderDays(days: Int) {
        prefs.edit().putInt(KEY_REMINDER_DAYS, days).apply()
    }

    fun getLastNewMonthNotification(): String = prefs.getString(KEY_LAST_NEW_MONTH_NOTIFICATION, "") ?: ""

    fun setLastNewMonthNotification(yearMonth: String) {
        prefs.edit().putString(KEY_LAST_NEW_MONTH_NOTIFICATION, yearMonth).apply()
    }

    fun logout() {
       prefs.edit()
           .remove(KEY_USER_ID)
           .remove(KEY_IS_LOGGED_IN)
           .remove(KEY_ACTIVE_WORKSPACE_ID)
        _activeWorkspaceId.value = -1
    }

    fun clearSessionData() {
        prefs.edit().clear().apply()
        _activeWorkspaceId.value = -1
        _darkModeEnabled.value = false
    }
}