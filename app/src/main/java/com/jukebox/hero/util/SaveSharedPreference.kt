package com.jukebox.hero.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SaveSharedPreference {
    companion object {
        private fun getPreference(context: Context): SharedPreferences{
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun setLoggedIn(context: Context, loggedIn : Boolean, userId : Int){
            var editor : SharedPreferences.Editor = getPreference(context).edit()
            editor.putBoolean(PreferencesUtil.LOGGED_IN_PREF, loggedIn)
            editor.putInt(PreferencesUtil.USER_ID_PREF, userId)
            editor.apply()
        }

        fun getLoggedStatus(context: Context) : Boolean{
            return getPreference(context).getBoolean(PreferencesUtil.LOGGED_IN_PREF, false)
        }

        fun getLoggedInUserId(context: Context) : Int {
            return getPreference(context).getInt(PreferencesUtil.USER_ID_PREF, 0)
        }
    }
}