package li.kta.espguard.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import kotlinx.android.synthetic.main.activity_settings.*
import li.kta.espguard.R
import li.kta.espguard.room.LocalSensorDb


class SettingsActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = SettingsActivity::class.java.name

        const val PREFERENCES_FILE = "prefs"
        const val PREFERENCES_DARK_THEME = "dark_theme"
        const val PREFERENCES_FIREBASE_TOKEN = "token"
        const val PREFERENCES_QUIET_NOTIFICATIONS = "quiet_notifications"
        const val PREFERENCES_IGNORE_NOTIFICATIONS = "ignore_notifications"

        fun setTheme(context: Context) {
            val useDarkTheme = getBooleanPreference(context, PREFERENCES_DARK_THEME)
            Log.i(TAG, "Setting theme of $context to dark: $useDarkTheme")

            AppCompatDelegate.setDefaultNightMode(
                    if (useDarkTheme) MODE_NIGHT_YES else MODE_NIGHT_NO
            )
        }

        private fun getBooleanPreference(context: Context, pref: String) =
                getSharedPreferences(context).getBoolean(pref, false)

        fun getSharedPreferences(context: Context): SharedPreferences =
                context.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        loadSwitchValues()
        setupButtons()
    }

    private fun setupButtons() {
        listOf(
                button_switch_theme to ::toggleThemeSwitch,
                switch_ignore_notifications to ::toggleIgnoreSwitch,
                switch_quiet_notifications to ::toggleQuietSwitch,
                button_clear_events to ::clearEvents
        ).forEach { (button, action) -> button.setOnClickListener { action() } }
    }

    private fun loadSwitchValues() {
        switch_quiet_notifications.isChecked = getBooleanPreference(PREFERENCES_QUIET_NOTIFICATIONS)
        switch_ignore_notifications.isChecked =
                getBooleanPreference(PREFERENCES_IGNORE_NOTIFICATIONS)
    }

    private fun toggleQuietSwitch() = invertBooleanPreference(PREFERENCES_QUIET_NOTIFICATIONS)

    private fun toggleIgnoreSwitch() = invertBooleanPreference(PREFERENCES_IGNORE_NOTIFICATIONS)

    private fun clearEvents(): Unit = LocalSensorDb.getEventDao(this).nukeTable()


    private fun toggleThemeSwitch() {
        invertBooleanPreference(PREFERENCES_DARK_THEME)
        recreate()
    }

    private fun invertBooleanPreference(pref: String): Unit =
            getSharedPreferencesEditor().let {
                it.putBoolean(pref, !getBooleanPreference(pref))
                it.apply()
            }

    private fun getBooleanPreference(pref: String) = getBooleanPreference(this, pref)

    private fun getSharedPreferencesEditor() = Companion.getSharedPreferences(this).edit()
}