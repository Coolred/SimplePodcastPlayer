package com.example.podcastplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastplayer.model.PodcastVS
import com.example.podcastplayer.model.repository.repositories.IPodcastRepository
import com.example.podcastplayer.utils.io
import com.example.podcastplayer.utils.ui
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class PodcastViewModel : ViewModel(), KoinComponent {
    private val logTag = PodcastViewModel::class.java.simpleName
    private val podcastRepository: IPodcastRepository by inject { parametersOf(logTag) }

    val viewState: LiveData<PodcastVS> get() = mViewState
    private val mViewState = MutableLiveData<PodcastVS>()

    fun loadPodcast() {
        viewModelScope.launch {
            mViewState.value = PodcastVS.ShowLoader(true)
            try {
                io {
                    podcastRepository.getPodcast().collect {
                            ui {
                                mViewState.value = PodcastVS.Podcast(it)
                            }
                        }
                }
            } catch (e: Exception) {
                ui {
                    mViewState.value = PodcastVS.Error(e.message)
                }
            }
            mViewState.value = PodcastVS.ShowLoader(false)
        }
    }
}