package com.creativesit.roommanager.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * অ্যাপের ভেতরেই মাইক দিয়ে ভয়েস মেসেজ রেকর্ড করার হেল্পার ক্লাস।
 * ব্যবহার: startRecording() → (কিছুক্ষণ পর) stopRecording() যা রেকর্ড করা ফাইলটা রিটার্ন করে।
 */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): Boolean {
        return try {
            val file = File.createTempFile("voice_", ".m4a", context.cacheDir)
            outputFile = file

            @Suppress("DEPRECATION")
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()

            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mr
            true
        } catch (e: Exception) {
            recorder = null
            outputFile = null
            false
        }
    }

    /** রেকর্ডিং বন্ধ করে, সফল হলে রেকর্ড করা ফাইলটা রিটার্ন করে (ব্যর্থ হলে null) */
    fun stopRecording(): File? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            outputFile
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            null
        }
    }

    /** রেকর্ডিং বাতিল করে ফাইল মুছে দেয় (যেমন খুব কম সময়ের রেকর্ডিং বা ব্যবহারকারী বাতিল করলে) */
    fun cancelRecording() {
        try {
            recorder?.apply { stop(); release() }
        } catch (e: Exception) {
            recorder?.release()
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }

    fun isRecording(): Boolean = recorder != null
}
