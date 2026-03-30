package com.example.audioplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // 1. Declare UI Elements
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var seekBar: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView

    // 2. Declare MediaPlayer
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 3. Initialize Views
        btnPlay = findViewById(R.id.btnPlay)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)
        seekBar = findViewById(R.id.seekBarProgress)
        txtCurrentTime = findViewById(R.id.txtCurrentTime)
        txtTotalTime = findViewById(R.id.txtTotalTime)

        // 4. Initialize MediaPlayer with raw resource
        // Make sure you have 'sample_audio.mp3' in res/raw folder
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)

        // Set SeekBar Max Duration
        mediaPlayer?.let {
            seekBar.max = it.duration
            txtTotalTime.text = formatTime(it.duration)
        }

        // 5. Button Click Listeners
        btnPlay.setOnClickListener {
            playAudio()
        }

        btnPause.setOnClickListener {
            pauseAudio()
        }

        btnStop.setOnClickListener {
            stopAudio()
        }

        // 6. SeekBar Change Listener (Dragging the bar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // --- Logic Functions ---

    private fun playAudio() {
        if (mediaPlayer != null) {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
                Toast.makeText(this, "Playing...", Toast.LENGTH_SHORT).show()
                startSeekBarUpdate()
            }
        }
    }

    private fun pauseAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
                stopSeekBarUpdate()
            }
        }
    }

    private fun stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.prepare() // Reset state
            mediaPlayer!!.seekTo(0) // Go to start
            seekBar.progress = 0
            txtCurrentTime.text = formatTime(0)
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show()
            stopSeekBarUpdate()
        }
    }

    // --- SeekBar Logic ---

    private fun startSeekBarUpdate() {
        runnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        val progress = it.currentPosition
                        seekBar.progress = progress
                        txtCurrentTime.text = formatTime(progress)
                        handler.postDelayed(this, 1000) // Update every 1 second
                    }
                }
            }
        }
        handler.post(runnable!!)
    }

    private fun stopSeekBarUpdate() {
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
        }
    }

    // --- Helper: Format Time (mm:ss) ---
    private fun formatTime(milliseconds: Int): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong()))
        )
    }

    // --- Lifecycle: Release Resources (Important for Storage/Memory) ---
    override fun onPause() {
        super.onPause()
        // Optional: Pause music when app goes to background
        // pauseAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        stopSeekBarUpdate()
    }
}