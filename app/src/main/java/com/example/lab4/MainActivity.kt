package com.example.lab4

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoView: VideoView
    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    private var isAudio = true
    private var isUsingExoPlayer = false

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (isAudio) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@MainActivity, it)
                    prepare()
                    start()
                }
                Toast.makeText(this, "Audio is playing", Toast.LENGTH_SHORT).show()
            } else {
                videoView.setVideoURI(it)
                videoView.start()
                Toast.makeText(this, "Video is playing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = findViewById(R.id.videoView)
        playerView = findViewById(R.id.exoPlayerView)
        val playButton = findViewById<Button>(R.id.btnPlay)
        val pauseButton = findViewById<Button>(R.id.btnPause)
        val stopButton = findViewById<Button>(R.id.btnStop)
        val selectFileButton = findViewById<Button>(R.id.btnSelectFile)
        val loadInternetButton = findViewById<Button>(R.id.btnLoadInternet)
        val switchTypeButton = findViewById<Button>(R.id.btnSwitchType)

        playButton.setOnClickListener { playMedia() }
        pauseButton.setOnClickListener { pauseMedia() }
        stopButton.setOnClickListener { stopMedia() }
        selectFileButton.setOnClickListener { pickMediaLauncher.launch(if (isAudio) "audio/*" else "video/*") }
        loadInternetButton.setOnClickListener { playFromInternet() }
        switchTypeButton.setOnClickListener {
            isAudio = !isAudio
            Toast.makeText(this, if (isAudio) "Audio Mode" else "Video Mode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playMedia() {
        if (isAudio) {
            if (!this::mediaPlayer.isInitialized) {
                mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)
            }

            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                Toast.makeText(this, "Audio is playing", Toast.LENGTH_SHORT).show()
            }
        } else {
            videoView.visibility = VideoView.VISIBLE
            playerView.visibility = PlayerView.GONE

            if (videoView.currentPosition == 0) {
                val uri = Uri.parse("android.resource://$packageName/${R.raw.sample_video}")
                videoView.setVideoURI(uri)
            }

            if (!videoView.isPlaying) {
                videoView.start()
                Toast.makeText(this, "Video is playing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pauseMedia() {
        if (isAudio && this::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            Toast.makeText(this, "Audio paused", Toast.LENGTH_SHORT).show()
        } else if (!isAudio && videoView.isPlaying) {
            videoView.pause()
            Toast.makeText(this, "Video paused", Toast.LENGTH_SHORT).show()
        } else if (isUsingExoPlayer && this::exoPlayer.isInitialized && exoPlayer.isPlaying) {
            exoPlayer.pause()
            Toast.makeText(this, "Video paused", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopMedia() {
        if (isAudio && this::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                Toast.makeText(this, "Audio stopped", Toast.LENGTH_SHORT).show()
            }
            mediaPlayer.release()
            mediaPlayer = MediaPlayer()
        } else if (!isAudio && videoView.isPlaying) {
            videoView.stopPlayback()
            videoView.resume()
            Toast.makeText(this, "Video stopped", Toast.LENGTH_SHORT).show()
        } else if (isUsingExoPlayer && this::exoPlayer.isInitialized) {
            exoPlayer.stop()
            exoPlayer.release()
            isUsingExoPlayer = false
            Toast.makeText(this, "Video stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playFromInternet() {
        stopCurrentPlayback()

        isUsingExoPlayer = true
        videoView.visibility = VideoView.GONE
        playerView.visibility = PlayerView.VISIBLE

        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        Toast.makeText(this, "Video is playing", Toast.LENGTH_SHORT).show()
    }

    private fun stopCurrentPlayback() {
        stopMedia()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
