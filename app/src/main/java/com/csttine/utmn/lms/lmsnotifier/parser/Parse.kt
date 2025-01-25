package com.csttine.utmn.lms.lmsnotifier.parser

import android.content.Context
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import java.util.Date


private fun formatTimeStamps(timestamp: Long) :String {
    //formats with app locale
    val format = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Resources.getSystem().configuration.locales[0])
    return format.format(Date(timestamp * 1000)) //sec to mill sec
}


fun parse(context: Context) :List<Any> {
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
    }
    val py = Python.getInstance()
    val pyModule = py.getModule("parser")
    //token chores
    var token = SharedDS().get(context, "token")
    if (token == "" || token == "-1"){
        val email = SharedDS().get(context, "email")
        val password = SharedDS().get(context, "password")
        token = pyModule.callAttr("getToken", email, password).toString()
        SharedDS().writeStr(context, "token", token)
    }

    var activities: MutableList<String> = mutableListOf()
    var activityTypes: MutableList<String> = mutableListOf()
    var timeStarts: MutableList<String> = mutableListOf()
    var timeDurations: MutableList<String> = mutableListOf()
    var descriptions: MutableList<String> = mutableListOf()
    var coursesNames: MutableList<String> = mutableListOf()
    var urls: MutableList<String> = mutableListOf()
    var accessTime: String
    var jsonDict = PyObject.fromJava("-1")
    if (token != "-1"){
        jsonDict = pyModule.callAttr("formatDict", pyModule.callAttr("getCalendar", token))}

    //NEW
    if (jsonDict.toString() != "-1") {
        val events = jsonDict.asMap()[PyObject.fromJava("events")]?.asList() ?: emptyList()
        accessTime = formatTimeStamps(
            jsonDict.asMap()[PyObject.fromJava("date")]?.asMap()?.get(PyObject.fromJava("timestamp"))!!
                .toLong())

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

        //format timestarts and timedurations
        for (i in 0..<timeStarts.size){
            timeStarts[i] = formatTimeStamps(timeStarts[i].toLong())
            //TODO: better formating + not sure it represented as seconds
            val temp = timeDurations[i].toLong()
            timeDurations[i] = "${(temp / 3600)}:${((temp % 3600) / 60)}:${(temp % 60)}"
        }
        SharedDS().writeStr(context, "accessTime", accessTime)
        SharedDS().writeList(context, "activities", activities)
        SharedDS().writeList(context, "activityTypes", activityTypes)
        SharedDS().writeList(context, "timeStarts", timeStarts)
        SharedDS().writeList(context, "timeDurations", timeDurations)
        SharedDS().writeList(context, "descriptions", descriptions)
        SharedDS().writeList(context, "coursesNames", coursesNames)
        SharedDS().writeList(context, "URLs", urls)

    }
    //OLD
    else if (SharedDS().get(context, "accessTime") != ""){
        activities = SharedDS().getList(context, "activities")
        activityTypes = SharedDS().getList(context, "activityTypes")
        timeStarts = SharedDS().getList(context, "timeStarts")
        timeDurations = SharedDS().getList(context, "timeDurations")
        descriptions = SharedDS().getList(context, "descriptions")
        coursesNames = SharedDS().getList(context, "coursesNames")
        urls = SharedDS().getList(context, "URLs")
        accessTime = SharedDS().get(context, "accessTime") + "(Старое)"
    }
    //NOTHING
    else{
        accessTime = "Здесь пока ничего нет"
        activities.add("проверьте интернет")
        activityTypes.add("или корректность логина и пароля")
        coursesNames.add("либо сам лмс сейчас лежит")
        timeStarts.add("322")
        timeDurations.add("бим бим")
        urls.add("https://github.com/Hlormar/UTMN-LMS-LXP-Notifier")
        descriptions.add("бам бам")
    }
    //TODO: rearrange
    return listOf(accessTime, activities, activityTypes, timeStarts, descriptions, coursesNames, urls, timeDurations)
}
