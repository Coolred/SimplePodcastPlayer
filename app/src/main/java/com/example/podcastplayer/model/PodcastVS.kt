package com.example.podcastplayer.model

import tw.ktrssreader.kotlin.model.channel.AutoMixChannelData

sealed class PodcastVS {
    class Podcast(val podcast: AutoMixChannelData):PodcastVS()
    class Error(val message:String?):PodcastVS()
    class ShowLoader(val showLoader:Boolean):PodcastVS()
}
