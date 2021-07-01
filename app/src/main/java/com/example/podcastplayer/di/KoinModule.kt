import com.example.podcastplayer.model.repository.api.IPodcastApiModel
import com.example.podcastplayer.model.repository.api.impl.PodcastApiModel
import com.example.podcastplayer.model.repository.repositories.IPodcastRepository
import com.example.podcastplayer.model.repository.repositories.impl.PodcastRepository
import com.example.podcastplayer.viewmodel.PodcastViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinViewModelModule = module {
    viewModel { PodcastViewModel() }
}

val koinRepositoryModule = module {
    factory<IPodcastRepository> { (logTag: String) -> PodcastRepository(logTag) }
}

val koinApiModelModule = module {
    factory<IPodcastApiModel> { (logTag: String) -> PodcastApiModel(logTag) }
}

val koinProviderModule = module {

}

val koinModuleList = listOf(
    koinViewModelModule,
    koinRepositoryModule,
    koinApiModelModule,
    koinProviderModule
)