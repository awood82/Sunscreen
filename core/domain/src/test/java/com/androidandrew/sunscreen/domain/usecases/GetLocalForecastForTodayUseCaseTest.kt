package com.androidandrew.sunscreen.domain.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.model.FakeUvPredictions
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.*
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.model.trim
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class GetLocalForecastForTodayUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var useCase: GetLocalForecastForTodayUseCase
    private val fakeUvService = FakeEpaService
    private lateinit var fakeDatabaseHolder: FakeDatabaseWrapper
    private lateinit var hourlyForecastRepo: HourlyForecastRepository
    private lateinit var userSettingsRepo: UserSettingsRepository
    private lateinit var userTrackingRepo: UserTrackingRepository
    private val clock = FakeData.clockDefaultNoon

    private fun createUseCase() {
        useCase = GetLocalForecastForTodayUseCase(
            userSettingsRepository = userSettingsRepo,
            hourlyForecastRepository = hourlyForecastRepo,
            clock = clock
        )
    }

    @Before
    fun setup() {
        fakeDatabaseHolder = FakeDatabaseWrapper()
        runBlocking {
            fakeDatabaseHolder.clearDatabase()
        }
        userTrackingRepo = UserTrackingRepositoryImpl(fakeDatabaseHolder.userTrackingDao)
        userSettingsRepo = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
        hourlyForecastRepo = HourlyForecastRepositoryImpl(fakeDatabaseHolder.hourlyForecastDao, fakeUvService)
    }

    @After
    fun tearDown() {
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    @Test
    fun invoke_withNullLocation_returnsEmptyList() = runTest {
        createUseCase()

        val forecastStream = useCase()

        val actualForecast = forecastStream.first()
        assertTrue(actualForecast.getOrNull()!!.isEmpty())
    }

    @Test
    fun invoke_withValidLocation_returnsForecast() = runTest {
        setLocation(FakeData.zip)
        createUseCase()

        val forecastStream = useCase()

        val actualForecast = forecastStream.first()
        assertTrue(actualForecast.isSuccess)
        assertEquals(FakeUvPredictions.forecast.trim(), actualForecast.getOrNull())
    }

    @Test
    fun forceRefresh_triggersForecastUpdate() = runTest {
        setLocation(FakeData.zip)
        fakeUvService.exception = IOException()
        createUseCase()
        var actualForecast: Result<List<UvPredictionPoint>> = Result.success(emptyList())
        val collectJob = launch(UnconfinedTestDispatcher()) {
            useCase.invoke().collect {
                actualForecast = it
            }
        }

        assertTrue(actualForecast.isFailure)

        fakeUvService.exception = null
        useCase.forceRefresh(FakeData.zip)

        assertTrue(actualForecast.isSuccess)
        assertEquals(FakeUvPredictions.forecast.trim(), actualForecast.getOrNull()!!)
        collectJob.cancel()
    }

    private suspend fun setLocation(location: String) {
        userSettingsRepo.setLocation(location)
    }
}