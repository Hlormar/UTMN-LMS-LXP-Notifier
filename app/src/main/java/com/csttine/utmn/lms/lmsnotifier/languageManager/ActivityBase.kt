package com.csttine.utmn.lms.lmsnotifier.languageManager

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

abstract class ActivityBase : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase))
    }

    private fun updateLocale(context: Context): Context {
        val savedLanguage = LanguageManager(context).getCurrentLangCode()
        return LanguageManager(context).updateLanguage(savedLanguage)
    }
}