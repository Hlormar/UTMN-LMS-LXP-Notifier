package com.csttine.utmn.lms.lmsnotifier.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.languageManager.LanguageManager
import com.csttine.utmn.lms.lmsnotifier.parser.LMSParser
import com.csttine.utmn.lms.lmsnotifier.translator.Translator
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private class CardViewAdapter(
    private val context: Context,
    private val titlesList: List<String>,
    private val coursesList: List<String>,
    private val timestarts: List<String>,
    private val locale: String,
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {

    private val lmsParser by lazy {LMSParser(context)}

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
        val description = "${coursesList[position]}\n${lmsParser.formatTimeStamps(timestarts[position], locale)}"
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
        val lmsParser by lazy {LMSParser(requireContext())}

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater.
            val inflater = requireActivity().layoutInflater
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
                dialogView.findViewById<TextView>(R.id.timestart).text = lmsParser.formatTimeStamps((mixedList[3] as List<String>)[position], locale)
                dialogView.findViewById<TextView>(R.id.duration).text = lmsParser.formatTimeStampsDuration((mixedList[7] as List<String>)[position], LanguageManager(requireContext()).getCurrentLangCode())
                dialogView.findViewById<TextView>(R.id.url).text = (mixedList[6] as List<String>)[position]
                dialogView.findViewById<TextView>(R.id.description).text = Html.fromHtml((mixedList[4] as List<String>)[position], Html.FROM_HTML_MODE_COMPACT)
            }
            builder.setView(dialogView)
                // Add action buttons.
                .setNegativeButton(R.string.button_close
                ) { _, _ ->
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

class ScheduleViewModel : ViewModel(){
    companion object {
        var isParsed = false
        val dataTemp = MutableLiveData<List<Any>>()
        //var isFallback = false
    }

    //var isFirstCreation = true
    var isTranslating = false
    val data : LiveData<List<Any>> = dataTemp
    var selectedNote = -1
    var locale = ""
    var isDialogShowing = false
    var status :Byte = -1

    fun asyncParse(context: Context){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    //===================Parse=========================
                    lateinit var mixedList: MutableList<Any>
                    val lmsParser by lazy {LMSParser(context)}
                    val sharedDS by lazy {SharedDS.getInstance(context)}
                    if (isParsed){
                        mixedList = lmsParser.parseOffline().toMutableList()
                        //if (!isFallback) mixedList[8] = (0).toByte() // setting source to online if it hasn't fallen back to offline during parse
                    }
                    else {
                        mixedList = lmsParser.parse().toMutableList()
                        status = mixedList[8] as Byte
                    }

                    //===================Translate=========================
                    try {
                        val shouldTranslate = when(sharedDS.get("Translation")){
                            "1" -> true
                            else -> false
                        }
                        val isTranslated = when (sharedDS.get("isTranslated")){
                            "1" -> true
                            else -> false
                        }

                        if (shouldTranslate && mixedList[8] != (-1).toByte()){
                            if (isTranslated){
                                Log.d("     TRANSLATING", "TRANSLATED")
                                mixedList[4] = sharedDS.getList("translated_descriptions")
                                mixedList[1] = sharedDS.getList("translated_activities")
                                mixedList[5] = sharedDS.getList("translated_coursesNames")

                            }
                            else if (!isTranslating){
                                Log.d("     TRANSLATING", "NOT TRANSLATED")
                                withContext(Dispatchers.Main){
                                    isTranslating = true
                                    Toast.makeText(context, context.getString(R.string.toast_translating), Toast.LENGTH_LONG).show()
                                }

                                //get correct email
                                var email = sharedDS.get("email")
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

                                val translator by lazy { Translator() }
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
                                        //Log.d("     ScheduleFragment", "translating descr $descr")
                                        if (translator.detectLanguage(descr) in cyrillicLanguages) {
                                            var newDescr = ""
                                            for (i in translator.portionSentences(translator.splitRuTextIntoSentences(descr))){
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

                                sharedDS.writeStr("isTranslated", "1")
                                Log.d("TRANSLATING", "FINISH ${sharedDS.get("isTranslated")}")
                                sharedDS.writeList("translated_activities", mixedList[1] as MutableList<String>)
                                sharedDS.writeList("translated_descriptions", mixedList[4] as MutableList<String>)
                                sharedDS.writeList("translated_coursesNames", mixedList[5] as MutableList<String>)
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
    private val lmsParser by lazy {LMSParser(requireContext())}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)
        return view }

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

                viewModel.locale = LanguageManager(requireContext()).getCurrentLangCode()

                //setting info message
                Log.d("     ScheduleFragment", "${data[8] == (-1).toByte()} ${data[8] == (0).toByte()} ${data[8] == (1).toByte()}")
                if (viewModel.status == (-1).toByte()) {
                    infoText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    infoText.text = getString(R.string.scheduleErrorMsg)
                    //ScheduleViewModel.isFallback = true
                }
                else{
                    if (viewModel.status == 0.toByte())
                        //TODO fix crasho after lang change
                        infoText.text = getString(R.string.schedule_accessTime, lmsParser.formatTimeStamps(data[0] as String, viewModel.locale))
                    else {
                        infoText.text = getString(R.string.schedule_accessTime, lmsParser.formatTimeStamps(data[0] as String, viewModel.locale)) + " (" + getString(R.string.schedule_outdated) + ")"

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


                    adapter = CardViewAdapter(requireContext(), titlesList, data[5] as List<String>, data[3] as List<String>, viewModel.locale, object : CardViewAdapter.OnItemClickListener {
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