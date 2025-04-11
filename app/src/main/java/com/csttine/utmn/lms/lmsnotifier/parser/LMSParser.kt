package com.csttine.utmn.lms.lmsnotifier.parser

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.csttine.utmn.lms.lmsnotifier.WorkRuntime
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class LMSParser(private val context: Context){

    private val sharedDS by lazy { SharedDS.getInstance(context) }

    fun formatTimeStamps(timestamp: String, locale: String): String {
        // Formats with app locale
        val format = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Locale.forLanguageTag(locale))
        return format.format(Date(timestamp.toLong() * 1000)) // sec to mill sec
    }

    fun formatTimeStampsDuration(timestampStr: String, locale: String): String {
        val timestamp = timestampStr.toLong() * 1000
        val hours = (timestamp / (1000 * 60 * 60)) % 24
        val minutes = (timestamp / (1000 * 60)) % 60
        val seconds = (timestamp / 1000) % 60

        return if (hours > 0) {
            String.format(Locale.forLanguageTag(locale), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.forLanguageTag(locale), "%02d:%02d", minutes, seconds)
        }
    }

    fun enqueueDeadlines(timeStarts: List<String>, hoursBeforeDeadline: Int) {
        for ((index, timestampStr) in timeStarts.withIndex()) {
            var delaySec = timestampStr.toLong() - (System.currentTimeMillis() / 1000)
            if (delaySec > 0) {
                // Try to remind 5 hours before, else remind now
                val calculated = delaySec - 3600 * hoursBeforeDeadline
                if (calculated >= 0) {
                    delaySec = calculated
                }

                val workRequest = OneTimeWorkRequest.Builder(WorkRuntime::class.java)
                    .addTag("lms-deadline")
                    .setInitialDelay(delaySec, TimeUnit.SECONDS)
                    .setInputData(
                        Data.Builder()
                            .putInt("scheduleActivityIndex", index)
                            .build()
                    )
                    .build()
                val workManager = WorkManager.getInstance(context.applicationContext)
                workManager.cancelAllWorkByTag("lms-deadline")
                workManager.enqueue(workRequest)
                Log.d("LMSParser", "Set the deadline alert in $delaySec seconds")
            }
        }
    }


    fun parseOffline(): List<Any> {
        Log.d("LMSParser", "Parsing offline")
        val activities = sharedDS.getList("activities")
        val activityTypes = sharedDS.getList("activityTypes")
        val timeStarts = sharedDS.getList("timeStarts")
        val timeDurations = sharedDS.getList("timeDurations")
        val descriptions = sharedDS.getList("descriptions")
        val coursesNames = sharedDS.getList("coursesNames")
        val urls = sharedDS.getList("URLs")
        val accessTime = sharedDS.get("accessTime")
        val source: Byte = if (accessTime.isNotEmpty()) 1 else -1
        val areThereNewTasks = false

        return listOf(accessTime, activities, activityTypes, timeStarts, descriptions, coursesNames, urls, timeDurations, source, areThereNewTasks)
    }


    fun parse(): List<Any> {
        Log.d("LMSParser", "Parsing online")
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
        }
        val py = Python.getInstance()
        val pyModule = py.getModule("parser")

        // Token chores
        var token = sharedDS.get("token")
        if (token.isEmpty() || token == "-1") {
            val email = sharedDS.get("email")
            val password = sharedDS.get("password")
            try {
                token = pyModule.callAttr("getToken", email, password).toString()
                sharedDS.writeStr("token", token)
            } catch (e: Exception) {
                Log.e("LMSParser", "Failed to get token", e)
                return parseOffline()
            }
        }

        var source: Byte = -1  // -1 = error; 0 = new; 1 = old
        var jsonDict = PyObject.fromJava("-1")
        if (token != "-1") {
            try {
                jsonDict = pyModule.callAttr("formatDict", pyModule.callAttr("getCalendar", token))
            } catch (e: Exception) {
                Log.e("LMSParser", "Failed to format dictionary", e)
                return parseOffline()
            }
        }

        return if (jsonDict.toString() != "-1") {
            source = 0
            val activities: MutableList<String> = mutableListOf()
            val activityTypes: MutableList<String> = mutableListOf()
            val timeStarts: MutableList<String> = mutableListOf()
            val timeDurations: MutableList<String> = mutableListOf()
            val descriptions: MutableList<String> = mutableListOf()
            val coursesNames: MutableList<String> = mutableListOf()
            val urls: MutableList<String> = mutableListOf()
            var areThereNewTasks = false
            val events = jsonDict.asMap()[PyObject.fromJava("events")]?.asList() ?: emptyList()
            val accessTime = jsonDict.asMap()[PyObject.fromJava("date")]?.asMap()?.get(PyObject.fromJava("timestamp")).toString()

            for (event in events) {
                val eventMap = event.asMap()
                activities.add(eventMap[PyObject.fromJava("activityname")]?.toString() ?: "")
                activityTypes.add(eventMap[PyObject.fromJava("activitystr")]?.toString() ?: "")
                timeStarts.add(eventMap[PyObject.fromJava("timestart")]?.toString() ?: "")
                timeDurations.add(eventMap[PyObject.fromJava("timeduration")]?.toString() ?: "")
                descriptions.add(eventMap[PyObject.fromJava("description")]?.toString() ?: "")
                urls.add(eventMap[PyObject.fromJava("viewurl")]?.toString() ?: "")
                coursesNames.add(eventMap[PyObject.fromJava("course")]?.asMap()?.get(PyObject.fromJava("fullname"))?.toString() ?: "")
            }

            val knownActivities = sharedDS.getList("activities")
            val knownTimestarts = sharedDS.getList("timeStarts")

            // Re-enqueue if there are new tasks
            if (!knownActivities.containsAll(activities) || !knownTimestarts.containsAll(timeStarts)) {
                sharedDS.clearStr("isTranslated")
                areThereNewTasks = true
                enqueueDeadlines(timeStarts, sharedDS.get("hoursBeforeDeadline").toIntOrNull() ?: 5)
            } else if (!activities.containsAll(knownActivities) || !timeStarts.containsAll(knownTimestarts)) {
                // When some tasks passed away
                val amountAssignsPassed = knownActivities.size - activities.size
                Log.d("LMSParser", "Amount of assignments has reduced by $amountAssignsPassed")
                Log.d("LMSParser", "Current Activities: $activities\nKnown Activities: $knownActivities")

                if (sharedDS.get("isTranslated") == "1") {
                    val translatedDescr = sharedDS.getList("translated_descriptions")
                    val translatedActs = sharedDS.getList("translated_activities")
                    val translatedCourses = sharedDS.getList("translated_coursesNames")

                    val currentLastIndex = translatedActs.size - amountAssignsPassed
                    sharedDS.writeList("translated_activities", translatedActs.subList(amountAssignsPassed, currentLastIndex))
                    sharedDS.writeList("translated_descriptions", translatedDescr.subList(amountAssignsPassed, currentLastIndex))
                    sharedDS.writeList("translated_coursesNames", translatedCourses.subList(amountAssignsPassed, currentLastIndex))
                }
            }

            sharedDS.writeStr("accessTime", accessTime)
            sharedDS.writeList("activities", activities)
            sharedDS.writeList("activityTypes", activityTypes)
            sharedDS.writeList("timeStarts", timeStarts)
            sharedDS.writeList("timeDurations", timeDurations)
            sharedDS.writeList("descriptions", descriptions)
            sharedDS.writeList("coursesNames", coursesNames)
            sharedDS.writeList("URLs", urls)

            listOf(accessTime, activities, activityTypes, timeStarts, descriptions, coursesNames, urls, timeDurations, source, areThereNewTasks)
        } else {
            // Return offline with outdated status
            parseOffline()
        }
    }
}
