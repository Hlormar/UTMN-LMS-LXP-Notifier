package com.csttine.utmn.lms.lmsnotifier.translator


import android.content.Context
import opennlp.tools.langdetect.LanguageDetectorME
import opennlp.tools.langdetect.LanguageDetectorModel
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import com.csttine.utmn.lms.lmsnotifier.R
import com.csttine.utmn.lms.lmsnotifier.datastore.SharedDS
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel


class Translator {
     fun detectLanguage(text: String): String {
        val modelInputStream = LanguageDetectorModel::class.java.getResourceAsStream("/opennlp/langdetect-183.bin")
        return LanguageDetectorME(LanguageDetectorModel(modelInputStream))
            .predictLanguage(text)
            .lang
    }


    fun splitRuTextIntoSentences(text: String): List<String> {
        val modelInputStream = SentenceModel::class.java.getResourceAsStream("/opennlp/opennlp-ru-ud-gsd-sentence-1.2-2.5.0.bin")
        val sentenceModel = SentenceModel(modelInputStream)
        val sentenceDetector = SentenceDetectorME(sentenceModel)
        return sentenceDetector.sentDetect(text).toList()
    }


    private fun getCorrectEmail(context: Context, email: String): String{
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Log.d("     getCorrectEmail", "wrong email $email")
            val emailNew = if (email.take(4).lowercase() == "stud") "$email@study.utmn.ru" // assume user is student
            else "$email@utmn.ru"
            Toast.makeText(context, context.getString(R.string.toast_incorrectEmail, email, emailNew), Toast.LENGTH_SHORT).show()
            return emailNew
        }
        else return email
    }

    // used in case the separate sentence too long. May result in missing of meaning
    private fun portionByWords(text: String, maxBytes: Int): List<String> {
        val result = mutableListOf<String>()
        val currentChunk = StringBuilder()
        var currentByteCount = 0

        // split by space
        for (word in text.split(" ")) {
            val wordBytes = word.toByteArray(Charsets.UTF_8).size

            // if appending exceeds the limit, save current chunk
            if (currentByteCount + wordBytes + 1 > maxBytes) { // +1 for space
                if (currentChunk.isNotEmpty()) {
                    result.add(currentChunk.toString().trim())
                    currentChunk.clear()
                    currentByteCount = 0
                }
            }

            // else just appending
            currentChunk.append(word).append(" ")
            currentByteCount += wordBytes + 1
        }

        // append last chunk if its not empty
        if (currentChunk.isNotEmpty()) {
            result.add(currentChunk.toString().trim())
        }

        return result
    }

    // used to separate text in <= 500B chunks (MyMemory request bottleneck)
    fun portionSentences(sentences: List<String>, maxBytes: Int = 500): List<String> {
        val result = mutableListOf<String>()

        for (sentence in sentences) {
            if (sentence.toByteArray(Charsets.UTF_8).size <= maxBytes) {
                result.add(sentence.trim())
                continue
            }

            // separate by words if sentence too long
            val parts = portionByWords(sentence, maxBytes)
            result.addAll(parts)
        }

        return result
    }


     suspend fun translateRuToEn(context: Context ,raw: String) : String{
        return withContext(Dispatchers.IO){
            try {
                val email = getCorrectEmail(context, SharedDS().get(context, "email"))

                //build an url
                val url = HttpUrl.parse("https://api.mymemory.translated.net/get").newBuilder()
                    .addQueryParameter("q", raw)
                    .addQueryParameter("langpair", "ru|en")
                    .addQueryParameter("de", email)  // providing email to expand usage limits
                    .build()
                    .toString()
                Log.d("     Translator build url", url)

                //build the request
                val request: Request = Request.Builder()
                    .url(url)
                    .build()

                //send the request
                val client = OkHttpClient()
                val response: Response = client.newCall(request).execute()

                //parse the JSON
                @Serializable
                data class ResponseData(
                    @SerialName("translatedText") val translatedText: String
                )
                @Serializable
                data class TranslationResponse(
                    @SerialName("responseData") val responseData: ResponseData,
                )

                // return the translation
                return@withContext  if (response.isSuccessful) {
                    Json.decodeFromString<TranslationResponse>(response.body().string()).responseData.translatedText
                } else (raw)


            }
            catch (e: Exception){
                Log.e("     Translator Error", "${e.message}", e)
                return@withContext raw
            }
        }
    }
}