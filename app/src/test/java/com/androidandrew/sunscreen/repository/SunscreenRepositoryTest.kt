package com.androidandrew.sunscreen.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.database.FakeDatabase
import com.androidandrew.sunscreen.database.Forecast
import com.androidandrew.sunscreen.network.FakeEpaService
import com.androidandrew.sunscreen.util.FakeData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SunscreenRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repo: SunscreenRepository
    private val fakeDatabaseHolder = FakeDatabase()
    private val fakeDatabase = fakeDatabaseHolder.createDatabase()
    private val fakeNetwork = FakeEpaService()

    private val DATE = FakeData.localDate
    private val ZIP = FakeData.zip

    private fun createRepository() { //useMockDatabase: Boolean, useMockNetwork: Boolean/*, dispatcher: CoroutineDispatcher = StandardTestDispatcher()*/) {
        repo = SunscreenRepository(fakeDatabase, fakeNetwork, FakeData.clockDefaultNoon)
        /*val databaseToUse = when(useMockDatabase) {
            true -> mockDatabase
            false -> fakeDatabase
        }
        val networkToUse = when(useMockNetwork) {
            true -> mockNetwork
            false -> fakeNetwork
        }*/
//        repo = SunscreenRepository(databaseToUse, networkToUse/*, dispatcher*/)
    }

    @Test
    fun refreshForecast_whenDatabaseIsEmpty_downloadsFromNetwork() = runTest {
        createRepository()

        repo.refreshForecast(DATE, ZIP)

        val expected = FakeEpaService.sampleDailyUvForecast.size - 1 // One of the forecasts is for midnight today, which is tomorrow
        assertEquals(expected, fakeDatabase.forecastDao.getForecastsCount(DATE.toString(), ZIP))
    }

    @Test
    fun refreshForecast_whenDatabaseHasEntries_doesNotDownloadFromNetwork() = runTest {
        createRepository()
        fakeDatabaseHolder.insertForecasts(2)

        repo.refreshForecast(DATE, ZIP)

        val expected = 2
        assertEquals(expected, fakeDatabase.forecastDao.getForecastsCount(DATE.toString(), ZIP))
    }
}