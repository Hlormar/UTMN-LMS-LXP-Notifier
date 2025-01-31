package com.csttine.utmn.lms.lmsnotifier

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.csttine.utmn.lms.lmsnotifier.fragments.ScheduleFragment
import com.csttine.utmn.lms.lmsnotifier.fragments.ScheduleViewModel
import com.csttine.utmn.lms.lmsnotifier.fragments.SettingsFragment
import com.csttine.utmn.lms.lmsnotifier.fragments.SettingsFragmentViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private var selectedFragment = 0

    private fun makeCurrentFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        val navBar = findViewById<BottomNavigationView>(R.id.nav_bar)

        selectedFragment = savedInstanceState?.getInt("FragmentIndex") ?: 0
        if (savedInstanceState == null || selectedFragment == 0){
            navBar.selectedItemId = R.id.menu_schedule
            makeCurrentFragment(ScheduleFragment())
        }
        else{
            navBar.selectedItemId = R.id.menu_settings
            makeCurrentFragment(SettingsFragment())
        }


        navBar.setOnItemSelectedListener {
            when (it.itemId){
                R.id.menu_lock -> {
                    val intent = Intent(this, LockScreen::class.java)
                    startActivity(intent) // Start the new activity
                    finish()
                }
                R.id.menu_schedule -> {
                    makeCurrentFragment(ScheduleFragment())
                    //uncomment if want the schedule to reload on reselection
                    /*ScheduleViewModel.isParsed = false
                    ScheduleViewModel.dataTemp.postValue(listOf())*/
                    SettingsFragmentViewModel.isFirstCreation.value = true
                    selectedFragment = 0}
                R.id.menu_settings -> {
                    ScheduleViewModel.isParsed = false
                    ScheduleViewModel.dataTemp.postValue(listOf()) //resetting data, forcing loadingAnimation to appear
                    makeCurrentFragment(SettingsFragment())
                    selectedFragment = 1}
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("FragmentIndex", selectedFragment)
    }
}