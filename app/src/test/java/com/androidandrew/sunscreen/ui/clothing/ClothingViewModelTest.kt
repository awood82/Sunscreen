package com.androidandrew.sunscreen.ui.clothing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import com.androidandrew.sunscreen.domain.UvFactor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ClothingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: ClothingViewModel
    private lateinit var fakeDatabaseHolder: FakeDatabaseWrapper
    private lateinit var userSettingsRepo: UserSettingsRepository

    @Before
    fun setup() {
        fakeDatabaseHolder = FakeDatabaseWrapper()
        runBlocking {
            fakeDatabaseHolder.clearDatabase()
        }
        userSettingsRepo = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
        vm = ClothingViewModel(userSettingsRepo)
    }

    @After
    fun tearDown() {
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    @Test
    fun whenClothing_isSelected_itIsSavedToRepo_andOnboardingisComplete() = runTest {
        vm.onEvent(ClothingEvent.Selected(UvFactor.Clothing.PANTS_NO_SHIRT))

        assertEquals(2, userSettingsRepo.getClothing())
        assertTrue(userSettingsRepo.getIsOnboarded())
    }

    // TODO: whenSkipped...

    @Test
    fun whenClothing_isSelected_stateIsUpdated() = runTest {
        val collectedIsClothingSelected = mutableListOf<Boolean>()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            vm.isClothingSelected.collect { collectedIsClothingSelected.add(it) }
        }

        vm.onEvent(ClothingEvent.Selected(UvFactor.Clothing.PANTS_LONG_SLEEVE_SHIRT))

        assertTrue(collectedIsClothingSelected.first())

        collectJob.cancel()
    }
}