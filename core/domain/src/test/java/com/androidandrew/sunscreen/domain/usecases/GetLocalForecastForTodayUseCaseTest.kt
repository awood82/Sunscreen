package com.androidandrew.sunscreen.domain.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.androidandrew.sharedtest.model.FakeUvPredictions
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.HourlyForecastRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetLocalForecastForTodayUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var useCase: GetLocalForecastForTodayUseCase
    private val mockUserSettingsRepository: UserSettingsRepository = mockk()
    private val mockHourlyForecastRepository: HourlyForecastRepository = mockk()
    private val clock = FakeData.clockDefaultNoon

    private fun createUseCase() {
        useCase = GetLocalForecastForTodayUseCase(
            userSettingsRepository = mockUserSettingsRepository,
            hourlyForecastRepository = mockHourlyForecastRepository,
            clock = clock
        )
    }

    @Test
    fun invoke_withNullLocation_returnsEmptyList() = runTest {
        setLocation(null)
        createUseCase()

        val forecastStream = useCase()

        val actualForecast = forecastStream.first()
        assertTrue(actualForecast.isEmpty())
    }

    @Test
    fun invoke_withValidLocation_returnsForecast() = runTest {
        setLocation(FakeData.zip)
        setForecastForLocation(FakeData.zip)
        createUseCase()

        val forecastStream = useCase()

        val actualForecast = forecastStream.first()
        assertEquals(FakeUvPredictions.forecast, actualForecast)
    }

    private fun setLocation(location: String?) {
        coEvery { mockUserSettingsRepository.getLocation() } returns location
        every { mockUserSettingsRepository.getLocationFlow() } returns flowOf(location)
    }

    private fun setForecastForLocation(location: String) {
        coEvery { mockHourlyForecastRepository.getForecast(location, FakeData.localDate) } returns FakeUvPredictions.forecast
        every { mockHourlyForecastRepository.getForecastFlow(location, FakeData.localDate) } returns flowOf(FakeUvPredictions.forecast)
    }
}