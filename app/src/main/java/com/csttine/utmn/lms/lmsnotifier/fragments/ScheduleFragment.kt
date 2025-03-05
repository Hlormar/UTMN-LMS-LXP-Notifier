package com.csttine.utmn.lms.lmsnotifier.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.parser.parse
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.util.DisplayMetrics
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.csttine.utmn.lms.lmsnotifier.MainActivity
import java.util.Date


fun formatTimeStamps(timestamp: String) :String {
    //formats with app locale
    val format = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Resources.getSystem().configuration.locales[0])
    return format.format(Date(timestamp.toLong() * 1000)) //sec to mill sec
}

fun formatTimeStampsDuration(timestampStr: String) :String{
    val timestamp = timestampStr.toLong() * 1000
    val hours = (timestamp / (1000 * 60 * 60)) % 24
    val minutes = (timestamp / (1000 * 60)) % 60
    val seconds = (timestamp / 1000) % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private class CardViewAdapter(
    private val titlesList: List<String>,
    private val coursesList: List<String>,
    private val timestarts: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.cardTitle)
        val description: TextView = itemView.findViewById(R.id.cardDescription)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view, parent, false)
        return ViewHolder(itemView)
    }

    // Binding the data to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val description = "${coursesList[position]}\n${formatTimeStamps(timestarts[position])}"
        holder.title.text = titlesList[position]
        holder.description.text = description

        // Set click listener on the entire card view
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    // Return the size of the list
    override fun getItemCount() = titlesList.size
}


class ScheduleViewModel : ViewModel(){
    companion object {
        var isParsed = false
        val dataTemp = MutableLiveData<List<Any>>()
    }

    var isFirstCreation = true
    val data : LiveData<List<Any>> = dataTemp
    private var text = ""

    fun asyncParse(context: Context){
        viewModelScope.launch {
            if (!isParsed){
                isParsed = true
                isFirstCreation = true
                withContext(Dispatchers.IO) {
                    val mixedList = parse(context)
                    dataTemp.postValue(mixedList)
                    /*val source = mixedList[8] as Byte
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
                    }*/
                }
            }
        }
    }
}

class ScheduleFragment : Fragment() {

    private var selectedNote :Int = -1
    private lateinit var viewModel : ScheduleViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardViewAdapter


    private fun popupNote(position: Int, mixedList: List<Any>){
        @SuppressLint("InflateParams")
        val rootView = (requireActivity() as AppCompatActivity).findViewById<View>(android.R.id.content)
        val popupLayout = layoutInflater.inflate(R.layout.popup_screen, null, false)

        popupLayout.findViewById<TextView>(R.id.activity).text = (mixedList[1] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.course).text = (mixedList[5] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.type).text = (mixedList[2] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.timestart).text = formatTimeStamps((mixedList[3] as List<String>)[position])
        popupLayout.findViewById<TextView>(R.id.duration).text = formatTimeStampsDuration((mixedList[7] as List<String>)[position])
        popupLayout.findViewById<TextView>(R.id.url).text = (mixedList[6] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.description).text = Html.fromHtml((mixedList[4] as List<String>)[position], Html.FROM_HTML_MODE_COMPACT)

        val navBar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_bar)
        var isListenerTriggered = false  //used to prevent redundant onGlobalLayout listener execution

        navBar.viewTreeObserver.addOnGlobalLayoutListener {
            if (isListenerTriggered) return@addOnGlobalLayoutListener  //stops execution
            isListenerTriggered = true
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val windowMetrics = windowManager.currentWindowMetrics
    val bounds = windowMetrics.bounds
    width = bounds.width()
    height = bounds.height()
} else {
    val displayMetrics = DisplayMetrics()
    @Suppress("DEPRECATION") // Temporarily suppress warning if you must use it
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    width = displayMetrics.widthPixels
    height = displayMetrics.heightPixels
}*/

            //make sizing, dimming, make card clickable, do translatein
            navBar.viewTreeObserver.removeOnGlobalLayoutListener{}
        }

        val windowManager = requireActivity().windowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val popupWidth = (displayMetrics.widthPixels * 0.8).toInt()
        val popupHeight = (displayMetrics.heightPixels * 0.8).toInt()
        val popupWindow = PopupWindow(
            popupLayout,
            popupWidth,
            popupHeight,
            true
        )

        popupLayout.findViewById<ImageButton>(R.id.close).setOnClickListener{
            popupWindow.dismiss()
        }

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        return view }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]
        viewModel.data.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()){
                val infoText = view.findViewById<TextView>(R.id.infoText)
                view.findViewById<ProgressBar>(R.id.loadingAnim).isVisible = false

                //setting info message
                if (data[8] as Byte == (-1).toByte()) {
                    infoText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    infoText.text = getString(R.string.scheduleErrorMsg)
                }
                else{
                    infoText.text = if (data[8] == 0.toByte()){
                        "Время доступа: " + formatTimeStamps(data[0] as String)}
                    else{
                        "Время доступа: " + formatTimeStamps(data[0] as String) + "\n(Старое)"}


                    //restore opened note
                    selectedNote = savedInstanceState?.getInt("selectedNote") ?: -1
                    Log.d("     restoring", selectedNote.toString())
                    if (selectedNote != -1){
                        popupNote(selectedNote, data)
                    }

                    recyclerView = view.findViewById(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(context)

                    //Setting bottom padding
                    val navBar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_bar)
                    var isListenerTriggered = false  //used to prevent redundant onGlobalLayout listener execution

                    navBar.viewTreeObserver.addOnGlobalLayoutListener {
                        if (isListenerTriggered) return@addOnGlobalLayoutListener  //stops execution
                        isListenerTriggered = true
                        recyclerView.setPadding(
                            recyclerView.paddingLeft,
                            recyclerView.paddingTop,
                            recyclerView.paddingRight,
                            navBar.height)
                        navBar.viewTreeObserver.removeOnGlobalLayoutListener{}
                    }

                    recyclerView.isVisible = true

                    val titlesList = data[1] as MutableList<String>
                    val timestarts = data[3] as MutableList<String>

                    // add dates to descriptions
                    /*val descriptionsList =  data[5] as MutableList<String>
                    if (viewModel.isFirstCreation){
                        viewModel.isFirstCreation = false
                        for (i in descriptionsList.indices){
                            descriptionsList[i] = "${descriptionsList[i]}\n${formatTimeStamps(timestarts[i])}"
                        }
                    }*/

//                    titlesList.add("Venom")
//                    descriptionsList.add("Venom")

                    adapter = CardViewAdapter(titlesList, data[5] as List<String>, data[3] as List<String>, object : CardViewAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            // Handle item click
                            //val clickedItem = titlesList[position]
                            //Toast.makeText(context, "Clicked: ${clickedItem}", Toast.LENGTH_SHORT).show()
                            /*val windowManager = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            val displayMetrics = DisplayMetrics()
                            windowManager.defaultDisplay.getMetrics(displayMetrics)
                            val popupWidth = (displayMetrics.widthPixels * 0.95).toInt()
                            val popupHeight = (displayMetrics.heightPixels * 0.85).toInt()
                            */
                            popupNote(position, data)
                            selectedNote = position
                            Log.d("     pop-up", selectedNote.toString())

                        }
                    })
                    recyclerView.adapter = adapter
                }
            }
        }

        viewModel.asyncParse(requireContext())
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("     saving", selectedNote.toString())
        // not saving + shows on top
        outState.putInt("selectedNote", selectedNote)
    }
}