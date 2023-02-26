package com.androidandrew.sunscreen.ui.location

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import com.androidandrew.sunscreen.ui.navigation.AppDestination
import com.androidandrew.sunscreen.util.LocationUtil
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: LocationViewModel
    private lateinit var fakeDatabaseHolder: FakeDatabaseWrapper
    private lateinit var userSettingsRepo: UserSettingsRepository
    private val mockAnalytics: EventLogger = mockk(relaxed = true)

    @Before
    fun setup() {
        fakeDatabaseHolder = FakeDatabaseWrapper()
        runBlocking {
            fakeDatabaseHolder.clearDatabase()
        }
        userSettingsRepo = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
    }

    @After
    fun tearDown() {
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    private fun createViewModel() {
        vm = LocationViewModel(userSettingsRepo, LocationUtil(), mockAnalytics)
    }

    @Test
    fun onSearchLocation_whenValidZipCode_savesItToRepo_andSetsLocationValid() = runTest {
        val collectedIsLocationValid = mutableListOf<Boolean>()
        createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            vm.isLocationValid.collect { collectedIsLocationValid.add(it) }
        }

        vm.onEvent(LocationBarEvent.LocationSearched("10001"))

        assertTrue(collectedIsLocationValid.first())
        assertEquals("10001", userSettingsRepo.getLocation())

        collectJob.cancel()
    }

    @Test
    fun onSearchLocation_whenInvalidZipCode_doesNotSaveItToRepo_andDoesNotNavigate() = runTest {
        val collectedIsLocationValid = mutableListOf<Boolean>()
        createViewModel()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            vm.isLocationValid.collect { collectedIsLocationValid.add(it) }
        }

        vm.onEvent(LocationBarEvent.LocationSearched("1"))

        assertTrue(collectedIsLocationValid.isEmpty())
        assertEquals("", userSettingsRepo.getLocation())

        collectJob.cancel()
    }

    @Test
    fun init_logsAnalyticsEvents() = runTest {
        createViewModel()

        verify { mockAnalytics.startTutorial() }
        verify { mockAnalytics.viewScreen(AppDestination.Location.name) }
    }

    @Test
    fun onSearchLocation_ifInvalid_logsAnalyticsEvent() = runTest {
        createViewModel()

        vm.onEvent(LocationBarEvent.LocationSearched("FAKE"))

        verify { mockAnalytics.searchLocation("FAKE") }
    }

    @Test
    fun onSearchLocation_ifValid_logsAnalyticsEvent() = runTest {
        createViewModel()

        vm.onEvent(LocationBarEvent.LocationSearched("94510"))

        verify { mockAnalytics.searchLocation("94510") }
    }
}