package com.example.podcastplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.podcastplayer.databinding.ActivityEpisodeBinding
import com.example.podcastplayer.model.PodcastVS
import com.example.podcastplayer.viewmodel.PodcastViewModel
import org.koin.android.ext.android.inject
import tw.ktrssreader.kotlin.model.item.AutoMixItem

class EpisodeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityEpisodeBinding

    private val viewModel: PodcastViewModel by inject()

    private var idx: Int = -1
    private lateinit var item: AutoMixItem

    companion object {
        private const val ARG_IDX= "arg_idx"
        fun createInstance(context: Context, idx: Int): Intent {
            return Intent(context, EpisodeActivity::class.java).putExtras(
                Bundle().apply {
                    putInt(ARG_IDX, idx)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras.let {
            idx = it?.getInt(ARG_IDX) ?: 0
        }

        observeViewModel()

        viewModel.loadPodcast()
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(this@EpisodeActivity) { it ->
            when (it) {
                is PodcastVS.Podcast -> {
                    binding.tvPodcastTitle.text = it.podcast.title ?: ""
                    it.podcast.items?.apply {
                        item = this[idx]
                        binding.tvEpisodeTitle.text = item.title
                        binding.tvDescription.text = item.description
                        Glide.with(this@EpisodeActivity).clear(binding.ivEpisodeCover)
                        Glide.with(this@EpisodeActivity)
                            .load(item.image)
                            .placeholder(android.R.drawable.stat_sys_warning)
                            .into(binding.ivEpisodeCover)
                        binding.ivPlay.setOnClickListener{
                            this@EpisodeActivity.onClick(it)
                        }
                    }
                }
                is PodcastVS.ShowLoader -> {
                    if (it.showLoader) {
                        binding.pbEpisode.visibility = View.VISIBLE
                    } else {
                        binding.pbEpisode.visibility = View.GONE
                    }
                }
                is PodcastVS.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onClick(v: View?) {
        startActivity(PlayerActivity.createInstance(context = this, idx))
    }
}