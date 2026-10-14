package com.example.os.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object AudioSynth {
    private const val SAMPLE_RATE = 22050
    private var isMuted = false
    private var musicJob: Job? = null
    var isPlayingMusic = false
        private set

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) stopMusic()
    }

    fun playTone(freqHz: Double, durationMs: Int, volume: Float = 0.4f, type: ToneType = ToneType.SINE) {
        if (isMuted) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / SAMPLE_RATE
                    val sample = when (type) {
                        ToneType.SINE -> sin(2.0 * PI * freqHz * time)
                        ToneType.SQUARE -> if (sin(2.0 * PI * freqHz * time) >= 0) 0.8 else -0.8
                        ToneType.SAWTOOTH -> 2.0 * (time * freqHz - kotlin.math.floor(time * freqHz + 0.5))
                    }
                    // Apply attack/decay envelope to prevent clicking
                    val envelope = when {
                        i < numSamples * 0.1 -> i.toDouble() / (numSamples * 0.1)
                        i > numSamples * 0.8 -> (numSamples - i).toDouble() / (numSamples * 0.2)
                        else -> 1.0
                    }
                    val finalSample = (sample * envelope * volume * Short.MAX_VALUE).toInt()
                    buffer[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                delay(durationMs.toLong() + 50)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Ignore audio hardware errors
            }
        }
    }

    fun playClick() {
        playTone(1200.0, 15, 0.2f, ToneType.SINE)
    }

    fun playWindowOpen() {
        CoroutineScope(Dispatchers.Default).launch {
            playTone(440.0, 40, 0.25f, ToneType.SINE)
            delay(35)
            playTone(660.0, 50, 0.25f, ToneType.SINE)
            delay(45)
            playTone(880.0, 70, 0.25f, ToneType.SINE)
        }
    }

    fun playWindowClose() {
        CoroutineScope(Dispatchers.Default).launch {
            playTone(750.0, 35, 0.2f, ToneType.SINE)
            delay(30)
            playTone(420.0, 50, 0.2f, ToneType.SINE)
        }
    }

    fun playBeep() {
        playTone(520.0, 120, 0.35f, ToneType.SINE)
    }

    fun playError() {
        CoroutineScope(Dispatchers.Default).launch {
            playTone(220.0, 100, 0.4f, ToneType.SAWTOOTH)
            delay(90)
            playTone(180.0, 150, 0.45f, ToneType.SAWTOOTH)
        }
    }

    fun playNote(noteIndex: Int) {
        val baseFreq = 261.63 // Middle C (C4)
        // 12-TET scale frequency
        val freq = baseFreq * Math.pow(2.0, noteIndex / 12.0)
        playTone(freq, 250, 0.4f, ToneType.SINE)
    }

    fun startMusic(scope: CoroutineScope, onBeat: ((Float) -> Unit)? = null) {
        if (isMuted) return
        stopMusic()
        isPlayingMusic = true
        musicJob = scope.launch(Dispatchers.Default) {
            // Melodic lo-fi synth chord progression (Chords in C minor / Eb / Ab / Bb)
            val melody = intArrayOf(
                0, 3, 7, 10,  // Cm7 arpeggio
                -2, 2, 5, 8,  // Bb
                -4, 0, 3, 7,  // Ab
                3, 7, 10, 14, // Eb
                0, 7, 12, 10,
                -2, 5, 10, 8,
                -4, 3, 8, 7,
                -1, 2, 7, 11
            )
            var index = 0
            while (isActive && isPlayingMusic) {
                val note = melody[index % melody.size]
                val baseFreq = 261.63
                val freq = baseFreq * Math.pow(2.0, note / 12.0)
                val duration = if (index % 4 == 0) 180 else 120
                playTone(freq, duration, 0.22f, ToneType.SINE)
                onBeat?.invoke((index % 8) / 8f)
                index++
                delay(220)
            }
        }
    }

    fun stopMusic() {
        isPlayingMusic = false
        musicJob?.cancel()
        musicJob = null
    }

    enum class ToneType {
        SINE, SQUARE, SAWTOOTH
    }
}
