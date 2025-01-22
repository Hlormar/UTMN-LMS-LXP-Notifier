package com.csttine.utmn.lms.lmsnotifier.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.fragments.SettingsFragmentViewModel.Companion.isFirstCreation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingsFragmentViewModel : ViewModel(){
    companion object {
        var isFirstCreation = MutableLiveData<Boolean>(true)
    }
    var email = ""
    var password = ""
    var passcode = ""
    var passwordEditInputType = (InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT)
    var passcodeEditInputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
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

        //restore field values after rotation or load default
        if (isFirstCreation.value == true) {
            isFirstCreation.value = false
            Log.d("RESTORE FROM DS", viewModel.email)
            emailEdit.setText(SharedDS().get(requireContext(), "email"))
            passwordEdit.setText(SharedDS().get(requireContext(), "password"))
            passcodeEdit.setText(SharedDS().get(requireContext(), "passcode"))
        }
        else{
            Log.d("RESTORE FROM VIEWMODEL", viewModel.email)
            emailEdit.setText(viewModel.email)
            passwordEdit.setText(viewModel.password)
            passcodeEdit.setText(viewModel.passcode)
            passwordEdit.inputType = viewModel.passwordEditInputType
            passcodeEdit.inputType = viewModel.passcodeEditInputType
        }


        //listen
        emailEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()){
                    emailField.error = getString(R.string.inputRequired)
                }
                else{
                    emailField.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        passwordEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()){
                    passwordField.error = getString(R.string.inputRequired)
                }
                else{
                    passwordField.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        passcodeEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()){
                    passcodeField.error = getString(R.string.inputRequired)
                }
                else if (!s.matches(Regex("\\d+"))){
                    passcodeField.error = "*Unacceptable characters"
                }
                else{
                    passcodeField.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //Input type save & restore
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

        return view
    }

    override fun onStop() {
        super.onStop()
        val tempEmail = requireView().findViewById<TextInputEditText>(R.id.emailEdit).text.toString()
        val tempPassword = requireView().findViewById<TextInputEditText>(R.id.passwordEdit).text.toString()
        val tempPasscode = requireView().findViewById<TextInputEditText>(R.id.passcodeEdit).text.toString()

        //save to viewModel to restore after rotation
        Log.d("SAVE TO VIEWMODEL", tempEmail)
        Log.d("PASSWORD END ICON", requireView().findViewById<TextInputEditText>(R.id.passwordEdit).inputType.toString())
        viewModel.email = tempEmail
        viewModel.password = tempPassword
        viewModel.passcode = tempPasscode

        //save to dataStore if user input is correct
        if (tempEmail.isNotEmpty()){
            SharedDS().writeStr(requireContext(),"email", tempEmail)
            SharedDS().writeStr(requireContext(),"token", "")
        }
        if (tempPassword.isNotEmpty()){
            SharedDS().writeStr(requireContext(),"password", tempPassword)
            SharedDS().writeStr(requireContext(),"token", "")
        }
        if (tempPasscode.length == 4 && tempPasscode.matches(Regex("\\d+"))){
            SharedDS().writeStr(requireContext(),"passcode", tempPasscode)
        }
    }
}