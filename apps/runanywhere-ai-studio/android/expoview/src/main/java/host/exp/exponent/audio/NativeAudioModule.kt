package host.exp.exponent.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

/**
 * Native Audio Module for recording WAV audio optimized for Whisper STT.
 * Records 16kHz mono 16-bit PCM audio.
 * 
 * NOTE: This module should ideally be part of @runanywhere/core SDK
 * but is currently provided by the host app. Future SDK versions
 * should bundle this natively.
 */
@ReactModule(name = NativeAudioModule.NAME)
class NativeAudioModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "NativeAudioModule"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private var audioBuffer: ByteArrayOutputStream? = null
    private var recordingPath: String? = null
    private var currentAudioLevel: Float = 0f
    
    // Playback
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun getName(): String = NAME

    // MARK: - Recording

    @ReactMethod
    fun startRecording(promise: Promise) {
        try {
            // Check permission
            if (ContextCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                promise.reject("PERMISSION_DENIED", "Microphone permission not granted")
                return
            }

            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                promise.reject("INIT_ERROR", "Failed to initialize AudioRecord")
                return
            }

            audioBuffer = ByteArrayOutputStream()
            isRecording = true
            
            // Create temp file path
            val cacheDir = reactApplicationContext.cacheDir
            recordingPath = File(cacheDir, "recording_${System.currentTimeMillis()}.wav").absolutePath

            audioRecord?.startRecording()

            // Start recording thread
            recordingThread = thread {
                val buffer = ShortArray(bufferSize / 2)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // Convert shorts to bytes
                        val byteBuffer = ByteBuffer.allocate(read * 2)
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        for (i in 0 until read) {
                            byteBuffer.putShort(buffer[i])
                        }
                        audioBuffer?.write(byteBuffer.array())

                        // Calculate audio level (RMS)
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = Math.sqrt(sum / read)
                        currentAudioLevel = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                    }
                }
            }

            // SDK expects { path: string } from startRecording
            val result = Arguments.createMap().apply {
                putString("path", recordingPath)
            }
            promise.resolve(result)

        } catch (e: Exception) {
            promise.reject("START_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun stopRecording(promise: Promise) {
        try {
            isRecording = false
            recordingThread?.join(1000)
            recordingThread = null

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            val pcmData = audioBuffer?.toByteArray() ?: ByteArray(0)
            audioBuffer = null

            if (pcmData.isEmpty()) {
                promise.reject("NO_DATA", "No audio data recorded")
                return
            }

            // Write WAV file
            val wavData = createWavFile(pcmData)
            File(recordingPath!!).writeBytes(wavData)

            // SDK expects { path: string } from stopRecording
            val result = Arguments.createMap().apply {
                putString("path", recordingPath)
            }
            promise.resolve(result)

        } catch (e: Exception) {
            promise.reject("STOP_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun cancelRecording(promise: Promise) {
        try {
            isRecording = false
            recordingThread?.join(1000)
            recordingThread = null

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            audioBuffer = null

            recordingPath?.let { File(it).delete() }
            recordingPath = null

            val result = Arguments.createMap().apply {
                putBoolean("success", true)
            }
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("CANCEL_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getAudioLevel(promise: Promise) {
        val result = Arguments.createMap().apply {
            putDouble("level", currentAudioLevel.toDouble())
        }
        promise.resolve(result)
    }

    // MARK: - Playback

    @ReactMethod
    fun playAudio(uri: String, promise: Promise) {
        try {
            // Release any existing player
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer().apply {
                // Handle file:// URLs and plain paths
                val path = when {
                    uri.startsWith("file://") -> uri.removePrefix("file://")
                    else -> uri
                }
                setDataSource(path)
                prepare()
                start()
            }
            isPlaying = true

            val result = Arguments.createMap().apply {
                putBoolean("success", true)
                putDouble("duration", (mediaPlayer?.duration ?: 0) / 1000.0)
            }
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("PLAYBACK_ERROR", "Failed to play audio: ${e.message}", e)
        }
    }

    @ReactMethod
    fun stopPlayback(promise: Promise) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            
            val result = Arguments.createMap().apply {
                putBoolean("success", true)
            }
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("STOP_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun pausePlayback(promise: Promise) {
        try {
            mediaPlayer?.pause()
            isPlaying = false
            
            val result = Arguments.createMap().apply {
                putBoolean("success", true)
            }
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("PAUSE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun resumePlayback(promise: Promise) {
        try {
            mediaPlayer?.start()
            isPlaying = true
            
            val result = Arguments.createMap().apply {
                putBoolean("success", true)
            }
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("RESUME_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getPlaybackStatus(promise: Promise) {
        val result = Arguments.createMap().apply {
            putBoolean("isPlaying", mediaPlayer?.isPlaying ?: false)
            putDouble("currentTime", (mediaPlayer?.currentPosition ?: 0) / 1000.0)
            putDouble("duration", (mediaPlayer?.duration ?: 0) / 1000.0)
        }
        promise.resolve(result)
    }

    // MARK: - Helpers

    private fun createWavFile(pcmData: ByteArray): ByteArray {
        val totalDataLen = pcmData.size + 36
        val totalAudioLen = pcmData.size
        val byteRate = SAMPLE_RATE * 1 * 16 / 8

        val header = ByteBuffer.allocate(44)
        header.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        header.put("RIFF".toByteArray())
        header.putInt(totalDataLen)
        header.put("WAVE".toByteArray())

        // fmt subchunk
        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size for PCM
        header.putShort(1) // AudioFormat (1 = PCM)
        header.putShort(1) // NumChannels (mono)
        header.putInt(SAMPLE_RATE) // SampleRate
        header.putInt(byteRate) // ByteRate
        header.putShort(2) // BlockAlign
        header.putShort(16) // BitsPerSample

        // data subchunk
        header.put("data".toByteArray())
        header.putInt(totalAudioLen)

        return header.array() + pcmData
    }
}
