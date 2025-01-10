package com.csttine.utmn.lms.lmsnotifier.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object SharedDS{
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    fun get(context: Context, key: String) :String{
        return runBlocking { context.dataStore.data.first()[stringPreferencesKey(key)] ?: "" }
    }
    fun writeStr(context: Context, key: String, value: String){
        runBlocking { context.dataStore.edit { prefs -> prefs[stringPreferencesKey(key)] = value } }
    }
    fun getList(context: Context, key: String) :MutableList<String>{
        return runBlocking {
            (context.dataStore.data.first()[stringSetPreferencesKey(key)]?.toMutableList()
                ?: mutableListOf())
        }
    }
    fun writeList(context: Context, key: String, value: MutableList<String>){
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[stringSetPreferencesKey(key)] = value.toSet()
            }
        }
    }
}