package host.exp.exponent.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

/**
 * Native Audio Module for recording WAV audio optimized for Whisper STT.
 * Records 16kHz mono 16-bit PCM audio.
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

    override fun getName(): String = NAME

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

            val result = Arguments.createMap().apply {
                putString("path", recordingPath)
                putInt("sampleRate", SAMPLE_RATE)
                putInt("channels", 1)
                putInt("bitsPerSample", 16)
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

            // Also provide base64 of raw PCM for direct use
            val audioBase64 = Base64.encodeToString(pcmData, Base64.NO_WRAP)

            val result = Arguments.createMap().apply {
                putString("path", recordingPath)
                putString("audioBase64", audioBase64)
                putInt("fileSize", wavData.size)
                putInt("sampleRate", SAMPLE_RATE)
                putInt("channels", 1)
                putDouble("duration", pcmData.size.toDouble() / (SAMPLE_RATE * 2))
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

            promise.resolve(null)
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
