package com.androidandrew.sunscreen.domain.usecases

import com.androidandrew.sunscreen.common.DataResult
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

    operator fun invoke(): Flow<DataResult<List<UvPredictionPoint>>> {
        return locationStream
            .map {
                Timber.i("Use Case: Get local forecast for '$it'")
                if (it.isEmpty()) {
                    DataResult.Success(emptyList()) // TODO: Use the NiA app's Result.Loading?
                } else {
                    hourlyForecastRepository.getForecast(it, LocalDate.now(clock))
                }
            }
    }

    suspend fun refresh(location: String, force: Boolean = false) {
        Timber.d("Updating location ($location) in repo, force = $force")
        if (force) {
            userSettingsRepository.setLocation("")
        }
        userSettingsRepository.setLocation(location)
    }
}