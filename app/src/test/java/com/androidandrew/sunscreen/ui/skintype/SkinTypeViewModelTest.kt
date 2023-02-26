package com.androidandrew.sunscreen.ui.skintype

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import com.androidandrew.sunscreen.ui.navigation.AppDestination
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
class SkinTypeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: SkinTypeViewModel
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
        vm = SkinTypeViewModel(userSettingsRepo, mockAnalytics)
    }

    @After
    fun tearDown() {
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    @Test
    fun whenSkinType_isSelected_itIsSavedToRepo_andOnboardingisNotCompleteYet() = runTest {
        vm.onEvent(SkinTypeEvent.Selected(4))

        assertEquals(4, userSettingsRepo.getSkinType())
        assertFalse(userSettingsRepo.getIsOnboarded())
    }

    @Test
    fun whenSkinType_isSelected_stateIsUpdated() = runTest {
        val collectedIsSkinTypeSelected = mutableListOf<Boolean>()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            vm.isSkinTypeSelected.collect { collectedIsSkinTypeSelected.add(it) }
        }

        vm.onEvent(SkinTypeEvent.Selected(4))

        assertTrue(collectedIsSkinTypeSelected.first())

        collectJob.cancel()
    }

    @Test
    fun init_logsAnalyticsEvent() = runTest {
        verify { mockAnalytics.viewScreen(AppDestination.SkinType.name) }
    }

    @Test
    fun whenSkinType_isSelected_logsAnalyticsEvent() = runTest {
        vm.onEvent(SkinTypeEvent.Selected(5))

        verify { mockAnalytics.selectSkinType(5) }
    }
}