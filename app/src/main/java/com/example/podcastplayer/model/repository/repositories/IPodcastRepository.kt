package com.example.podcastplayer.model.repository.repositories

import kotlinx.coroutines.flow.Flow
import tw.ktrssreader.kotlin.model.channel.AutoMixChannelData

interface IPodcastRepository {
    fun getPodcast() : Flow<AutoMixChannelData>
}