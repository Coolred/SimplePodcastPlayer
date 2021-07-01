package com.example.podcastplayer.model.repository.repositories.impl

import com.example.podcastplayer.model.repository.api.IPodcastApiModel
import com.example.podcastplayer.model.repository.repositories.IPodcastRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import tw.ktrssreader.kotlin.model.channel.AutoMixChannelData

class PodcastRepository(logTag: String) : IPodcastRepository, KoinComponent {

    private val podcastApiModel: IPodcastApiModel by inject { parametersOf(logTag) }

    override fun getPodcast(): Flow<AutoMixChannelData> {
        return podcastApiModel.getDaodu()
    }
}