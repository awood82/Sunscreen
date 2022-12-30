package com.androidandrew.sunscreen.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.network.model.HourlyUvIndexForecast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HourlyForecastRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: HourlyForecastRepository
    private lateinit var databaseHolder: FakeDatabaseWrapper

    private val forecast = listOf(
        HourlyUvIndexForecast(5, FakeData.zip, "${FakeData.dateNetworkFormatted} 08 AM", 0),
        HourlyUvIndexForecast(6, FakeData.zip, "${FakeData.dateNetworkFormatted} 09 AM", 2),
        HourlyUvIndexForecast(7, FakeData.zip, "${FakeData.dateNetworkFormatted} 10 AM", 4)
    )

    private val expectedModel = listOf(
        UvPredictionPoint(time = LocalTime.of(8, 0), 0.0),
        UvPredictionPoint(time = LocalTime.of(9, 0), 2.0),
        UvPredictionPoint(time = LocalTime.of(10, 0), 4.0)
    )

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
        repository = HourlyForecastRepositoryImpl(
            hourlyForecastDao = databaseHolder.hourlyForecastDao
        )
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        runBlocking {
            databaseHolder.tearDown()
        }
    }

    @Test
    fun networkForecast_savedToDb_andRead_returnsExpectedModel() = runTest {
        repository.setForecast(forecast)

        val actualReadModel = repository.getForecast(FakeData.zip, FakeData.localDate)

        assertEquals(expectedModel, actualReadModel)
    }

    @Test
    fun getNetworkForecastFlow_thenInsert_getsUpdatedFlow() = runTest {
        val forecastFlow = repository.getForecastFlow(FakeData.zip, FakeData.localDate)

        repository.setForecast(forecast)

        assertEquals(expectedModel, forecastFlow.first())
    }
}