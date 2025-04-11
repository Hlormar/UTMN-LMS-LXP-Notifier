package com.csttine.utmn.lms.lmsnotifier.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SharedDS private constructor(private val applicationContext: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SharedDS? = null

        fun getInstance(context: Context): SharedDS =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedDS(context.applicationContext).also {
                    INSTANCE = it
                }
            }
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        applicationContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun get(key: String): String {
        return runBlocking { encryptedSharedPreferences.getString(key, "") ?: "" }
    }

    fun writeStr(key: String, value: String) {
        runBlocking {
            encryptedSharedPreferences.edit().putString(key, value).apply()
        }
    }

    fun clearStr(key: String) {
        runBlocking {
            encryptedSharedPreferences.edit().remove(key).apply()
        }
    }

    fun getList(key: String): MutableList<String> {
        return runBlocking {
            Json.decodeFromString((encryptedSharedPreferences.getString(key, "[]") ?: "[]"))
        }
    }

    fun writeList(key: String, value: MutableList<String>) {
        runBlocking {
            encryptedSharedPreferences.edit().putString(key, Json.encodeToString(value)).apply()
        }
    }
}