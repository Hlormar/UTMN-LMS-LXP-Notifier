package com.csttine.utmn.lms.lmsnotifier.languageManager

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import java.util.Locale


class LanguageManager {
     fun updateLanguage(context: Context, languageCode: String): Context{
         val locale = Locale(languageCode, languageCode)
        Locale.setDefault(locale)

         // save
         SharedDS().writeStr(context, "locale", languageCode)

         //new cfg
         val config = Configuration(context.resources.configuration)
         config.setLocale(locale)

         //return the updated context
         return context.createConfigurationContext(config)
    }

    fun getCurrentLangCode(context: Context): String{
        val locale = SharedDS().get(context, "locale")
        Log.d("     getCurrentLocale", locale)
        return if (locale != "") locale
        else {
            val localeFallback = Locale.getDefault().language
            Log.d("     getCurrentLocale", "fallback to default $localeFallback")
            localeFallback
        }
    }
}