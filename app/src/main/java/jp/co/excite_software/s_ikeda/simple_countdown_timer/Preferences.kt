package jp.co.excite_software.s_ikeda.simple_countdown_timer

import android.content.Context
import android.content.SharedPreferences


class Preferences(private val context: Context) {

    companion object {
        private const val TIME = "time"
    }

    private val preferences: SharedPreferences
        get() = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    fun setTime(minutes: Int, seconds: Int) {
        preferences.edit().apply {
            putInt(TIME, minutes * 60 + seconds)
            apply()
        }
    }

    fun getTime(): Int {
        return preferences.getInt(TIME, 3 * 60)
    }
}
