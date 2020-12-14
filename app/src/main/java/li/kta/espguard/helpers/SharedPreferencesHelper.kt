package li.kta.espguard.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

object SharedPreferencesHelper {
    private const val PREFERENCES_FILE = "prefs"

    fun getBooleanPreference(context: Context, pref: String): Boolean =
            getSharedPreferences(context)
                    .getBoolean(pref, false)

    fun getSharedPreferencesEditor(context: Context): SharedPreferences.Editor =
            getSharedPreferences(context)
                    .edit()

    fun getSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFERENCES_FILE, AppCompatActivity.MODE_PRIVATE)
}