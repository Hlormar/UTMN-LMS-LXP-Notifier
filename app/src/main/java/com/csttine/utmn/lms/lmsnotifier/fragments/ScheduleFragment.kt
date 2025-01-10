package com.csttine.utmn.lms.lmsnotifier.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.viewmodel.SharedViewModel

lateinit var viewModel : SharedViewModel


class ScheduleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        return view }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        viewModel.data.observe(viewLifecycleOwner, Observer { data ->
            val test = view.findViewById<TextView>(R.id.test)

            var text = "Время доступа:" + data[0] + "\n\n"
            val activities = data[1] as List<String>
            val activityTypes = data[2] as List<String>
            val courseNames = data[5] as List<String>
            val timeStamps = data[3] as List<String>
            val descriptions = data[4] as List<String>
            val urls = data[6] as List<String>
            val amount = activities.size - 1

            for (i in 0..amount) {
                text += "Название: " + activities[i] + "\n"
                text += "Тип: " + activityTypes[i] + "\n"
                text += "Курс: " + courseNames[i] + "\n"
                text += "Время: " + timeStamps[i] + "\n"
                text += "Календарь: " + urls[i] + "\n"
                text += "Описание: " + Html.fromHtml(descriptions[i], Html.FROM_HTML_MODE_COMPACT) + "\n\n"
            }
            test.text = text
        })

        viewModel.asyncParse(requireContext())
    }
}