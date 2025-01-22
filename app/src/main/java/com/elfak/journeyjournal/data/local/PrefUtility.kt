package com.elfak.journeyjournal.data.local

import android.content.Context
import android.content.SharedPreferences

object PrefUtility {

    private const val PREF_NAME = "app_prefs"
    private const val FIRST_RUN = "first_run"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstRun(context: Context): Boolean {
        return getPreferences(context).getBoolean(FIRST_RUN, true)
    }

    fun saveFirstRun(context: Context, firstRun: Boolean) {
        getPreferences(context).edit().putBoolean(FIRST_RUN, firstRun).apply()
    }
}