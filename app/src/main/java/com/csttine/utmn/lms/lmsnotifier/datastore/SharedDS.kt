package com.csttine.utmn.lms.lmsnotifier.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SharedDS{

    fun get(context: Context, key: String) :String{
        return runBlocking { context.dataStore.data.first()[stringPreferencesKey(key)] ?: "" }
    }
    fun writeStr(context: Context, key: String, value: String){
        runBlocking { context.dataStore.edit { prefs -> prefs[stringPreferencesKey(key)] = value } }
    }
    fun getList(context: Context, key: String): MutableList<String>{
        return runBlocking {
            Json.decodeFromString((context.dataStore.data.first()[stringPreferencesKey(key)] ?: "[]"))
        }
    }
    fun writeList(context: Context, key: String, value: MutableList<String>){
        runBlocking {
            context.dataStore.edit { prefs ->
                prefs[stringPreferencesKey(key)] = Json.encodeToString(value)
            }
        }
    }
}