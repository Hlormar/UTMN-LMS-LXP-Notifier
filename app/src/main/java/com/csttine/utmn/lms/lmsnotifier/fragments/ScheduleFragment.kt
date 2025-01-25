package com.csttine.utmn.lms.lmsnotifier.fragments

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.parser.parse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ScheduleViewModel : ViewModel(){
    private val dataTemp = MutableLiveData<String>()
    val data : LiveData<String> = dataTemp
    companion object {
        var isParsed = false
    }
    private var text = ""

    fun asyncParse(context: Context){
        viewModelScope.launch {
            if (!isParsed){
                isParsed = true
                withContext(Dispatchers.IO) {
                    val mixedList = parse(context)
                    text = "Время доступа:" + mixedList[0] + "\n\n"
                    val activities = mixedList[1] as List<String>
                    val activityTypes = mixedList[2] as List<String>
                    val courseNames = mixedList[5] as List<String>
                    val timeStarts = mixedList[3] as List<String>
                    val timeDuration = mixedList[7] as List<String>
                    val descriptions = mixedList[4] as List<String>
                    val urls = mixedList[6] as List<String>
                    val amount = activities.size - 1

                    for (i in 0..amount) {
                        text += "Название: " + activities[i] + "\n"
                        text += "Тип: " + activityTypes[i] + "\n"
                        text += "Курс: " + courseNames[i] + "\n"
                        text += "Время: " + timeStarts[i] + "\n"
                        text += "Продолжительность: " + timeDuration[i] + "\n"
                        text += "Календарь: " + urls[i] + "\n"
                        text += "Описание: " + Html.fromHtml(
                            descriptions[i],
                            Html.FROM_HTML_MODE_COMPACT
                        ) + "\n\n"
                    }
                }
            }
            dataTemp.postValue(text)
        }
    }
}

class ScheduleFragment : Fragment() {

    private lateinit var viewModel : ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        return view }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]
        viewModel.data.observe(viewLifecycleOwner) { data ->
            view.findViewById<TextView>(R.id.test).text = data
            view.findViewById<ProgressBar>(R.id.loadingAnim).isVisible = false
        }

        viewModel.asyncParse(requireContext())
    }
}