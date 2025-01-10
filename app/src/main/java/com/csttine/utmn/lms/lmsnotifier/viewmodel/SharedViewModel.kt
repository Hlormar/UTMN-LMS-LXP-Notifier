package com.csttine.utmn.lms.lmsnotifier.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csttine.utmn.lms.lmsnotifier.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedViewModel : ViewModel(){
    private val dataTemp = MutableLiveData<List<Any>>()
    val data : LiveData<List<Any>> = dataTemp

    fun asyncParse(context: Context){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {dataTemp.postValue(MainActivity.ParsingChores.parse(context))} }
    }
}