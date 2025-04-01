package com.csttine.utmn.lms.lmsnotifier.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import android.util.Patterns
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.languageManager.LanguageManager
import com.csttine.utmn.lms.lmsnotifier.parser.formatTimeStamps
import com.csttine.utmn.lms.lmsnotifier.parser.parseOffline
import com.csttine.utmn.lms.lmsnotifier.translator.Translator
import kotlinx.coroutines.CoroutineScope
import java.util.Locale


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


class ScheduleDialog: DialogFragment() {
    var position: Int = 0
    var locale: String = ""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Log.d("     DIALOG", "${mixedList.indices} $position $locale")
        val viewModel =  ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater.
            val inflater = requireActivity().layoutInflater;
            // Inflate and set the layout for the dialog.
            // Pass null as the parent view because it's going in the dialog
            // layout.
            val dialogView = inflater.inflate(R.layout.popup_screen, null)
            // modify the view
            val mixedList =viewModel.data.value ?: listOf()
            Log.d("     DIALOG", "${mixedList.indices}")
            if (mixedList.isNotEmpty()){
                dialogView.findViewById<TextView>(R.id.activity).text = (mixedList[1] as List<String>)[position]
                dialogView.findViewById<TextView>(R.id.course).text = (mixedList[5] as List<String>)[position]
                dialogView.findViewById<TextView>(R.id.type).text = (mixedList[2] as List<String>)[position]
                dialogView.findViewById<TextView>(R.id.timestart).text = formatTimeStamps((mixedList[3] as List<String>)[position], locale)
                dialogView.findViewById<TextView>(R.id.duration).text = formatTimeStampsDuration((mixedList[7] as List<String>)[position], LanguageManager().getCurrentLangCode(requireContext()))
                dialogView.findViewById<TextView>(R.id.url).text = (mixedList[6] as List<String>)[position]
                dialogView.findViewById<TextView>(R.id.description).text = Html.fromHtml((mixedList[4] as List<String>)[position], Html.FROM_HTML_MODE_COMPACT)
            }
            builder.setView(dialogView)
                // Add action buttons.
                .setNegativeButton(R.string.button_close
                ) { dialog, id ->
                    //getDialog()?.cancel()
                    viewModel.isDialogShowing = false
                    dismiss()
                }
            Log.d("DIALOG", "CREATED")
            viewModel.isDialogShowing = true
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

/*private fun popupNote(position: Int, mixedList: List<Any>, locale: String){
    @SuppressLint("InflateParams")
    val rootView = (requireActivity() as AppCompatActivity).findViewById<View>(android.R.id.content)
    val popupLayout = layoutInflater.inflate(R.layout.popup_screen, null, false)

    popupLayout.findViewById<TextView>(R.id.activity).text = (mixedList[1] as List<String>)[position]
    popupLayout.findViewById<TextView>(R.id.course).text = (mixedList[5] as List<String>)[position]
    popupLayout.findViewById<TextView>(R.id.type).text = (mixedList[2] as List<String>)[position]
    popupLayout.findViewById<TextView>(R.id.timestart).text = formatTimeStamps((mixedList[3] as List<String>)[position], locale)
    popupLayout.findViewById<TextView>(R.id.duration).text = formatTimeStampsDuration((mixedList[7] as List<String>)[position], LanguageManager().getCurrentLangCode(requireContext()))
    popupLayout.findViewById<TextView>(R.id.url).text = (mixedList[6] as List<String>)[position]
    popupLayout.findViewById<TextView>(R.id.description).text = Html.fromHtml((mixedList[4] as List<String>)[position], Html.FROM_HTML_MODE_COMPACT)

    val navBar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_bar)
    var isListenerTriggered = false  //used to prevent redundant onGlobalLayout listener execution

    navBar.viewTreeObserver.addOnGlobalLayoutListener {
        if (isListenerTriggered) return@addOnGlobalLayoutListener  //stops execution
        isListenerTriggered = true


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
}*/


class ScheduleViewModel : ViewModel(){
    companion object {
        var isParsed = false
        val dataTemp = MutableLiveData<List<Any>>()
        var isFallback = false
    }

