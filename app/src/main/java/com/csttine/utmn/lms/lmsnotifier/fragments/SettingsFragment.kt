package com.csttine.utmn.lms.lmsnotifier.fragments

import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.fragments.SettingsFragmentViewModel.Companion.isFirstCreation
import com.csttine.utmn.lms.lmsnotifier.languageManager.LanguageManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragmentViewModel : ViewModel(){
    companion object {
        var isFirstCreation = MutableLiveData(true)
    }
    var email = ""
    var password = ""
    var passcode = ""
    var isTranslationEnabled = false
    var passwordEditInputType = (InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT)
    var passcodeEditInputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    var isEmailEdited = false
    var isPasswordEdited = false
    var isPasscodeEdited = false
    var emailFieldError : CharSequence? = null
    var passwordFieldError : CharSequence? = null
    var passcodeFieldError : CharSequence? = null
}


class SettingsFragment : Fragment() {

    private lateinit var viewModel : SettingsFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val emailEdit = view.findViewById<TextInputEditText>(R.id.emailEdit)
        val passwordEdit = view.findViewById<TextInputEditText>(R.id.passwordEdit)
        val emailField = view.findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordField = view.findViewById<TextInputLayout>(R.id.passwordLayout)
        val passcodeEdit = view.findViewById<TextInputEditText>(R.id.passcodeEdit)
        val passcodeField = view.findViewById<TextInputLayout>(R.id.passcodeLayout)
        val translationSwitcher = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.translationSwitcher)
        val languageList = view.findViewById<AutoCompleteTextView>(R.id.language_autocomplete)

        viewModel = ViewModelProvider(requireActivity())[SettingsFragmentViewModel::class.java]

        //restore field values after rotation or load defaults
        if (isFirstCreation.value == true) {
            isFirstCreation.value = false
            viewModel.email = SharedDS().get(requireContext(), "email")
            viewModel.password = SharedDS().get(requireContext(), "password")
            viewModel.passcode = SharedDS().get(requireContext(), "passcode")
            when (SharedDS().get(requireContext(),"Translation")) {
                "1" -> viewModel.isTranslationEnabled = true
                else -> viewModel.isTranslationEnabled = false
            }
        }

        emailEdit.setText(viewModel.email)
        passwordEdit.setText(viewModel.password)
        passcodeEdit.setText(viewModel.passcode)
        passwordEdit.inputType = viewModel.passwordEditInputType
        passcodeEdit.inputType = viewModel.passcodeEditInputType
        emailField.error = viewModel.emailFieldError
        passwordField.error = viewModel.passwordFieldError
        passcodeField.error = viewModel.passcodeFieldError
        translationSwitcher.isChecked = viewModel.isTranslationEnabled

        //disclaimer padding
        var isListenerTriggered = false
        val navBar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_bar)

        navBar.viewTreeObserver.addOnGlobalLayoutListener {
            if (isListenerTriggered) return@addOnGlobalLayoutListener  //stops execution
            isListenerTriggered = true
            val disclaimer = view.findViewById<TextView>(R.id.disclaimer)
            disclaimer.setPadding(
                disclaimer.paddingLeft,
                disclaimer.paddingTop,
                disclaimer.paddingRight,
                navBar.height + (15 * Resources.getSystem().displayMetrics.density).toInt())  //15dp
            navBar.viewTreeObserver.removeOnGlobalLayoutListener{}
        }

        //attach language list to menu
        val items = listOf(getString(R.string.lang_eng), getString(R.string.lang_rus))
        val adapter = ArrayAdapter(requireContext(), R.layout.language_list, items)
        languageList.setAdapter(adapter)

        //translation switcher colors
        if (viewModel.isTranslationEnabled) {
            translationSwitcher.thumbTintList = requireContext().getColorStateList(R.color.utmn)
            translationSwitcher.trackTintList = requireContext().getColorStateList(R.color.utmn_lighter)
        }

        //listeners inside Dispatchers.IO to provide async & instant fragment switching
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main){
            withContext(Dispatchers.IO){
                emailEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //check if actually has been edited
                        if (!s.isNullOrEmpty() && !viewModel.isEmailEdited){
                            viewModel.isEmailEdited = true
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {
                        viewModel.email = s.toString()
                        if (s.isNullOrEmpty() && viewModel.isEmailEdited){
                            emailField.error = getString(R.string.inputRequired)
                        }
                        else{
                            emailField.error = null
                        }
                        viewModel.emailFieldError = emailField.error
                    }
                })
                passwordEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //check if actually has been edited
                        if (!s.isNullOrEmpty() && !viewModel.isPasswordEdited){
                            viewModel.isPasswordEdited = true
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {
                        viewModel.password = s.toString()
                        if (s.isNullOrEmpty() && viewModel.isPasswordEdited){
                            passwordField.error = getString(R.string.inputRequired)
                        }
                        else{
                            passwordField.error = null
                        }
                        viewModel.passwordFieldError = passwordField.error
                    }
                })

                passcodeEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        //check if actually has been edited
                        if (!s.isNullOrEmpty() && !viewModel.isPasscodeEdited){
                            viewModel.isPasscodeEdited = true
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {
                        viewModel.passcode = s.toString()
                        if (s.isNullOrEmpty() && viewModel.isPasscodeEdited){
                            passcodeField.error = getString(R.string.inputRequired)
                        }
                        else if (!s!!.matches(Regex("\\d+"))){
                            passcodeField.error = getString(R.string.unacceptableChars)
                        }
                        else{
                            passcodeField.error = null
                        }
                        viewModel.passcodeFieldError = passcodeField.error
                    }
                })


                //Input type save & change
                passwordField.setEndIconOnClickListener {
                    if (viewModel.passwordEditInputType == (InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT)){
                        viewModel.passwordEditInputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                    }
                    else{
                        viewModel.passwordEditInputType = (InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT)
                    }
                    passwordEdit.inputType = viewModel.passwordEditInputType
                }

                passcodeField.setEndIconOnClickListener {
                    if (viewModel.passcodeEditInputType == (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)){
                        viewModel.passcodeEditInputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)
                    }
                    else{
                        viewModel.passcodeEditInputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                    }
                    passcodeEdit.inputType = viewModel.passcodeEditInputType
                }

                //TODO: fix the get life cycle exception error
                languageList.setOnItemClickListener { parent, _, position, _ ->
                    // Retrieve the selected item
                    val selectedItem = when (parent.getItemAtPosition(position) as String) {
                        getString(R.string.lang_rus) -> "ru"
                        else -> "en"
                    }
                    if (selectedItem != LanguageManager().getCurrentLangCode(requireContext())){
                        LanguageManager().updateLanguage(requireContext(), selectedItem)
                        // Check if the activity is in a valid state
                        if (!requireActivity().isFinishing && !requireActivity().isDestroyed) {
                            recreate(requireActivity())
                        }

                    }
                }
                translationSwitcher.setOnCheckedChangeListener{_, isChecked ->
                    if (isChecked){
                        //change on enabled state
                        translationSwitcher.thumbTintList = requireContext().getColorStateList(R.color.utmn)
                        translationSwitcher.trackTintList = requireContext().getColorStateList(R.color.utmn_lighter)
                        SharedDS().writeStr(requireContext(), "Translation", "1")
                    }
                    else{
                        //set to false + clean cache
                        SharedDS().writeStr(requireContext(), "Translation", "")
                        SharedDS().writeStr(requireContext(), "isTranslated", "")
                        SharedDS().clearStr(requireContext(), "translatedDescr")
                        translationSwitcher.isUseMaterialThemeColors = true
                    }

                    viewModel.isTranslationEnabled = isChecked
                }

            }
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        //save to dataStore if user input is correct
        if (viewModel.email.isNotEmpty()){
            SharedDS().writeStr(requireContext(),"email", viewModel.email)
            SharedDS().writeStr(requireContext(),"token", "")
        }
        if (viewModel.password.isNotEmpty()){
            SharedDS().writeStr(requireContext(),"password", viewModel.password)
            SharedDS().writeStr(requireContext(),"token", "")
        }
        if (viewModel.passcode.length == 4 && viewModel.passcode.matches(Regex("\\d+"))){
            SharedDS().writeStr(requireContext(),"passcode", viewModel.passcode)
        }
    }
}