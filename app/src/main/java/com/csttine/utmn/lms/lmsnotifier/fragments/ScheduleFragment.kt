package com.csttine.utmn.lms.lmsnotifier.fragments

import android.content.Context
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
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
import java.util.Date


fun formatTimeStamps(timestamp: String) :String {
    //formats with app locale
    val format = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Resources.getSystem().configuration.locales[0])
    return format.format(Date(timestamp.toLong() * 1000)) //sec to mill sec
}


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
                    val source = mixedList[8] as Byte
                    if (source == (-1).toByte()){
                        text = "Что-то пошло не так\n" +
                                "Проверьте подключение к сети\n" +
                                "Или правильность логина и пароля\n" +
                                "Или возможно сайт LMS сейчас лежит\n" +
                                "бим бим бам бам 322\n" +
                                "https://github.com/Hlormar/UTMN-LMS-LXP-Notifier"
                    } else {
                        val accessTime = (mixedList[0] as String)
                        text = if (source == 0.toByte()){
                            "Время доступа: " + formatTimeStamps(accessTime) + "\n\n"}
                        else{
                            "Время доступа: " + formatTimeStamps(accessTime) + "(Старое) \n\n" }

                        val activities = mixedList[1] as List<String>
                        val activityTypes = mixedList[2] as List<String>
                        val courseNames = mixedList[5] as List<String>
                        val timeStarts = mixedList[3] as List<String>
                        val timeDurations = mixedList[7] as List<String>
                        val descriptions = mixedList[4] as List<String>
                        val urls = mixedList[6] as List<String>

                        for (i in activities.indices) {
                            val temp = timeDurations[i].toLong()

                            text += "Название: " + activities[i] + "\n"
                            text += "Тип: " + activityTypes[i] + "\n"
                            text += "Курс: " + courseNames[i] + "\n"
                            text += "Время: " + formatTimeStamps(timeStarts[i]) + "\n"
                            text += "Продолжительность: ${(temp / 3600)}:${((temp % 3600) / 60)}:${(temp % 60)}\n"
                            text += "Календарь: " + urls[i] + "\n"
                            text += "Описание: " + Html.fromHtml(
                                descriptions[i],
                                Html.FROM_HTML_MODE_COMPACT
                            ) + "\n\n"
                        }
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