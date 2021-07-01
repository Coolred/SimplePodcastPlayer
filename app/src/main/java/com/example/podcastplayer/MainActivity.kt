package com.example.podcastplayer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.podcastplayer.adapters.RVPodcastAdapter
import com.example.podcastplayer.databinding.ActivityMainBinding
import com.example.podcastplayer.model.PodcastVS
import com.example.podcastplayer.viewmodel.PodcastViewModel
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), RVPodcastAdapter.Listener {
    private lateinit var binding: ActivityMainBinding

    private val mAdapter = RVPodcastAdapter()
    private val viewModel: PodcastViewModel by inject()
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        mAdapter.setListener(this)

        binding.ivPodcastCover.apply {
            setImageResource(android.R.drawable.stat_sys_warning)
        }
        binding.rvPodcast.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mAdapter
        }

        viewModel.loadPodcast()
    }

    private fun observeViewModel(){

        viewModel.viewState.observe(this@MainActivity) {
            when (it) {
                is PodcastVS.Podcast -> {
                    title = it.podcast.title ?: ""
                    Glide.with(this@MainActivity).clear(binding.ivPodcastCover)
                    Glide.with(this@MainActivity)
                        .load(it.podcast.image?.url)
                        .placeholder(android.R.drawable.stat_sys_warning)
                        .into(binding.ivPodcastCover)
                    it.podcast.items?.apply{
                        mAdapter.setList(this)
                    }
                }
                is PodcastVS.ShowLoader -> {
                    if (it.showLoader) {
                        binding.pbPodcast.visibility = View.VISIBLE
                        binding.rvPodcast.visibility = View.GONE
                    } else {
                        binding.pbPodcast.visibility = View.GONE
                        binding.rvPodcast.visibility = View.VISIBLE
                    }
                }
                is PodcastVS.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onEpisodeClicked(position: Int) {
        startActivity(EpisodeActivity.createInstance(context = this, position))
    }
}