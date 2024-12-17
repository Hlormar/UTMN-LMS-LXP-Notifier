package com.csttine.utmn.lms.lmsnotifier

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.csttine.utmn.lms.WorkRuntime
import java.util.concurrent.TimeUnit

class LmsApp :Application() {
    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<WorkRuntime>(8, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("lms_notif", ExistingPeriodicWorkPolicy.KEEP, workRequest)

    }
}