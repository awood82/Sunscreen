package com.androidandrew.sunscreen.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.model.asExternalModel
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.common.DataResult
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.model.trim
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.model.HourlyUvIndexForecast
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.rules.TestName
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalTime


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HourlyForecastRepositoryTest {

    private annotation class UseMockService

    @get:Rule
    val testName = TestName()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: HourlyForecastRepository
    private lateinit var databaseHolder: FakeDatabaseWrapper
    private val fakeService = FakeEpaService
    private val mockService: EpaService = mockk(relaxed = true)

    private val forecast = listOf(
        HourlyUvIndexForecast(1, FakeData.zip, "${FakeData.dateNetworkFormatted} 07 AM", 0),
        HourlyUvIndexForecast(2, FakeData.zip, "${FakeData.dateNetworkFormatted} 08 AM", 0),
        HourlyUvIndexForecast(3, FakeData.zip, "${FakeData.dateNetworkFormatted} 09 AM", 2),
        HourlyUvIndexForecast(4, FakeData.zip, "${FakeData.dateNetworkFormatted} 10 AM", 4),
        HourlyUvIndexForecast(5, FakeData.zip, "${FakeData.dateNetworkFormatted} 11 AM", 2),
        HourlyUvIndexForecast(6, FakeData.zip, "${FakeData.dateNetworkFormatted} 12 PM", 0),
        HourlyUvIndexForecast(7, FakeData.zip, "${FakeData.dateNetworkFormatted} 01 PM", 0)
    )

    // It's expected that only one 0.0 UV value is kept at the start and end
    private val expectedModel = listOf(
//        UvPredictionPoint(time = LocalTime.of(7, 0), 0.0),
        UvPredictionPoint(time = LocalTime.of(8, 0), 0.0),
        UvPredictionPoint(time = LocalTime.of(9, 0), 2.0),
        UvPredictionPoint(time = LocalTime.of(10, 0), 4.0),
        UvPredictionPoint(time = LocalTime.of(11, 0), 2.0),
        UvPredictionPoint(time = LocalTime.of(12, 0), 0.0),
//        UvPredictionPoint(time = LocalTime.of(13, 0), 0.0)
    )

    private val forecastForTomorrow = listOf(
        HourlyUvIndexForecast(1, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 07 AM", 0),
        HourlyUvIndexForecast(2, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 08 AM", 0),
        HourlyUvIndexForecast(3, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 09 AM", 3),
        HourlyUvIndexForecast(4, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 10 AM", 5),
        HourlyUvIndexForecast(5, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 11 AM", 3),
        HourlyUvIndexForecast(6, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 12 PM", 0),
        HourlyUvIndexForecast(7, FakeData.zip, "${FakeData.nextDateNetworkFormatted} 01 PM", 0)
    )

    // It's expected that only one 0.0 UV value is kept at the start and end
    private val expectedModelForTomorrow = listOf(
//        UvPredictionPoint(time = LocalTime.of(7, 0), 0.0),
        UvPredictionPoint(time = LocalTime.of(8, 0), 0.0),
        UvPredictionPoint(time = LocalTime.of(9, 0), 3.0),
        UvPredictionPoint(time = LocalTime.of(10, 0), 5.0),
        UvPredictionPoint(time = LocalTime.of(11, 0), 3.0),
        UvPredictionPoint(time = LocalTime.of(12, 0), 0.0),
//        UvPredictionPoint(time = LocalTime.of(13, 0), 0.0)
    )

    private val mockForecast = listOf(
        HourlyUvIndexForecast(3, FakeData.zip, "${FakeData.dateNetworkFormatted} 09 AM", 5)
    )

    private val expectedMockModel = listOf(
        UvPredictionPoint(time = LocalTime.of(9, 0), 5.0),
    )

    @Before
    fun setup() {
        val method = this.javaClass.getMethod(testName.methodName)
        val uvService = when (method.isAnnotationPresent(UseMockService::class.java)) {
            true -> mockService
            false -> fakeService
        }
        coEvery { mockService.getUvForecast(any()) } returns Result.success(mockForecast)

        databaseHolder = FakeDatabaseWrapper()
        repository = HourlyForecastRepositoryImpl(
            hourlyForecastDao = databaseHolder.hourlyForecastDao,
            uvService = uvService
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

        assertEquals(expectedModel, (actualReadModel as DataResult.Success).data)
    }

    @UseMockService
    @Test
    fun getNetworkForecast_ifExistsInDatabase_doesNotQueryNetwork() = runTest {
        repository.setForecast(forecast)

        val actualReadForecast = repository.getForecast(FakeData.zip, FakeData.localDate)

        coVerify(exactly = 0) { mockService.getUvForecast(any()) }
        assertEquals(expectedModel, (actualReadForecast as DataResult.Success).data)
    }

    @Test
    fun getNetworkForecast_ifDoesNotExistInDatabase_queriesNetwork_andCachesResult() = runTest {
        val expectedNetworkResult = FakeEpaService.forecast.asExternalModel().trim()

        var actualReadForecast = repository.getForecast(FakeData.zip, FakeData.localDate)

        assertTrue(actualReadForecast is DataResult.Success)
        assertEquals(expectedNetworkResult, (actualReadForecast as DataResult.Success).data)

        // The result should be cached, so the network is not queried again
        fakeService.exception = IOException() // Make updates unreadablee
        actualReadForecast = repository.getForecast(FakeData.zip, FakeData.localDate)
        assertTrue(actualReadForecast is DataResult.Success)
        assertEquals(expectedNetworkResult, (actualReadForecast as DataResult.Success).data)
    }

    @Test
    fun getNetworkForecast_ifDoesNotExistInDatabase_andNetworkHasError_returnsError() = runTest {
        fakeService.exception = IOException()
        val actualReadForecast = repository.getForecast(FakeData.zip, FakeData.localDate)

        assertTrue(actualReadForecast is DataResult.Error)
    }
}