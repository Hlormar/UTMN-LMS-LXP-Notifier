package com.csttine.utmn.lms.lmsnotifier.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.languageManager.LanguageManager
import com.csttine.utmn.lms.lmsnotifier.translator.Translator
import java.util.Date
import java.util.Locale


fun formatTimeStamps(timestamp: String, locale: String) :String {
    //formats with app locale
    val format = SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Locale(locale, locale))
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
    private val locale: String,
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
        val description = "${coursesList[position]}\n${formatTimeStamps(timestarts[position], locale)}"
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

    //TODO: move offline part from parse() to separated func;
    /*
    val mixedList: mutableListOf<Any> = mutableListOf()
    if (!isparsed) mixedList= online parse()
    else mixedList = offline parse()

    if(should translate)
        if(is tanslated)
            mixedlist[4] = sharedDS.getList("translated descr")
        else
            sharedDS.write(translated = 1)
            for (i in range len(mixedlist[4]))
                TODO: translate text extracted from html and then insert it back to html
                if (mixedList[4][i] = russian)
                    temp = translate(portion(split sentences(text from html(mixedList[4][i]))))
                    mixedList[4][i] = insert text to html(temp)
    datatemp.post(mixedlist)
    */

    fun asyncParse(context: Context){
        viewModelScope.launch {
            try {
                if (!isParsed) {
                    isParsed = true
                    isFirstCreation = true
                    withContext(Dispatchers.IO) {
                        val mixedList = parse(context).toMutableList()
                        val shouldTranslate = when(SharedDS().get(context, "Translation")){
                            "1" -> true
                            else -> false
                        }
                        val isTranslated = when (SharedDS().get(context, "isTranslated")){
                            "1" -> true
                            else -> false
                        }

                        if (shouldTranslate){
                            if (isTranslated){
                                Log.d("     TRANSLATING", "SHOULD + TRANSLATED")
                                mixedList[4] = SharedDS().getList(context, "translatedDescr")
                            }
                            else{
                                Log.d("     TRANSLATING", "SHOULD + NOT TRANSLATED")
                                for (i in (mixedList[4] as List<String>).indices ){
                                    val descr = (mixedList[4] as List<String>)[i]
                                    Log.d("     TRANSLATING", "detectlang of $i ${Translator().detectLanguage(descr)}")
                                    Log.d("     TRANSLATING", "Sentences of $i ${Translator().splitRuTextIntoSentences(descr)}")
                                    Log.d("     TRANSLATING", "Portion of $i ${Translator().portionSentences(Translator().splitRuTextIntoSentences(descr))}")

                                }
                                //SharedDS().writeStr(context, "isTranslated", "1")
                            }
                        }

                        dataTemp.postValue(mixedList)
                    }
                }
            } catch (e: Exception){
                Log.e("     asyncParse()", "${e.message}", e)
            }
        }
    }
}


class ScheduleFragment : Fragment() {

    private var selectedNote :Int = -1
    private lateinit var viewModel : ScheduleViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardViewAdapter


    private fun popupNote(position: Int, mixedList: List<Any>, locale: String){
        @SuppressLint("InflateParams")
        val rootView = (requireActivity() as AppCompatActivity).findViewById<View>(android.R.id.content)
        val popupLayout = layoutInflater.inflate(R.layout.popup_screen, null, false)

        popupLayout.findViewById<TextView>(R.id.activity).text = (mixedList[1] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.course).text = (mixedList[5] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.type).text = (mixedList[2] as List<String>)[position]
        popupLayout.findViewById<TextView>(R.id.timestart).text = formatTimeStamps((mixedList[3] as List<String>)[position], locale)
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

            //TODO: make sizing, dimming, make card clickable, do translatein
            navBar.viewTreeObserver.removeOnGlobalLayoutListener{}
        }

        val windowManager = requireActivity().windowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val popupWidth = (displayMetrics.widthPixels * 0.9).toInt()
        val popupHeight = (displayMetrics.heightPixels * 0.85).toInt()
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

                val locale = LanguageManager().getCurrentLangCode(requireContext())

                //setting info message
                if (data[8] as Byte == (-1).toByte()) {
                    infoText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    infoText.text = getString(R.string.scheduleErrorMsg)
                }
                else{
                    infoText.text = if (data[8] == 0.toByte()) getString(R.string.schedule_accessTime, formatTimeStamps(data[0] as String, locale))
                    else getString(R.string.schedule_accessTime, formatTimeStamps(data[0] as String, locale)) + " (" + getString(R.string.schedule_outdated) + ")"


                    //restore opened note
                    selectedNote = savedInstanceState?.getInt("selectedNote") ?: -1
                    Log.d("     restoring", selectedNote.toString())
                    if (selectedNote != -1){
                        popupNote(selectedNote, data, locale)
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


                    adapter = CardViewAdapter(titlesList, data[5] as List<String>, data[3] as List<String>, locale, object : CardViewAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            popupNote(position, data, locale)
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