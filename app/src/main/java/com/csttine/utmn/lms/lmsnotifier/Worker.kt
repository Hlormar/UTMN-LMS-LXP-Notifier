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

class WorkRuntime(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    //val dS = appContext.dataStore

    private fun sendNotification(title: String, message: String) {
        val channelId = "lms_not_id"
        val notificationId = 1
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val channel = NotificationChannel(
            channelId,
            "LMS & LXP tasks",
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
            .setSmallIcon(R.drawable.tyumgu_transformed)
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
        if (SharedDS.get(applicationContext, "passcode") != ""){
            val current = (ParsingChores.parse(applicationContext)[1] as List<String>).toSet()
            val existing = (SharedDS.getList(applicationContext, "activities")).toSet()

            if (!existing.containsAll(current)){
                //notify
                sendNotification("You got a new task","Please check the application for more details")
            }
            //else{
                //do nothin or test
                //sendNotification("test","nothing")
            //}
        }
        return Result.success()
    }
}