package com.example.podcastplayer.model.repository.api.impl

import android.util.Log
import com.example.podcastplayer.model.repository.api.IPodcastApiModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinComponent
import tw.ktrssreader.Reader
import tw.ktrssreader.kotlin.model.channel.AutoMixChannelData

class PodcastApiModel(private val logTag: String) : IPodcastApiModel, KoinComponent {
    companion object {
        private const val DEFAULT_RSS_URL = "https://feeds.soundcloud.com/users/soundcloud:users:322164009/sounds.rss"
    }

    override fun getDaodu(): Flow<AutoMixChannelData> {
        Log.d(logTag, "GetDaodi flowRead")
        return Reader.flowRead(DEFAULT_RSS_URL)
    }
}