    //var isFirstCreation = true
    var isTranslating = false
    val data : LiveData<List<Any>> = dataTemp
    var selectedNote = -1
    var locale = ""
    var isDialogShowing = false

    fun asyncParse(context: Context){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    //===================Parse=========================
                    lateinit var mixedList: MutableList<Any>
                    if (isParsed){
                        mixedList = parseOffline(context).toMutableList()
                        if (!isFallback) mixedList[8] = (0).toByte() // setting source to online if it hasn't fallen back to offline during parse
                    }
                    else mixedList = parse(context).toMutableList()

                    //===================Translate=========================
                    try {
                        val shouldTranslate = when(SharedDS().get(context, "Translation")){
                            "1" -> true
                            else -> false
                        }
                        val isTranslated = when (SharedDS().get(context, "isTranslated")){
                            "1" -> true
                            else -> false
                        }

                        if (shouldTranslate && mixedList[8] != (-1).toByte()){
                            if (isTranslated){
                                Log.d("     TRANSLATING", "TRANSLATED")
                                mixedList[4] = SharedDS().getList(context, "translated_descriptions")
                                mixedList[1] = SharedDS().getList(context, "translated_activities")
                                mixedList[5] = SharedDS().getList(context, "translated_coursesNames")

                            }
                            else if (!isTranslating){
                                Log.d("     TRANSLATING", "NOT TRANSLATED")
                                withContext(Dispatchers.Main){
                                    isTranslating = true
                                    Toast.makeText(context, context.getString(R.string.toast_translating), Toast.LENGTH_LONG).show()
                                }

                                //get correct email
                                var email = SharedDS().get(context, "email")
                                email = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                                    Log.d("     getCorrectEmail", "wrong email $email")
                                    val emailNew = if (email.take(4).lowercase() == "stud") "$email@study.utmn.ru" // assume user is student
                                    else "$email@utmn.ru"
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_incorrectEmail, email, emailNew),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    emailNew
                                }
                                else {
                                    Log.d("     getCorrectEmail", "correct email $email")
                                    email
                                }

                                val translator = Translator()
                                val cyrillicLanguages = listOf("rus", "bel", "ukr", "bul", "srp", "mkd", "kaz",
                                    "tgk", "tat", "kir", "uzb", "bak", "che", "mon")
                                // opennlp may detect lang not correctly
                                val descriptions = mixedList[4] as List<String>
                                val titles = mixedList[1] as List<String>
                                val courses = mixedList[5] as List<String>
                                val coursesTranslationMap: MutableMap<String, String> = mutableMapOf() // stores pairs of course : translation to speed up

                                // Parallelize translation tasks
                                val translationScope = CoroutineScope(Dispatchers.IO)

                                // Translate Titles
                                val titleJob = translationScope.launch {
                                    val translatedTitles = titles.mapIndexed { _, title ->
                                        if (translator.detectLanguage(title) in cyrillicLanguages) {
                                            translator.translateRuToEn(context, title, email)
                                        } else {
                                            title
                                        }
                                    }
                                    (mixedList[1] as MutableList<String>).clear()
                                    (mixedList[1] as MutableList<String>).addAll(translatedTitles)
                                }

                                // Translate Courses Names
                                val courseJob = translationScope.launch {
                                    val translatedCourses = courses.mapIndexed { _, course ->
                                        if (coursesTranslationMap.containsKey(course)) {
                                            coursesTranslationMap[course]
                                        } else {
                                            if (translator.detectLanguage(course) in cyrillicLanguages) {
                                                val translatedCourse = translator.translateRuToEn(context, course, email)
                                                coursesTranslationMap[course] = translatedCourse
                                                translatedCourse
                                            } else {
                                                course
                                            }
                                        }
                                    }
                                    (mixedList[5] as MutableList<String>).clear()
                                    (mixedList[5] as MutableList<String>).addAll(translatedCourses as List<String>)
                                }

                                // Translate Descriptions
                                val descriptionJob = translationScope.launch {
                                    val translatedDescriptions = descriptions.mapIndexed { _, descr ->
                                        Log.d("     ScheduleFragment", "translating descr $descr")
                                        if (translator.detectLanguage(descr) in cyrillicLanguages) {
                                            var newDescr = ""
                                            for (i in Translator().splitRuTextIntoSentences(descr)){
                                                Log.d("     ScheduleFragment", "translating part $i")
                                                newDescr += translator.translateRuToEn(context, i, email)
                                            }
                                            newDescr
                                        } else {
                                            descr
                                        }
                                    }
                                    (mixedList[4] as MutableList<String>).clear()
                                    (mixedList[4] as MutableList<String>).addAll(translatedDescriptions)
                                }

                                // Wait for all jobs to complete
                                listOf(titleJob, courseJob, descriptionJob).forEach { job ->
                                    job.join()
                                }

                                SharedDS().writeStr(context, "isTranslated", "1")
                                Log.d("TRANSLATING", "FINISH ${SharedDS().get(context, "isTranslated")}")
                                SharedDS().writeList(context, "translated_activities", mixedList[1] as MutableList<String>)
                                SharedDS().writeList(context, "translated_descriptions", mixedList[4] as MutableList<String>)
                                SharedDS().writeList(context, "translated_coursesNames", mixedList[5] as MutableList<String>)
                                isTranslating = false
                            } else Log.d("      TRANSLATING", "NOT STARTING, BECAUSE ALREADY TRANSLATING")
                        }
                    } catch (e: Exception){
                        Log.e("     asyncParse()", "translation error ${e.message}", e)
                    }

