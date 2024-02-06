package jp.ac.neec.it.k023c6009.gpt_k023c6009

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class SpeechUtil {
    companion object {
        private val TAG = "SpeechUtil"
        private var speech: TextToSpeech? = null

        /**
         * 音声認識
         */
        fun speechToText(context: Context, recordFunc: (text: String) -> Unit, errorFunc: (errorCode: Int) -> Unit) {
            val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "onReadyForSpeech")
                }
                override fun onRmsChanged(rmsdB: Float) {
                    Log.d(TAG, "onRmsChanged")
                }
                override fun onBufferReceived(buffer: ByteArray?) {
                    Log.d(TAG, "onBufferReceived")
                }
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "onBeginningOfSpeech")
                }
                override fun onEndOfSpeech() {
                    Log.d(TAG, "onEndOfSpeech")
                }
                override fun onError(error: Int) {
                    var errorCode = ""
                    when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> errorCode = "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> errorCode = "Other client side errors"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> errorCode = "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> errorCode = "Network related errors"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> errorCode = "Network operation timed out"
                        SpeechRecognizer.ERROR_NO_MATCH -> errorCode = "No recognition result matched"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> errorCode = "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> errorCode = "Server sends error status"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> errorCode = "No speech input"
                    }
                    Log.d("RecognitionListener", "onError:$errorCode")

                    speechRecognizer.destroy()

                    errorFunc(error)
                }
                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.d(TAG, "onEvent")
                }
                override fun onPartialResults(partialResults: Bundle) {
                    Log.d(TAG, "onPartialResults")
                    val result = partialResults.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.i(TAG, "onPartialResults" + result.toString())
                }
                override fun onResults(results: Bundle) {
                    Log.d(TAG, "onResults")
                    val result = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.i(TAG, "onResults" + result.toString())

                    var text = ""
                    if (result?.size != 0) {
                        text = result?.get(0).toString()
                    }
                    speechRecognizer.destroy()
                    recordFunc(text)
                }
            })

            speechRecognizer.startListening(speechRecognizerIntent)
        }

        /**
         * 音声合成
         */
        fun textToSpeech(context: Context, speechText: String, successFunc:() -> Unit, errorFunc:() -> Unit) {
            speech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "onInit:SUCCESS")

                    val locale = Locale.JAPAN
                    if (speech!!.isLanguageAvailable(locale) > TextToSpeech.LANG_AVAILABLE) {
                        speech?.language = Locale.JAPANESE
                        speech?.setPitch(1.4f)

                        val listener = object : UtteranceProgressListener(){
                            override fun onDone(utteranceId: String?) {
                                Log.d(TAG,"onDone")
                                speech?.shutdown()
                                speech = null

                                successFunc()
                            }
                            override fun onError(utteranceId: String?) {
                                Log.d(TAG,"onError:$utteranceId")
                                speech?.shutdown()
                                speech = null

                                errorFunc()
                            }
                            override fun onError(utteranceId: String?, errorCode: Int) {
                                Log.d(TAG,"onError:$utteranceId, $errorCode")
                                speech?.shutdown()
                                speech = null

                                errorFunc()
                            }
                            override fun onStart(utteranceId: String?) {
                                Log.d(TAG,"onStart:$utteranceId")
                            }
                            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                                Log.d(TAG,"onStop:$utteranceId, $interrupted")
                                speech?.shutdown()
                                speech = null

                                successFunc()
                            }
                            override fun onBeginSynthesis(utteranceId: String?, sampleRateInHz: Int, audioFormat: Int, channelCount: Int) {
                                Log.d(TAG,"onBeginSynthesis:$utteranceId, $sampleRateInHz, $audioFormat, $channelCount")
                            }
                            override fun onAudioAvailable(utteranceId: String?, audio: ByteArray?) {
                                Log.d(TAG,"onAudioAvailable:$utteranceId")
                            }
                        }
                        // イベントリスナを登録
                        speech?.setOnUtteranceProgressListener(listener)

                        speech?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
                    } else {
                        // 言語の設定に失敗
                        Log.d(TAG, "言語の設定に失敗。")
                    }
                } else {
                    Log.d(TAG, "onInit:$status")
                }
            }
        }
    }
}