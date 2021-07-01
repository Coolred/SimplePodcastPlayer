package com.example.podcastplayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.podcastplayer.databinding.ActivityPlayerBinding
import com.example.podcastplayer.model.PodcastVS
import com.example.podcastplayer.viewmodel.PodcastViewModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.koin.android.ext.android.inject
import tw.ktrssreader.kotlin.model.item.AutoMixItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding

    private val viewModel: PodcastViewModel by inject()

    private var player: SimpleExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private var isPlaying = false
    private var idx: Int = -1
    private lateinit var itemList : List<AutoMixItem>

    private var handler : Handler = Handler(Looper.getMainLooper())

    companion object {
        private const val ARG_IDX= "arg_idx"
        fun createInstance(context: Context, idx: Int): Intent {
            return Intent(context, PlayerActivity::class.java).putExtras(
                Bundle().apply {
                    putInt(ARG_IDX, idx)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras.let {
            idx = it?.getInt(ARG_IDX) ?: 0
        }

        observeViewModel()

        viewModel.loadPodcast()

        binding.playerContainer.ivNext.setOnClickListener {
            nextEpisode()
        }

        binding.playerContainer.ivPrev.setOnClickListener {
            previousEpisode()
        }
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(this@PlayerActivity) { it ->
            when (it) {
                is PodcastVS.Podcast -> {
                    it.podcast.items?.apply {
                        itemList = this.sortedBy {
                            LocalDate.parse(it.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochDay()
                        }
                        idx = itemList.size - idx - 1
                        updateEpisodesDate()
                        setEpisodes()
                    }
                }
                is PodcastVS.ShowLoader -> {
                    if (it.showLoader) {
                        binding.pbPlayer.visibility = View.VISIBLE
                    } else {
                        binding.pbPlayer.visibility = View.GONE
                    }
                }
                is PodcastVS.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun updateEpisodesDate() {
        val item = itemList[idx]
        binding.tvEpisodeTitle.text = item.title
        Glide.with(this@PlayerActivity).clear(binding.ivEpisodeCover)
        Glide.with(this@PlayerActivity)
            .load(item.image)
            .placeholder(android.R.drawable.stat_sys_warning)
            .into(binding.ivEpisodeCover)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        if (player == null) {
            trackSelector = DefaultTrackSelector(this).apply {
                player = SimpleExoPlayer.Builder(this@PlayerActivity)
                    .setTrackSelector(this)
                    .build()
            }
        }
    }

    private fun buildMediaSource(): List<MediaSource> {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            "exoplayer-codelab"
        )

        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

        return itemList.map {
            val mediaItem = MediaItem.fromUri(Uri.parse(it.enclosure?.url))
            mediaSourceFactory.createMediaSource(mediaItem)
        }
    }

    private fun releasePlayer() {
        handler.removeCallbacksAndMessages(null)
        player?.run {
            idx = currentWindowIndex
            removeListener(eventListener)
            release()
        }
        player = null
    }

    private fun setEpisodes() {
        initDefaultTimeBar()

        val mediaSources = buildMediaSource()

        player?.run {
            addListener(eventListener)
            setMediaSources(mediaSources, idx, 0)
            prepare()
            setPlayPause(true)
            binding.playerContainer.ivPlay.setOnClickListener {
                setPlayPause(!isPlaying)
            }
        }
    }

    private fun setPlayPause(play: Boolean) {
        isPlaying = play
        player?.playWhenReady = play

        if (!isPlaying) {
            binding.playerContainer.ivPlay.setImageResource(R.drawable.ic_play)
        } else {
            setProgress()
            binding.playerContainer.ivPlay.setImageResource(R.drawable.ic_pause)
        }
    }

    fun stringForTime(timeMs: Int): String {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeMs / 1000

        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600

        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun setProgress() {
        binding.playerContainer.tvCurrentTime.text = stringForTime(player?.currentPosition!!.toInt())
        binding.playerContainer.tvEndTime.text = stringForTime(player?.duration!!.toInt())

        handler.post(object: Runnable {
            override fun run() {
                if (isPlaying) {
                    val defaultTimeBar = binding.playerContainer.defaultTimeBar
                    if (player == null) return
                    defaultTimeBar.setDuration((player?.duration!! / 1000))
                    val mCurrentPosition = player?.currentPosition!!.toInt() / 1000
                    defaultTimeBar.setPosition(mCurrentPosition.toLong())
                    binding.playerContainer.tvCurrentTime.text = stringForTime(player?.currentPosition!!.toInt())
                    binding.playerContainer.tvEndTime.text = stringForTime(player?.duration!!.toInt())
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun initDefaultTimeBar() {
        val defaultTimeBar = binding.playerContainer.defaultTimeBar
        defaultTimeBar.requestFocus()
        defaultTimeBar.addListener(timeBarListener)
        defaultTimeBar.onFocusChangeListener =
            object : SeekBar.OnSeekBarChangeListener, View.OnFocusChangeListener {
                override fun onFocusChange(p0: View?, p1: Boolean) {
                    // Do Nothing for Focus Change
                }

                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (!p2) {
                        return
                    }

                    player?.seekTo(p1 * 1000L)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    player?.seekTo((p0?.progress ?: 0) * 1000L)
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    player?.seekTo((p0?.progress ?: 0) * 1000L)
                }
            }
    }

    private val timeBarListener = object : TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            player?.seekTo(position * 1000L)
        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            // Do Nothing for start
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            player?.seekTo(position * 1000L)
        }
    }

    private val eventListener = object : Player.Listener {
        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
            super.onTracksChanged(trackGroups, trackSelections)
            player?.currentWindowIndex?.apply {
                idx = this
            }
        }
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                nextEpisode()
            }
        }
    }

    private fun nextEpisode() {
        val hasNext = player?.hasNext() ?: false
        if (hasNext) {
            idx += 1
            updateEpisodesDate()
            player?.next()
        } else {
            Toast.makeText(this, getString(R.string.no_more_new_episodes), Toast.LENGTH_SHORT).show()
        }
    }

    private fun previousEpisode() {
        val hasPrevious = player?.hasPrevious() ?: false
        if (hasPrevious) {
            idx -= 1
            updateEpisodesDate()
            player?.previous()
        } else {
            Toast.makeText(this, getString(R.string.you_are_in_first_episode), Toast.LENGTH_SHORT).show()
        }
    }
}
