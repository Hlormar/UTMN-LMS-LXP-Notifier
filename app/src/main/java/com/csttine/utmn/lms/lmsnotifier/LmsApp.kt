package com.csttine.utmn.lms.lmsnotifier

import android.app.Application
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LmsApp :Application() {
    companion object {
        lateinit var appContext: LmsApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}