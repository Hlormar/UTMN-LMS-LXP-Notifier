package com.csttine.utmn.lms.lmsnotifier

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS


class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedDS by lazy { SharedDS.getInstance(LmsApp.appContext) }

        if (sharedDS.get("passcode") == ""){
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
