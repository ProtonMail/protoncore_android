package me.proton.core.humanverification.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import me.proton.core.humanverification.domain.repository.LocalRepository

/**
 * Created by dinokadrikj on 6/18/20.
 */
class LoadCountriesUseCase(private val localRepository: LocalRepository) {

    suspend operator fun invoke() = channelFlow {
        localRepository.allCountries().collect {
            send(it)
        }
    }
}
