package jp.ac.neec.it.k023c6009.gpt_k023c6009

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.speech.tts.TextToSpeech
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var chatGptModel: ChatGPTModel

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        initialize()
    }

    //マニフェストで指定されたすべての権限が付与されているかどうかを確認します
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    private fun initialize() {
        chatGptModel = ChatGPTModel()

        // リクエストの初期データを作成
        val initText = """
                        あなたは女性アイドルです。
                        あなたは私と会話をしている、
                        アイドルとして振る舞ってください。
                        私の発言に対して、1回に1つずつ回答します。
                        説明は書かないでください。まとめて会話内容を書かないでください。
                        口調は、口語体で、親しく、可愛く、
                        すごく短い、必ず50文字以内の文章にしてください。
                        あなたの最初の発言は「どうしたの？」です。
                        """

        launch(Dispatchers.IO) {
            var responseText = chatGptModel.sendData(initText)

            withContext(Dispatchers.Main) {
                if (responseText == "") {
                    responseText = "通信に失敗しました。"
                }

                // 認識したテキストを画面に表示
                val messageText: TextView = findViewById(R.id.messageText)
                messageText.text = responseText

                TextToSpeech(responseText)
            }
        }
    }

    // 音声合成
    private fun TextToSpeech(text: String) {
        val success = ::speechSuccess
        val error = ::speechError
        SpeechUtil.textToSpeech(this, text, success, error)
    }

    // 音声認識
    private fun SpeechToText() {
        val success = ::recordSuccess
        val error = ::recordError
        SpeechUtil.speechToText(this, success, error)
    }

    // 音声合成成功コールバック
    private fun speechSuccess() {
        launch(Dispatchers.Main) {
            // 喋った後は聞く
            SpeechToText()
        }
    }

    // 音声合成失敗コールバック
    private fun speechError() {
        launch(Dispatchers.Main) {
            // 喋るのを失敗しても、とりあえず聞く
            SpeechToText()
        }
    }

    // 音声認識成功コールバック
    private fun recordSuccess(text: String) {
        // 認識したテキストを画面に表示
        val messageText: TextView = findViewById(R.id.messageText)
        messageText.text = text

        if (text == "") {
            // もう一度聞く
            SpeechToText()
        } else if (text.contains("話変わるんだけど")) {
            initialize()
        } else {
            launch(Dispatchers.IO) {
                // Chatに投げる
                var responseText = chatGptModel.sendData(text)

                withContext(Dispatchers.Main) {
                    if (responseText == "") {
                        responseText = "通信に失敗しました。"
                    }

                    // 認識したテキストを画面に表示
                    messageText.text = responseText

                    // 回答を喋る
                    TextToSpeech(responseText)
                }
            }
        }
    }

    // 音声認識失敗コールバック
    private fun recordError(error: Int) {
        // 再度聞く
        SpeechToText()
    }
}