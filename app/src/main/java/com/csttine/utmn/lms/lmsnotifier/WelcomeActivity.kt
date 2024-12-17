package com.csttine.utmn.lms.lmsnotifier

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.welcome_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailKey = stringPreferencesKey("email")
        val passwordKey = stringPreferencesKey("password")
        var password = ""
        var email = ""
        val passcodeKey = stringPreferencesKey("passcode")
        var passcode = ""
        var passLen = 0

        //define 4 indicators
        val indicator1 = findViewById<View>(R.id.indicator1)
        val indicator2 = findViewById<View>(R.id.indicator2)
        val indicator3 = findViewById<View>(R.id.indicator3)
        val indicator4 = findViewById<View>(R.id.indicator4)
        val indicators = mutableListOf(indicator1, indicator2, indicator3, indicator4)
        val indicatorInitialColor = indicator1.backgroundTintList
        //define 11 buttons
        val butt0 = findViewById<Button>(R.id.button0)
        val butt1 = findViewById<Button>(R.id.button1)
        val butt2 = findViewById<Button>(R.id.button2)
        val butt3 = findViewById<Button>(R.id.button3)
        val butt4 = findViewById<Button>(R.id.button4)
        val butt5 = findViewById<Button>(R.id.button5)
        val butt6 = findViewById<Button>(R.id.button6)
        val butt7 = findViewById<Button>(R.id.button7)
        val butt8 = findViewById<Button>(R.id.button8)
        val butt9 = findViewById<Button>(R.id.button9)
        val buttBackspace = findViewById<ImageButton>(R.id.buttonBackspace)
        val buttons = setOf ( butt0, butt1, butt2, butt3, butt4, butt5, butt6, butt7, butt8, butt9, buttBackspace)
        //define input fields
        val emailEdit = findViewById<TextInputEditText>(R.id.emailEdit)
        val passwordEdit = findViewById<TextInputEditText>(R.id.passwordEdit)
        val emailField = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordField = findViewById<TextInputLayout>(R.id.passwordLayout)

        fun disableUserInput(){
            for (i in buttons){
                i.isEnabled = false}}

        fun fillIndicator(color:Int, border:Int){
            for (i in 0..border) {
                indicators[i].backgroundTintList = ContextCompat.getColorStateList(this, color)}}

        fun passcodeProceed(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // Start MainActivity
            finish()}

        fun pressPasscodeButton(number:String) {
            GlobalScope.launch (Dispatchers.Main){
                passcode += number
                passLen += 1
                fillIndicator(R.color.utmn, passLen-1)

                if (passLen == 4) {
                    disableUserInput()
                    delay(150)
                    dataStore.edit { prefs ->
                        prefs[passcodeKey] = passcode }
                    dataStore.edit { prefs ->
                        prefs[emailKey] = email }
                    dataStore.edit { prefs ->
                        prefs[passwordKey] = password }

                    passcodeProceed()}
            }
        }

        butt0.setOnClickListener{
            pressPasscodeButton("0") }
        butt1.setOnClickListener{
            pressPasscodeButton("1") }
        butt2.setOnClickListener{
            pressPasscodeButton("2") }
        butt3.setOnClickListener{
            pressPasscodeButton("3") }
        butt4.setOnClickListener{
            pressPasscodeButton("4") }
        butt5.setOnClickListener{
            pressPasscodeButton("5") }
        butt6.setOnClickListener{
            pressPasscodeButton("6") }
        butt7.setOnClickListener{
            pressPasscodeButton("7") }
        butt8.setOnClickListener{
            pressPasscodeButton("8") }
        butt9.setOnClickListener{
            pressPasscodeButton("9") }
        buttBackspace.setOnClickListener{
            if (passLen > 0){
                passLen -= 1
                passcode = passcode.dropLast(1)
                indicators[passLen].backgroundTintList = indicatorInitialColor}
        }
        emailEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()){
                    emailField.error = getString(R.string.inputRequired)
                }
                else{
                    emailField.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {email = s.toString()}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
        passwordEdit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()){
                    passwordField.error = getString(R.string.inputRequired)
                }
                else{
                    passwordField.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {password = s.toString()}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }
}