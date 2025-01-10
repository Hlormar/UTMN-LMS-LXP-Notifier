package com.csttine.utmn.lms.lmsnotifier.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingsFragment : Fragment() {

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


        //get values
        emailEdit.setText(SharedDS.get(requireContext(), "email"))
        passwordEdit.setText(SharedDS.get(requireContext(), "password"))
        passcodeEdit.setText(SharedDS.get(requireContext(), "passcode"))


        //recieve values
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

        return view
    }

    override fun onStop() {
        super.onStop()
        super.onDestroy()
        val tempEmail = requireView().findViewById<TextInputEditText>(R.id.emailEdit).text.toString()
        val tempPassword = requireView().findViewById<TextInputEditText>(R.id.passwordEdit).text.toString()
        val tempPasscode = requireView().findViewById<TextInputEditText>(R.id.passcodeEdit).text.toString()

        if (tempEmail.isNotEmpty()){
            SharedDS.writeStr(requireContext(),"email", tempEmail)
            SharedDS.writeStr(requireContext(),"token", "")
        }
        if (tempPassword.isNotEmpty()){
            SharedDS.writeStr(requireContext(),"password", tempPassword)
            SharedDS.writeStr(requireContext(),"token", "")
        }
        if (tempPasscode.length == 4 && tempPasscode.matches(Regex("\\d+"))){
            SharedDS.writeStr(requireContext(),"passcode", tempPasscode)
        }
    }
}