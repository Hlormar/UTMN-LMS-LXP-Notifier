package com.csttine.utmn.lms.lmsnotifier.languageManager

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.csttine.utmn.lms.lmsnotifier.LmsApp
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import java.util.Locale


class LanguageManager(private val context: Context) {

    private val sharedDS by lazy { SharedDS.getInstance(LmsApp.appContext) }

     fun updateLanguage(languageCode: String): Context{
         val locale = Locale(languageCode, languageCode)
        Locale.setDefault(locale)

         // save
         sharedDS.writeStr("locale", languageCode)

         //new cfg
         val config = Configuration(context.resources.configuration)
         config.setLocale(locale)

         //return the updated context
         return context.createConfigurationContext(config)
    }

    fun getCurrentLangCode(): String{
        val locale = sharedDS.get("locale")
        Log.d("     getCurrentLocale", locale)
        return if (locale != "") locale
        else {
            val localeFallback = Locale.getDefault().language
            Log.d("     getCurrentLocale", "fallback to default $localeFallback")
            localeFallback
        }
    }
}