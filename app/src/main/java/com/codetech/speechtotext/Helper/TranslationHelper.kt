package com.codetech.speechtotext.Helper

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

class TranslationHelper(val context: Activity) {
    private var translationComplete: TranslationComplete? = null
    private var language = ""

    fun setTranslationComplete(translationComplete: TranslationComplete) {
        this.translationComplete = translationComplete
    }


    fun initTranslation(
        text: String,
        outputCode: String,
        inputCode: String
    ) {

        var result = "0"
        val onCompleteTranslateWord = CoroutineScope(Dispatchers.Default).launch {
            result = execute("manual", text, outputCode, inputCode)!!
        }
        onCompleteTranslateWord.invokeOnCompletion {
            CoroutineScope(Dispatchers.Main).launch {
                translationComplete?.translationCompleted(result, language)
            }
        }
    }

    private fun execute(
        type: String,
        inputText: String,
        outputCode: String,
        inputCode: String?
    ): String {
        return if (type == "auto")
            callUrlAndParseResult(
                inputText,
                outputCode
            )
        else callUrlAndParseResult(inputText, outputCode, inputCode!!)
    }

    private fun clearString(word: String): String {
        var text: String
        if (word.contains("&") || word.contains("\n")) {
            text = word.trim { it <= ' ' }.replace("&", "^~^")
            text = text.trim { it <= ' ' }.replace("%", "!^")
            text = text.trim { it <= ' ' }.replace("\n", "~~")
            text = text.trim { it <= ' ' }.replace("-", "")
            text = text.trim { it <= ' ' }.replace("#", "")
        } else {
            text = word
        }
        return text
    }

    private fun apiCall(url: String, isAuto: Boolean): String {
        Log.d("API Call URL", url)
        val response: StringBuffer
        val obj: URL
        try {
            obj = URL(url)
            val con = obj.openConnection() as HttpURLConnection
            con.setRequestProperty("User-Agent", "Mozilla/5.0")
            con.connect()

            if (con.responseCode != HttpURLConnection.HTTP_OK) {
                return "Error: ${con.responseCode} - ${con.responseMessage}"
            }

            val `in` = BufferedReader(InputStreamReader(con.inputStream))
            var inputLine: String?
            response = StringBuffer()
            while (`in`.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            `in`.close()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return "Oops. There was an error"
        } catch (e: IOException) {
            Log.e("API Error", e.toString())
            return "There seems to be a network issue!"
        }
        val outputString: String = parseResult(response.toString(), context, isAuto)
        return outputString.ifEmpty { "Fail to Translate" }
    }

    private fun callUrlAndParseResult(word: String, outputLanguageCode: String): String {
        val text = clearString(word)

        val url: String = "https://translate.googleapis.com/translate_a/single?client=gtx&" +
                "sl=auto&" +
                "tl=$outputLanguageCode&" +
                "dt=t&q=${URLEncoder.encode(text.trim(), "UTF-8")}&ie=UTF-8&oe=UTF-8"
        Log.e("for_translation", url)

        return apiCall(url, true)
    }

    private fun callUrlAndParseResult(
        word: String,
        outputLanguageCode: String,
        inputLanguageCode: String
    ): String {
        val text = clearString(word)
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&" +
                "sl=$inputLanguageCode&" +
                "tl=$outputLanguageCode&" +
                "dt=t&q=${URLEncoder.encode(text.trim(), "UTF-8")}&ie=UTF-8&oe=UTF-8"

        Log.d("uri", url)
        return apiCall(url, false)
    }


    private fun parseResult(inputJson: String, context: Context, isAuto: Boolean): String {
        Log.e("result", inputJson)
        val tempData = StringBuilder()
        var data = ""
        try {
            val jsonArray = JSONArray(inputJson)
            val jsonArray2 = jsonArray[0] as JSONArray
            if (isAuto) {
                val language = jsonArray[jsonArray.length() - 1] as JSONArray
                val lang = language[0] as JSONArray
                this.language = lang[0].toString()
            }
            Log.d("Language_", language)
            for (i in 0 until jsonArray2.length()) {
                val jsonArray3 = jsonArray2[i] as JSONArray
                tempData.append(jsonArray3[0].toString())
            }
            data = tempData.toString()
        } catch (e: JSONException) {
            Log.e("error_", e.toString())
        } catch (e: Exception) {
            Log.e("error_s", e.toString())
            (context as Activity).runOnUiThread {
                Toast.makeText(
                    context,
                    "Something went wrong", Toast.LENGTH_SHORT
                ).show()
            }
        }
        data = data.replace("~~ ", "\n")
        data = data.replace("~ ~ ", "\n")
        data = data.replace("~ ~", "\n")
        data = data.replace("~~", "\n")
        data = data.replace(" !^ ", "%")
        data = data.replace(" ! ^ ", "%")
        data = data.replace("! ^", "%")
        data = data.replace(" ^ ~ ^ ", "&")
        data = data.replace("^ ~ ^", "&")
        data = data.replace(" ^~^ ", "&")
        data = data.replace("^~^", "&")
        return data
    }

    interface TranslationComplete {
        fun translationCompleted(translation: String, language: String)
    }

}