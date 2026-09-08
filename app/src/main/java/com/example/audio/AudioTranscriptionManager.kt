package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class LiveAudioState(
  val isListening: Boolean = false,
  val activeTranscript: String = "Tap mic to stream voice commentary...",
  val aiCommentary: String = "🎙️ AI Caster: Standing by for live audio feed!",
  val audioWaveLevels: List<Float> = listOf(0.2f, 0.4f, 0.6f, 0.3f, 0.7f, 0.5f, 0.2f, 0.4f),
  val isStreaming: Boolean = false,
  val error: String? = null
)

class AudioTranscriptionManager(
  private val context: Context,
  private val scope: CoroutineScope
) {
  private val _state = MutableStateFlow(LiveAudioState())
  val state: StateFlow<LiveAudioState> = _state.asStateFlow()

  private var speechRecognizer: SpeechRecognizer? = null
  private var audioWaveJob: Job? = null
  private var streamingJob: Job? = null

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  init {
    initSpeechRecognizer()
  }

  private fun initSpeechRecognizer() {
    try {
      if (SpeechRecognizer.isRecognitionAvailable(context)) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
          setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
              _state.value = _state.value.copy(
                isListening = true,
                activeTranscript = "Listening to audio feed..."
              )
              startWaveformAnimation()
            }

            override fun onBeginningOfSpeech() {
              _state.value = _state.value.copy(activeTranscript = "Voice detected...")
            }

            override fun onRmsChanged(rmsdB: Float) {
              val normalized = (rmsdB / 10f).coerceIn(0.1f, 1.0f)
              updateWaveform(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
              stopWaveformAnimation()
            }

            override fun onError(error: Int) {
              stopWaveformAnimation()
              _state.value = _state.value.copy(
                isListening = false,
                activeTranscript = "Audio feed idle."
              )
            }

            override fun onResults(results: Bundle?) {
              stopWaveformAnimation()
              val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
              val recognized = matches?.firstOrNull() ?: ""
              if (recognized.isNotBlank()) {
                streamTranscript(recognized)
              } else {
                _state.value = _state.value.copy(isListening = false)
              }
            }

            override fun onPartialResults(partialResults: Bundle?) {
              val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
              val partial = matches?.firstOrNull() ?: ""
              if (partial.isNotBlank()) {
                _state.value = _state.value.copy(activeTranscript = partial)
              }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
          })
        }
      }
    } catch (e: Exception) {
      // SpeechRecognizer initialization fallback
    }
  }

  fun toggleListening() {
    if (_state.value.isListening) {
      stopListening()
    } else {
      startListening()
    }
  }

  fun startListening() {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    try {
      speechRecognizer?.startListening(intent)
      _state.value = _state.value.copy(isListening = true, activeTranscript = "Connecting live feed...")
      startWaveformAnimation()
    } catch (e: Exception) {
      // Fallback: Trigger dynamic voice prompt simulation with real Gemini streaming
      simulateVoiceStream("Superpower Ludo live action!")
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (ignored: Exception) {}
    stopWaveformAnimation()
    _state.value = _state.value.copy(isListening = false)
  }

  /**
   * Broadcasts a voice action or quick command (e.g. from UI buttons or user speech)
   * and processes it via Gemini for live transcription and commentator analysis!
   */
  fun broadcastVoiceCommand(phrase: String) {
    simulateVoiceStream(phrase)
  }

  private fun simulateVoiceStream(phrase: String) {
    streamingJob?.cancel()
    streamingJob = scope.launch {
      _state.value = _state.value.copy(
        isListening = true,
        isStreaming = true,
        activeTranscript = ""
      )
      startWaveformAnimation()

      // Stream words smoothly in real-time
      val words = phrase.split(" ")
      var accumulator = ""
      for (word in words) {
        accumulator = if (accumulator.isEmpty()) word else "$accumulator $word"
        _state.value = _state.value.copy(activeTranscript = "$accumulator...")
        delay(160)
      }
      _state.value = _state.value.copy(
        activeTranscript = "\"$phrase\"",
        isListening = false
      )
      stopWaveformAnimation()

      // Request live commentary / reaction via Gemini API
      requestGeminiLiveCommentary(phrase)
    }
  }

  private fun streamTranscript(text: String) {
    scope.launch {
      _state.value = _state.value.copy(
        activeTranscript = "\"$text\"",
        isListening = false
      )
      requestGeminiLiveCommentary(text)
    }
  }

  private suspend fun requestGeminiLiveCommentary(userSpeech: String) = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      // Graceful local AI caster reaction when API key is unconfigured
      val cannedReactions = listOf(
        "🎙️ AI Caster: \"$userSpeech\" — What a bold strategy! The board is heating up!",
        "🎙️ AI Caster: \"$userSpeech\" — Electric momentum on the track!",
        "🎙️ AI Caster: \"$userSpeech\" — Runes are pulsing! Big moves ahead!"
      )
      _state.value = _state.value.copy(
        aiCommentary = cannedReactions.random(),
        isStreaming = false
      )
      return@withContext
    }

    try {
      val prompt = "You are an enthusiastic, funny live esports commentator for a fast-paced Super Ludo game. The player just shouted: \"$userSpeech\". Give a super brief, 1-sentence reaction (under 15 words) with emojis."
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

      val jsonBody = JSONObject().apply {
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply {
                put("text", prompt)
              })
            })
          })
        })
      }

      val request = Request.Builder()
        .url(url)
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = httpClient.newCall(request).execute()
      if (response.isSuccessful) {
        val responseText = response.body?.string() ?: ""
        val json = JSONObject(responseText)
        val candidate = json.optJSONArray("candidates")?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val text = content?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""
        if (text.isNotBlank()) {
          _state.value = _state.value.copy(
            aiCommentary = "🎙️ AI Caster: ${text.trim()}",
            isStreaming = false
          )
        }
      }
    } catch (e: Exception) {
      _state.value = _state.value.copy(
        aiCommentary = "🎙️ AI Caster: \"$userSpeech\" — The dice have spoken!",
        isStreaming = false
      )
    }
  }

  private fun startWaveformAnimation() {
    audioWaveJob?.cancel()
    audioWaveJob = scope.launch {
      while (isActive) {
        val randomizedLevels = List(8) { Random.nextFloat().coerceIn(0.15f, 0.95f) }
        _state.value = _state.value.copy(audioWaveLevels = randomizedLevels)
        delay(120)
      }
    }
  }

  private fun updateWaveform(level: Float) {
    val levels = List(8) { (level * Random.nextFloat()).coerceIn(0.1f, 1.0f) }
    _state.value = _state.value.copy(audioWaveLevels = levels)
  }

  private fun stopWaveformAnimation() {
    audioWaveJob?.cancel()
    audioWaveJob = null
    _state.value = _state.value.copy(
      audioWaveLevels = listOf(0.15f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f)
    )
  }

  fun destroy() {
    try {
      speechRecognizer?.destroy()
    } catch (ignored: Exception) {}
  }
}
