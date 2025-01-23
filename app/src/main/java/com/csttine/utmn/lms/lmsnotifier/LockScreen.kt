package com.csttine.utmn.lms.lmsnotifier

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockScreenViewModel : ViewModel(){
    var passcode = ""
    var passLen = 0
}

class LockScreen : AppCompatActivity() {

    private lateinit var viewModel : LockScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.lock_screen)

        viewModel = ViewModelProvider(this)[LockScreenViewModel::class.java]
        var passcode = viewModel.passcode
        var passLen = viewModel.passLen
        val exactPass = SharedDS().get(this, "passcode")

        //vibrator
        val vibrator = getSystemService("vibrator") as Vibrator
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

        fun enableUserInput(){
            for (i in buttons){
                i.isClickable = true}}

        fun disableUserInput(){
            for (i in buttons){
                i.isClickable = false}}

        fun fillIndicator(color:Int, border:Int){
            for (i in 0..border) {
                indicators[i].backgroundTintList = ContextCompat.getColorStateList(this, color)}}

        fun passcodeProceed(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // Start MainActivity
            finish()}

        fun pressPasscodeButton(number:String) {
            passcode += number
            passLen += 1
            //kinda cache
            viewModel.passcode = passcode
            viewModel.passLen = passLen
            fillIndicator(R.color.utmn, passLen-1)

            if (passLen == 4) {
                disableUserInput()
                if (passcode == exactPass) {
                    GlobalScope.launch (Dispatchers.Main) {
                        delay(150)
                        enableUserInput()
                        passcodeProceed()
                    }
                }
                else {
                    //reset
                    passLen = 0
                    passcode = ""
                    viewModel.passLen = 0
                    viewModel.passcode = ""
                    fillIndicator(R.color.error, 3)

                    if (vibrator.hasVibrator()) {
                        vibrator.cancel()
                        val effect = VibrationEffect.createOneShot(100, 1)
                        vibrator.vibrate(effect)}

                    GlobalScope.launch (Dispatchers.Main) {
                        delay(350)
                        for (i in 0..3) {
                            indicators[i].backgroundTintList = indicatorInitialColor}
                        enableUserInput()
                    }
                }
            }
        }

        //restore indicators after screen rotation or theme change
        fillIndicator(R.color.utmn, passLen-1)

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
                indicators[passLen].backgroundTintList = indicatorInitialColor
            }
        }

    }
}