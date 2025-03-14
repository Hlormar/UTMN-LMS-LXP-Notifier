package com.csttine.utmn.lms.lmsnotifier
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.fragments.formatTimeStamps
import com.csttine.utmn.lms.lmsnotifier.languageManager.LanguageManager
import com.csttine.utmn.lms.lmsnotifier.parser.parse

class WorkRuntime(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val channelId = "lms_not_id"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channel = NotificationChannel(
            channelId,
            applicationContext.getString(R.string.notification_name),
            NotificationManager.IMPORTANCE_DEFAULT

        ).apply {
            //description = "description"
            setSound(soundUri, null)}

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, LockScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )



        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.utmn_inv1_eng_eps)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    override fun doWork(): Result {
        val isPeriodic = inputData.getBoolean("isPeriodic", true)

        if (SharedDS().get(applicationContext, "passcode") != ""){
            if (isPeriodic){
                if (parse(applicationContext)[9] as Boolean){
                    //notify
                    sendNotification(applicationContext.getString(R.string.notification_title),applicationContext.getString(R.string.notification_msg), -1)
                }
                else{
                    //do nothing or test
                    sendNotification("test","just working ${LanguageManager().getCurrentLangCode(applicationContext)}", -1)
                    sendNotification(applicationContext.getString(R.string.notification_upcoming), SharedDS().getList(applicationContext, "activities")[0] +
                            " at ${formatTimeStamps(SharedDS().getList(applicationContext, "timeStarts")[0], LanguageManager().getCurrentLangCode(applicationContext))}", 0)
                    sendNotification(applicationContext.getString(R.string.notification_title),applicationContext.getString(R.string.notification_msg), -1)

                }
            }

            else {
                val scheduleActivityIndex = inputData.getInt("scheduleActivityIndex", -1)
                sendNotification(applicationContext.getString(R.string.notification_upcoming), SharedDS().getList(applicationContext, "activities")[scheduleActivityIndex] +
                        " at ${formatTimeStamps(SharedDS().getList(applicationContext, "timeStarts")[scheduleActivityIndex], LanguageManager().getCurrentLangCode(applicationContext))}", scheduleActivityIndex)
            }

        }
        return Result.success()
    }
}