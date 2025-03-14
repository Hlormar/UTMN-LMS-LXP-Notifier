package com.csttine.utmn.lms.lmsnotifier

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class LmsApp :Application() {
    override fun onCreate() {
        super.onCreate()
        val workRequest = PeriodicWorkRequestBuilder<WorkRuntime>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("lms_notification", ExistingPeriodicWorkPolicy.KEEP, workRequest)

    }
}