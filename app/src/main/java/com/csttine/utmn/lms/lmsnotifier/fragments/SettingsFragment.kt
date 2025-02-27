package com.csttine.utmn.lms.lmsnotifier.fragments

import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.fragments.SettingsFragmentViewModel.Companion.isFirstCreation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragmentViewModel : ViewModel(){
    companion object {
        var isFirstCreation = MutableLiveData<Boolean>(true)
    }
    var email = ""
    var password = ""
    var passcode = ""
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

        viewModel = ViewModelProvider(requireActivity())[SettingsFragmentViewModel::class.java]

        //restore field values after rotation or load defaults
        if (isFirstCreation.value == true) {
            isFirstCreation.value = false
            viewModel.email = SharedDS().get(requireContext(), "email")
            viewModel.password = SharedDS().get(requireContext(), "password")
            viewModel.passcode = SharedDS().get(requireContext(), "passcode")
        }
        emailEdit.setText(viewModel.email)
        passwordEdit.setText(viewModel.password)
        passcodeEdit.setText(viewModel.passcode)
        passwordEdit.inputType = viewModel.passwordEditInputType
        passcodeEdit.inputType = viewModel.passcodeEditInputType
        emailField.error = viewModel.emailFieldError
        passwordField.error = viewModel.passwordFieldError
        passcodeField.error = viewModel.passcodeFieldError


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