                    isParsed = true
                    Log.d("     Parse", "${mixedList[1] as List<String>}")
                    dataTemp.postValue(mixedList)
                }
            } catch (e: Exception){
                Log.e("     asyncParse()", "${e.message}", e)
                //TODO: post mixedList w error status if parsing was interrupted
            }
        }
    }
}


class ScheduleFragment : Fragment() {

    private lateinit var viewModel : ScheduleViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        return view }


    //TODO : POPUP ON CLOSE selectedPopup = -1
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]

        val dialog = ScheduleDialog()
        Log.d("     DIALOG", "${viewModel.isDialogShowing}")
        if (viewModel.isDialogShowing){
            dialog.locale = viewModel.locale
            dialog.position = viewModel.selectedNote
            dialog.show(childFragmentManager, "ScheduleDialog")
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty() and !viewModel.isTranslating){
                val infoText = view.findViewById<TextView>(R.id.infoText)
                view.findViewById<ProgressBar>(R.id.loadingAnim).isVisible = false

                viewModel.locale = LanguageManager().getCurrentLangCode(requireContext())

                //setting info message
                if (data[8] == (-1).toByte()) {
                    infoText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    infoText.text = getString(R.string.scheduleErrorMsg)
                }
                else{
                    if (data[8] == 0.toByte())
                        infoText.text = getString(R.string.schedule_accessTime, formatTimeStamps(data[0] as String, viewModel.locale))
                    else {
                        infoText.text = getString(R.string.schedule_accessTime, formatTimeStamps(data[0] as String, viewModel.locale)) + " (" + getString(R.string.schedule_outdated) + ")"
                        ScheduleViewModel.isFallback = true

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


                    adapter = CardViewAdapter(titlesList, data[5] as List<String>, data[3] as List<String>, viewModel.locale, object : CardViewAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            //popupNote(position, data, locale)
                            viewModel.selectedNote = position
                            dialog.position = position
                            dialog.locale = viewModel.locale
                            dialog.show(childFragmentManager, "ScheduleDialog")

                            viewModel.selectedNote = position
                            Log.d("     pop-up", viewModel.selectedNote.toString())

                        }
                    })
                    recyclerView.adapter = adapter
                }
            }
        }

        viewModel.asyncParse(requireContext())
    }
}