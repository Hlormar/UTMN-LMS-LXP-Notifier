package com.csttine.utmn.lms.lmsnotifier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.Python.getInstance
import com.chaquo.python.android.AndroidPlatform
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.fragments.ScheduleFragment
import com.csttine.utmn.lms.lmsnotifier.fragments.ScheduleViewModel
import com.csttine.utmn.lms.lmsnotifier.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

object ParsingChores{
    fun parse(context: Context) :List<Any> {
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        val py = getInstance()
        val pyModule = py.getModule("main2")
        //token chores
        var token = SharedDS.get(context, "token")
        if (token == "" || token == "-1"){
            val email = SharedDS.get(context, "email")
            val password = SharedDS.get(context, "password")
            token = pyModule.callAttr("getToken", email, password).toString()
            SharedDS.writeStr(context, "token", token)
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
                PyObject.fromJava("timestamp"))).toString()
            //test
            for (i in events) {
                activities.add(i.asMap()[PyObject.fromJava("activityname")].toString())
                activityTypes.add(i.asMap()[PyObject.fromJava("activitystr")].toString())
                timeStamps.add(i.asMap()[PyObject.fromJava("timestart")].toString())
                descriptions.add(i.asMap()[PyObject.fromJava("description")].toString())
                urls.add(i.asMap()[PyObject.fromJava("viewurl")].toString())
                coursesNames.add(i.asMap()[PyObject.fromJava("course")]?.asMap()?.get(PyObject.fromJava("fullname")).toString()) }

            for (i in 0..timeStamps.size-1){
                timeStamps[i] = pyModule.callAttr("convertTime", timeStamps[i]).toString()
            }

            SharedDS.writeStr(context, "accessTime", accessTime)
            SharedDS.writeList(context, "activities", activities)
            SharedDS.writeList(context, "activityTypes", activityTypes)
            SharedDS.writeList(context, "timeStamps", timeStamps)
            SharedDS.writeList(context, "descriptions", descriptions)
            SharedDS.writeList(context, "coursesNames", coursesNames)
            SharedDS.writeList(context, "URLs", urls)
        }
        //OLD
        else if (SharedDS.get(context, "accessTime") != ""){
            activities = SharedDS.getList(context, "activities")
            activityTypes = SharedDS.getList(context, "activityTypes")
            timeStamps = SharedDS.getList(context, "timeStamps")
            descriptions = SharedDS.getList(context, "descriptions")
            coursesNames = SharedDS.getList(context, "coursesNames")
            urls = SharedDS.getList(context, "URLs")
            accessTime = SharedDS.get(context, "accessTime") + "(Старое)"
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
}


class MainActivity : AppCompatActivity() {

    private var selectedFragment = 0

    private fun makeCurrentFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)


        val navBar = findViewById<BottomNavigationView>(R.id.nav_bar)

        selectedFragment = savedInstanceState?.getInt("FragmentIndex") ?: 0
        if (savedInstanceState == null || selectedFragment == 0){
            navBar.selectedItemId = R.id.menu_schedule
            makeCurrentFragment(ScheduleFragment())
        }
        else{
            navBar.selectedItemId = R.id.menu_settings
            makeCurrentFragment(SettingsFragment())
        }


        navBar.setOnItemSelectedListener {
            when (it.itemId){
                R.id.menu_lock -> {
                    val intent = Intent(this, LockScreen::class.java)
                    startActivity(intent) // Start the new activity
                    finish()
                }
                R.id.menu_schedule -> {
                    makeCurrentFragment(ScheduleFragment())
                    selectedFragment = 0}
                R.id.menu_settings -> {
                    ScheduleViewModel.reset()  //force to refresh schedule after returning to it
                    makeCurrentFragment(SettingsFragment())
                    selectedFragment = 1}
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("FragmentIndex", selectedFragment)
    }
}