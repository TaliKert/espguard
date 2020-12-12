package li.kta.espguard.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_sensor_details.*
import kotlinx.android.synthetic.main.activity_settings.*
import li.kta.espguard.R
import li.kta.espguard.room.LocalSensorDb


class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFERENCES_FILE = "prefs"
        const val PREFERENCES_DARK_THEME = "dark_theme"
        const val PREFERENCES_FIREBASE_TOKEN = "token"
        const val PREFERENCES_QUIET_NOTIFICATIONS = "quiet_notifications"
        const val PREFERENCES_IGNORE_NOTIFICATIONS = "ignore_notifications"

        fun setTheme(context: Context) {
            val preferences =
                context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
            val useDarkTheme = preferences.getBoolean(PREFERENCES_DARK_THEME, false)

            if (useDarkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        loadThemeSwitchValue()
        loadIgnoreSwitchValue()
        loadQuietSwitchValue()


        switch_theme.setOnCheckedChangeListener { _, isChecked ->
            toggleThemeSwitch(isChecked)
        }

        switch_ignore_notifications.setOnCheckedChangeListener { _, isChecked ->
            toggleIgnoreSwitch(isChecked)
        }

        switch_quiet_notifications.setOnCheckedChangeListener { _, isChecked ->
            toggleQuietSwitch(isChecked)
        }

        button_clear_events.setOnClickListener { clearEvents() }

    }

    private fun loadThemeSwitchValue() {
        val preferences =
            getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        switch_theme.isChecked = preferences.getBoolean(PREFERENCES_DARK_THEME, false)
    }

    private fun loadQuietSwitchValue() {
        val preferences =
            getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        switch_theme.isChecked = preferences.getBoolean(PREFERENCES_QUIET_NOTIFICATIONS, false)
    }

    private fun loadIgnoreSwitchValue() {
        val preferences =
            getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        switch_theme.isChecked = preferences.getBoolean(PREFERENCES_IGNORE_NOTIFICATIONS, false)
    }

    private fun toggleQuietSwitch(isChecked: Boolean) {
        val editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit()
        editor.putBoolean(PREFERENCES_QUIET_NOTIFICATIONS, isChecked)
        editor.apply()
    }

    private fun toggleIgnoreSwitch(isChecked: Boolean) {
        val editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit()
        editor.putBoolean(PREFERENCES_IGNORE_NOTIFICATIONS, isChecked)
        editor.apply()
    }

    private fun clearEvents() {
        LocalSensorDb.getInstance(this).getEventDao().nukeTable()
    }


    private fun toggleThemeSwitch(isChecked: Boolean) {
        val editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit()
        editor.putBoolean(PREFERENCES_DARK_THEME, isChecked)
        editor.apply()
        setTheme(this)
    }

}