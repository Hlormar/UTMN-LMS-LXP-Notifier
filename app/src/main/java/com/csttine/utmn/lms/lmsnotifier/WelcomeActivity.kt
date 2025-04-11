package com.csttine.utmn.lms.lmsnotifier

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.csttine.utmn.lms.lmsnotifier.languageManager.ActivityBase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class WelcomeActivityViewModel : ViewModel() {
    var email = ""
    var password = ""
    var passcode = ""
    var passcodeLen = 0
    var isEmailEdited = false
    var isPasswordEdited = false
    var isPasswordVisible = false
}


class WelcomeActivity : ActivityBase() {

    private lateinit var viewModel : WelcomeActivityViewModel
    private val sharedDS by lazy {SharedDS.getInstance(LmsApp.appContext)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.welcome_screen)

        viewModel = ViewModelProvider(this)[WelcomeActivityViewModel::class.java]

        val vibrator = getSystemService("vibrator") as Vibrator
        val indicator1 = findViewById<View>(R.id.indicator1)
        val indicator2 = findViewById<View>(R.id.indicator2)
        val indicator3 = findViewById<View>(R.id.indicator3)
        val indicator4 = findViewById<View>(R.id.indicator4)
        val indicators = mutableListOf(indicator1, indicator2, indicator3, indicator4)
        val indicatorInitialColor = indicator1.backgroundTintList

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

        val emailEdit = findViewById<TextInputEditText>(R.id.emailEdit)
        val passwordEdit = findViewById<TextInputEditText>(R.id.passwordEdit)
        val emailField = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordField = findViewById<TextInputLayout>(R.id.passwordLayout)

        fun disableUserInput(){
            for (i in buttons){
                i.isClickable = false}}

        fun enableUserInput(){
            for (i in buttons){
                i.isClickable = true}}

        fun fillIndicator(color:Int, border:Int){
            for (i in 0..border) {
                indicators[i].backgroundTintList = ContextCompat.getColorStateList(this, color)}}

        fun passcodeProceed(){
            /*//init auto checks
            val sharedDS = SharedDS()

            val hour = sharedDS.get(this, "autoCheckStartHour").toIntOrNull() ?: 8
            //val amountOfChecks = sharedDS.get(this, "autoChecksAmount")
            lmsApp.enqueueAutoCheck(2, lmsApp.calcDelayUntil(hour, lmsApp.randomMinutes(0)))*/

            val autoCheckManager by lazy {AutoCheckManager()}
            val initScheduleAutoCheckRequest = OneTimeWorkRequest.Builder(WorkRuntime::class.java)
                .addTag("lms-initScheduler")
                .setInitialDelay(autoCheckManager.calcDelayUntil(0,0), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(this).enqueue(initScheduleAutoCheckRequest)
            Log.d("     WelcomeActivity", "enqueued auto check schedule work initializer: ${WorkManager.getInstance(this).getWorkInfosByTag("lms-initScheduler")}")
            autoCheckManager.scheduleAutoChecks() // for now


            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()}

        fun pressPasscodeButton(number:String) {
            viewModel.passcode += number
            viewModel.passcodeLen += 1
            fillIndicator(R.color.utmn, viewModel.passcodeLen-1)

            if (viewModel.passcodeLen == 4) {
                disableUserInput()
                var isAllFilled = true
                if (viewModel.password == ""){
                    isAllFilled = false
                    passwordField.error = getString(R.string.inputRequired)
                }
                if (viewModel.email == ""){
                    isAllFilled = false
                    emailField.error = getString(R.string.inputRequired)
                }

                if (isAllFilled){
                    sharedDS.writeStr("passcode", viewModel.passcode)
                    sharedDS.writeStr("email", viewModel.email)
                    sharedDS.writeStr("password", viewModel.password)
                    GlobalScope.launch (Dispatchers.Main){
                        delay(150)
                        passcodeProceed()
                    }
                }

                else{
                    viewModel.passcodeLen = 0
                    viewModel.passcode = ""
                    fillIndicator(R.color.error, 3)

                    if (vibrator.hasVibrator()) {
                        vibrator.cancel()
                        val effect = VibrationEffect.createOneShot(100, 1)
                        vibrator.vibrate(effect)}

                    GlobalScope.launch (Dispatchers.Main){
                        delay(350)
                        for (i in 0..3) {
                            indicators[i].backgroundTintList = indicatorInitialColor}
                        enableUserInput()
                    }
                }
            }
        }

        //Restore view after config changes
        emailEdit.setText(viewModel.email)
        passwordEdit.setText(viewModel.password)
        if (viewModel.isPasswordVisible){
            // using this because setting inputType directly triggers EndIconOnClick listener some why(
            passwordEdit.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        fillIndicator(R.color.utmn, viewModel.passcodeLen-1)

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
            if (viewModel.passcodeLen > 0){
                viewModel.passcodeLen -= 1
                viewModel.passcode = viewModel.passcode.dropLast(1)
                indicators[viewModel.passcodeLen].backgroundTintList = indicatorInitialColor}
        }

        //listen
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
            }
        })


        //Input type save & change
        passwordField.setEndIconOnClickListener{
            viewModel.isPasswordVisible = passwordEdit.inputType == (InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT)
            if (viewModel.isPasswordVisible){
                passwordEdit.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            } else{
                passwordEdit.inputType = (InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT)
            }
        }
    }

}
