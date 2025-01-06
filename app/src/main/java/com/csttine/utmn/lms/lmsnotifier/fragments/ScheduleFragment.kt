package com.csttine.utmn.lms.lmsnotifier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.csttine.utmn.lms.lmsnotifier.MainActivity
import com.csttine.utmn.lms.lmsnotifier.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        val test = view.findViewById<TextView>(R.id.test)
        GlobalScope.launch(Dispatchers.Main) {
            delay(1)
            val mixedList = MainActivity.ParsingChores.parse(requireContext())
            var text = "Время доступа:" + mixedList[0] + "\n\n"

            val activities = mixedList[1] as List<String>
            val activityTypes = mixedList[2] as List<String>
            val courseNames = mixedList[5] as List<String>
            val timeStamps = mixedList[3] as List<String>
            val descriptions = mixedList[4] as List<String>
            val amount = activities.size - 1

            for (i in 0..amount) {
                text += "Название: " + activities[i] + "\n"
                text += "Тип: " + activityTypes[i] + "\n"
                text += "Курс:" + courseNames[i] + "\n"
                text += "Начало: " + timeStamps[i] + "\n"
                text += "Описание: " + descriptions[i] + "\n\n"
            }
            test.text = text
        }
        return view
    }
}