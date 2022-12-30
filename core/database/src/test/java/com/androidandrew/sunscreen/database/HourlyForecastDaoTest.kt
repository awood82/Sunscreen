package com.androidandrew.sunscreen.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.database.entity.HourlyForecastEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HourlyForecastDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var databaseHolder: FakeDatabaseWrapper

    private val locationOne = FakeData.zip
    private val today = FakeData.localDate.toString()
    private val locationOneForecastForToday = buildForecast(locationOne, today)

    private val tomorrow = FakeData.nextLocalDate.toString()
    private val locationOneForecastForTomorrow = buildForecast(locationOne, tomorrow)

    private val locationTwo = "10001"
    private val locationTwoForecastForToday = buildForecast(locationTwo, today)

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        runBlocking {
            databaseHolder.tearDown()
        }
    }

    @Test
    fun noForecasts_thenGet_returnsNoForecasts() = runTest {

        val actualForecast = databaseHolder.db.hourlyForecastDao.getOnce(zip = locationOne, date = today)

        assertTrue(actualForecast.isEmpty())
    }

    @Test
    fun noForecasts_getFlow_returnsNoForecasts() = runTest {

        val forecastFlow = databaseHolder.db.hourlyForecastDao.getFlow(zip = locationOne, date = today)

        val actualForecast = forecastFlow.first()
        assertTrue(actualForecast.isEmpty())
    }

    @Test
    fun insertHourlyForecasts_thenGet_returnsAllForecasts() = runTest {
        insertForecast(locationOneForecastForToday)

        val actualForecast = databaseHolder.db.hourlyForecastDao.getOnce(zip = locationOne, date = today)

        assertEquals(locationOneForecastForToday, actualForecast)
    }

    @Test
    fun getFlow_thenInsertForecasts_returnsUpdatedForecasts() = runTest {
        val forecastFlow = databaseHolder.db.hourlyForecastDao.getFlow(zip = locationOne, date = today)

        insertForecast(locationOneForecastForToday)

        val actualForecast = forecastFlow.first()
        assertEquals(locationOneForecastForToday, actualForecast)
    }

    @Test
    fun insertHourlyForecasts_twiceForSameLocation_thenGet_doesNotReturnDuplicates() = runTest {
        insertForecast(locationOneForecastForToday)
        insertForecast(locationOneForecastForToday)

        val actualForecast = databaseHolder.db.hourlyForecastDao.getOnce(zip = locationOne, date = today)

        assertEquals(locationOneForecastForToday, actualForecast)
    }

    @Test
    fun insertHourlyForecasts_forTwoLocations_thenGet_returnsAllForecastsForOneLocation() = runTest {
        assertNotEquals(locationOne, locationTwo)
        insertForecast(locationOneForecastForToday)
        insertForecast(locationTwoForecastForToday)

        val actualForecast = databaseHolder.db.hourlyForecastDao.getOnce(zip = locationOne, date = today)

        assertEquals(locationOneForecastForToday, actualForecast)
    }

    // The forecast network provider returns tomorrow's forecast when called in the evening.
    // If there is no data for today, then we should see tomorrow's forecast.
    @Test
    fun insertHourlyForecastsForTomorrow_andGetForecastForToday_returnsForecastForTomorrow() = runTest {
        insertForecast(locationOneForecastForTomorrow)

        val actualForecast = databaseHolder.db.hourlyForecastDao.getOnce(zip = locationOne, date = today)

        assertEquals(locationOneForecastForTomorrow, actualForecast)
    }

    // The forecast network provider returns tomorrow's forecast when called in the evening.
    // If there *IS* data for today, then we should still see today's forecast.
    @Test
    fun insertHourlyForecastsForTodayAndTomorrow_andGetForecastForToday_returnsForecastForToday() = runTest {
        insertForecast(locationOneForecastForToday)
        insertForecast(locationOneForecastForTomorrow)

        val actualForecast = databaseHolder.db.hourlyForecastDao.getOnce(zip = locationOne, date = today)

        assertEquals(locationOneForecastForToday, actualForecast)
    }

    // The forecast network provider returns tomorrow's forecast when called in the evening.
    // If there *IS* data for today, then we should still see today's forecast.
    @Test
    fun insertHourlyForecastsForTodayAndTomorrow_andGetFlowForToday_returnsForecastForToday() = runTest {
        insertForecast(locationOneForecastForToday)
        insertForecast(locationOneForecastForTomorrow)
        val forecastFlow = databaseHolder.db.hourlyForecastDao.getFlow(zip = locationOne, date = today)

        val actualForecast = forecastFlow.first()

        assertEquals(locationOneForecastForToday, actualForecast)
    }

    @Test
    fun getFlow_whenDateChanges_returnsUpdatedForecasts() = runTest {
        insertForecast(locationOneForecastForToday)
        insertForecast(locationOneForecastForTomorrow)
        val date = MutableStateFlow(today)
        // See https://stackoverflow.com/questions/69800618/how-to-manually-update-a-kotlin-flow
        val forecastFlow = date.flatMapLatest {
            databaseHolder.db.hourlyForecastDao.getFlow(zip = locationOne, date = it)
        }

        advanceUntilIdle()
        var actualForecast = forecastFlow.first()

        assertEquals(locationOneForecastForToday, actualForecast)

        date.value = tomorrow
        advanceUntilIdle()
        actualForecast = forecastFlow.first()

        assertEquals(locationOneForecastForTomorrow, actualForecast)
    }

    private suspend fun insertForecast(forecast: List<HourlyForecastEntity>) {
        databaseHolder.db.hourlyForecastDao.insert(forecast)
    }

    private fun buildForecast(location: String, date: String): List<HourlyForecastEntity> {
        return listOf(
            HourlyForecastEntity(zip = location, date = date, order = 1, time = LocalTime.of(8, 0).toString(), uvi = 5.0f),
            HourlyForecastEntity(zip = location, date = date, order = 2, time = LocalTime.of(9, 0).toString(), uvi = 3.0f)
        )
    }
}