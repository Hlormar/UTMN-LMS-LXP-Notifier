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

    private fun getRandomMinutes(start: Int): Int{
        return Random.nextInt(start, 60)
    }


    fun scheduleAutoChecks(){
        try {
            val amountOfChecks = SharedDS().get(this, "autoChecksAmount").toIntOrNull() ?: 3
            if (amountOfChecks > 0){
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val milestones = listOf(8, 16, 23)
                var rndMinute: Int
                for (i in 0..< amountOfChecks){
                    if (currentHour < milestones[i]){
                        Log.d("     scheduleAutoChecks", "${milestones[i]} > $currentHour")
                        rndMinute = getRandomMinutes(0)
                        enqueueAutoCheck(calcDelayUntil(milestones[i],rndMinute))
                    }
                    else if (currentHour == milestones[i]){
                        rndMinute = getRandomMinutes(calendar.get(Calendar.MINUTE))
                        Log.d("     scheduleAutoChecks", "${milestones[i]} == $currentHour")
                        enqueueAutoCheck(calcDelayUntil(milestones[i], rndMinute))

                    }
                }
            } else Log.d("      scheduleAutoChecks", "amount of checks = 0")
        } catch (e: Exception){
            Log.e("     scheduleAutoChecks", "${e.message}", e)
        }
    }


    fun calcDelayUntil(hour: Int, minute: Int): Long {
        Log.d("     calcDelayUntil", "$hour:$minute")

        val calendar = Calendar.getInstance()
        val targetTime = calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // If the current time is past the adjusted target time, schedule it for the next day
        val currentTime = System.currentTimeMillis()
        if (currentTime >= targetTime.timeInMillis) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1) // Add one day
        }

        return targetTime.timeInMillis - currentTime
    }


    private fun enqueueAutoCheck(delayUntilStart: Long){
        val workRequest = OneTimeWorkRequest.Builder(WorkRuntime::class.java)
            .addTag("lms-autoCheck")
            .setInitialDelay(delayUntilStart, TimeUnit.MILLISECONDS)
            //.setInputData(
                //Data.Builder()
                    //.putBoolean("isPeriodic", true)
                    //.putInt("autoChecksAmount", amountOfChecks)
                    //.build()
            //)
            .build()
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(workRequest)
    }
}