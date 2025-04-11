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

    fun formatTimeStamps(timestamp: String, locale: String) :String {
        //formats with app locale
        val format = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Locale(locale, locale))
        return format.format(Date(timestamp.toLong() * 1000)) //sec to mill sec
    }

    fun formatTimeStampsDuration(timestampStr: String, locale: String) :String{
        val timestamp = timestampStr.toLong() * 1000
        val hours = (timestamp / (1000 * 60 * 60)) % 24
        val minutes = (timestamp / (1000 * 60)) % 60
        val seconds = (timestamp / 1000) % 60

        return if (hours > 0) {
            String.format(Locale(locale, locale), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale(locale, locale),"%02d:%02d", minutes, seconds)
        }
    }

    fun enqueueDeadlines(timestarts: MutableList<String>, hoursBeforeDeadline: Int){
        for (i in timestarts.indices) {
            var delaySex = timestarts[i].toLong() - (System.currentTimeMillis() / 1000)
            if (delaySex > 0) {

                //try to remind 5 hrs before, else remind now
                val calculated = delaySex - 3600 * hoursBeforeDeadline
                if (calculated >= 0) {
                    delaySex = calculated
                }

                val workRequest = OneTimeWorkRequest.Builder(WorkRuntime::class.java)
                    .addTag("lms-deadline")
                    .setInitialDelay(delaySex, TimeUnit.SECONDS)
                    .setInputData(
                        Data.Builder()
                            //.putBoolean("isAutoCheck", false)
                            .putInt("scheduleActivityIndex", i)
                            .build()
                    )
                    .build()
                val workManager = WorkManager.getInstance(context)
                workManager.cancelAllWorkByTag("lms-deadline")
                workManager.enqueue(workRequest)
                Log.d("     Parser", "set the deadline alert in $delaySex seconds")

            }
        }
    }


    fun parseOffline() :List<Any>{
        Log.d("     Parser",    "Parsing offline")
        var activities: MutableList<String> = mutableListOf()
        var activityTypes: MutableList<String> = mutableListOf()
        var timeStarts: MutableList<String> = mutableListOf()
        var timeDurations: MutableList<String> = mutableListOf()
        var descriptions: MutableList<String> = mutableListOf()
        var coursesNames: MutableList<String> = mutableListOf()
        var urls: MutableList<String> = mutableListOf()
        var accessTime = sharedDS.get("accessTime")
        var source: Byte = -1  //-1 = error; 0 = new; 1 = old
        val areThereNewTasks = false

        if (accessTime != ""){
            source = 1
            activities = sharedDS.getList("activities")
            activityTypes = sharedDS.getList("activityTypes")
            timeStarts = sharedDS.getList("timeStarts")
            timeDurations = sharedDS.getList("timeDurations")
            descriptions = sharedDS.getList("descriptions")
            coursesNames = sharedDS.getList("coursesNames")
            urls = sharedDS.getList("URLs")
            accessTime = sharedDS.get("accessTime")
        }
        return listOf(accessTime, activities, activityTypes, timeStarts, descriptions, coursesNames, urls, timeDurations, source, areThereNewTasks)
    }

    fun parse() :List<Any> {
        Log.d("     Parser", "Parsing online")
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        val py = Python.getInstance()
        val pyModule = py.getModule("parser")
        //token chores
        var token = sharedDS.get("token")
        if (token == "" || token == "-1"){
            val email = sharedDS.get("email")
            val password = sharedDS.get("password")
            token = pyModule.callAttr("getToken", email, password).toString()
            sharedDS.writeStr("token", token)
        }


        var source: Byte = -1  //-1 = error; 0 = new; 1 = old
        var jsonDict = PyObject.fromJava("-1")
        if (token != "-1"){
            jsonDict = pyModule.callAttr("formatDict", pyModule.callAttr("getCalendar", token))}

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

            for (i in events) {
                activities.add(i.asMap()[PyObject.fromJava("activityname")].toString())
                activityTypes.add(i.asMap()[PyObject.fromJava("activitystr")].toString())
                timeStarts.add(i.asMap()[PyObject.fromJava("timestart")].toString())
                timeDurations.add(i.asMap()[PyObject.fromJava("timeduration")].toString())
                descriptions.add(i.asMap()[PyObject.fromJava("description")].toString())
                urls.add(i.asMap()[PyObject.fromJava("viewurl")].toString())
                coursesNames.add(i.asMap()[PyObject.fromJava("course")]?.asMap()?.get(
                    PyObject.fromJava(
                        "fullname"
                    )
                ).toString()) }

            val knownActivities = sharedDS.getList("activities")
            val knownTimestarts = sharedDS.getList("timeStarts")

            //re-enqueue if there are new tasks
            //we check if there are new activities or if there are new deadlines, which i guess handles cases when the new ass has old name
            //cause they are unlike to has the same timestart obviously
            if (!knownActivities.containsAll(activities) or !knownTimestarts.containsAll(timeStarts)) {
                //TODO: optimise by translating only new descr, titles and courses (requires storing maps<origin:translation>)
                sharedDS.clearStr("isTranslated")
                areThereNewTasks = true
                enqueueDeadlines(timeStarts, sharedDS.get("hoursBeforeDeadline").toIntOrNull() ?: 5)

            } else if(!activities.containsAll(knownActivities) or !timeStarts.containsAll(knownTimestarts)) {
                // when some tasks passed away
                val amountAssignsPassed = knownActivities.lastIndex - activities.lastIndex
                Log.d("     Parser", "amount of asses has reduced by $amountAssignsPassed")
                Log.d("     Parser", "$activities\n$knownActivities")

                if (sharedDS.get("isTranslated") == "1"){
                    val translatedDescr = sharedDS.getList("translated_descriptions")
                    val translatedActs = sharedDS.getList("translated_activities")
                    val translatedCourses = sharedDS.getList("translated_coursesNames")
                    //Log.d("     Parser", "trans acts before transformation $translatedActs")

                    val currentLastIndex = translatedActs.lastIndex
                    sharedDS.writeList("translated_activities", translatedActs.slice(0 + amountAssignsPassed..currentLastIndex) as MutableList<String>)
                    sharedDS.writeList("translated_descriptions", translatedDescr.slice(0 + amountAssignsPassed..currentLastIndex) as MutableList<String>)
                    sharedDS.writeList("translated_coursesNames", translatedCourses.slice(0 + amountAssignsPassed..currentLastIndex) as MutableList<String>)

                    //Log.d("     Parser", "trans acts after transformation ${sharedDS.getList(context,"translated_activities")}")
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
            //TODO: rearrange
            listOf(accessTime, activities, activityTypes, timeStarts, descriptions, coursesNames, urls, timeDurations, source, areThereNewTasks)
        }
        //Offline
        else {
            // return offline with outdated status
            val data = (parseOffline()).toMutableList()
            data
        }
    }
}
