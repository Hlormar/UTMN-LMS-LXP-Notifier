package com.csttine.utmn.lms.lmsnotifier

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import java.util.concurrent.TimeUnit

class LmsApp :Application() {
    fun startPeriodicWork(){
        val autoCheckInterval = SharedDS().get(this, "notifications_autocheck_interval").toLongOrNull() ?: 15
        val workRequest = PeriodicWorkRequestBuilder<WorkRuntime>(autoCheckInterval, TimeUnit.MINUTES).build()
        //val workManager = WorkManager.getInstance(this)
        //workManager.cancelUniqueWork("lms_notification")
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("lms_notification", ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }


    override fun onCreate() {
        super.onCreate()
        startPeriodicWork()
    }
}