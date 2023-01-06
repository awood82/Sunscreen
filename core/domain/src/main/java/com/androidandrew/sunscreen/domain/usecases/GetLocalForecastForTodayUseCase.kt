package com.androidandrew.sunscreen.domain.usecases

import com.androidandrew.sunscreen.data.repository.HourlyForecastRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.model.UvPredictionPoint
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate

/**
 * Retrieving the forecast requires: Current Date, Current Location/ZIP
 * Computing the amount of sun exposure requires: Current Time, User Settings
 * The only thing missing is the Current Date.
 *   Either inject a Clock everywhere, or write it to a database as it gets updated by some clock-watching feature
 */
class GetLocalForecastForTodayUseCase(
    private val userSettingsRepository: UserSettingsRepository,
    private val hourlyForecastRepository: HourlyForecastRepository,
    private val clock: Clock
) {

    private val locationStream = userSettingsRepository.getLocationFlow()

    operator fun invoke(): Flow<List<UvPredictionPoint>> {
        return locationStream
            .distinctUntilChanged()
            .map {
                Timber.i("Use Case: Get local forecast for $it")
                if (it.isEmpty()) {
                    emptyList()
                } else {
                    hourlyForecastRepository.getForecast(it, LocalDate.now(clock))
                }
            }
    }

    suspend fun forceRefresh(location: String) {
        userSettingsRepository.setLocation("")
        userSettingsRepository.setLocation(location)
    }
}