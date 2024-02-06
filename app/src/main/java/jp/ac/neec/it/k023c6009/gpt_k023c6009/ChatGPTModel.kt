package jp.ac.neec.it.k023c6009.gpt_k023c6009

//import jp.co.arpcorp.voicechatgpt.entities.ChatCompletion
//import jp.co.arpcorp.voicechatgpt.entities.Message
//import jp.co.arpcorp.voicechatgpt.entities.RequestData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executors

class ChatGPTModel {
    private val apiKey = "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"
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val modelName = "gpt-3.5-turbo"

    private val messages: MutableList<Message> = mutableListOf<Message>()

    fun sendData(text: String): String {
        // リクエストデータを生成
        val requestMessage = Message("user", text)

        // リクエストを連結
        messages.add(requestMessage)

        // リクエストデータを生成
        val requestData = RequestData(modelName, messages)

        // リクエストデータをJSON化
        val requestJson = Json.encodeToString(requestData)
        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestJson)

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()

        // 送信
        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        // レスポンスの取得
        if (response.isSuccessful) {
            val responseBody = response.body()?.string()

            val chatCompletion = Json.decodeFromString<ChatCompletion>(responseBody!!)

            // 回答の取得
            val responsMessage = chatCompletion.choices[0].message

            // 回答をリクエスト文に連結
            messages.add(responsMessage)

            return  responsMessage.content
        }

        return ""
    }
}