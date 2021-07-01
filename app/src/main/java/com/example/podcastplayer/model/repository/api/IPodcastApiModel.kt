package com.example.podcastplayer.model.repository.api

import kotlinx.coroutines.flow.Flow
import tw.ktrssreader.kotlin.model.channel.AutoMixChannelData

interface IPodcastApiModel {
    fun getDaodu() : Flow<AutoMixChannelData>
}