package com.csttine.utmn.lms.lmsnotifier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fun isFirstLaunch(): String {
            return runBlocking {
                val preferences = dataStore.data.first()

                // Check if the first launch key exists and is true (default: true if absent)
                val isFirstLaunch = preferences[ stringPreferencesKey("passcode") ] ?: ""
                isFirstLaunch
            }
        }

        if (isFirstLaunch() == ""){
            //setContentView(R.layout.welcome_screen)
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
        else{
            //setContentView(R.layout.lock_screen)
            startActivity(Intent(this, LockScreen::class.java))
        }
        finish()
    }
}
