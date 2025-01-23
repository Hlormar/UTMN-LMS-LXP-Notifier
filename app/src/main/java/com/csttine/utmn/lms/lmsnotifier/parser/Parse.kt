package com.csttine.utmn.lms.lmsnotifier.parser

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS

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
    var timeStamps: MutableList<String> = mutableListOf()
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
        accessTime = pyModule.callAttr("convertTime",jsonDict.asMap()[PyObject.fromJava("date")]?.asMap()?.get(
            PyObject.fromJava("timestamp")
        )).toString()
        //test
        for (i in events) {
            activities.add(i.asMap()[PyObject.fromJava("activityname")].toString())
            activityTypes.add(i.asMap()[PyObject.fromJava("activitystr")].toString())
            timeStamps.add(i.asMap()[PyObject.fromJava("timestart")].toString())
            descriptions.add(i.asMap()[PyObject.fromJava("description")].toString())
            urls.add(i.asMap()[PyObject.fromJava("viewurl")].toString())
            coursesNames.add(i.asMap()[PyObject.fromJava("course")]?.asMap()?.get(
                PyObject.fromJava(
                    "fullname"
                )
            ).toString()) }

        for (i in 0..<timeStamps.size){
            timeStamps[i] = pyModule.callAttr("convertTime", timeStamps[i]).toString()
        }
        SharedDS().writeStr(context, "accessTime", accessTime)
        SharedDS().writeList(context, "activities", activities)
        SharedDS().writeList(context, "activityTypes", activityTypes)
        SharedDS().writeList(context, "timeStamps", timeStamps)
        SharedDS().writeList(context, "descriptions", descriptions)
        SharedDS().writeList(context, "coursesNames", coursesNames)
        SharedDS().writeList(context, "URLs", urls)

    }
    //OLD
    else if (SharedDS().get(context, "accessTime") != ""){
        activities = SharedDS().getList(context, "activities")
        activityTypes = SharedDS().getList(context, "activityTypes")
        timeStamps = SharedDS().getList(context, "timeStamps")
        descriptions = SharedDS().getList(context, "descriptions")
        coursesNames = SharedDS().getList(context, "coursesNames")
        urls = SharedDS().getList(context, "URLs")
        accessTime = SharedDS().get(context, "accessTime") + "(Старое)"
    }
    //NOTHING
    else{
        accessTime = "Что-то пошло не так"
        activities.add("проверьте интернет")
        activityTypes.add("или корректность логина и пароля")
        coursesNames.add("322")
        timeStamps.add("бим бим")
        urls.add("https://github.com/Hlormar/UTMN-LMS-LXP-Notifier")
        descriptions.add("бам бам")
    }
    return listOf(accessTime, activities, activityTypes, timeStamps, descriptions, coursesNames, urls)
}
