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
    fun topAndBottomCombos_mapToUvFactor() = runTest {
        val default = UvFactor.Clothing.SHORTS_T_SHIRT

        // Nothing selected initially. Use default.
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(default.ordinal, userSettingsRepo.getClothing())

        // Selecting only one option will still use default.
        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.NOTHING))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(default.ordinal, userSettingsRepo.getClothing())

        // Once top and bottom are selected, map to a combo.
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.NOTHING))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UvFactor.Clothing.NAKED.ordinal, userSettingsRepo.getClothing())

        // Keep shirt off, put shorts on
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.SHORTS))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UvFactor.Clothing.SHORTS_NO_SHIRT.ordinal, userSettingsRepo.getClothing())

        // Keep shirt off, put pants on
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.PANTS))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UvFactor.Clothing.PANTS_NO_SHIRT.ordinal, userSettingsRepo.getClothing())

        // Put on T-shirt, keep pants on
        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.T_SHIRT))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UvFactor.Clothing.PANTS_T_SHIRT.ordinal, userSettingsRepo.getClothing())

        // Put on long sleeve shirt, keep pants on
        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.LONG_SLEEVE_SHIRT))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UvFactor.Clothing.PANTS_LONG_SLEEVE_SHIRT.ordinal, userSettingsRepo.getClothing())

        // Put on shorts and a T-shirt
        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.T_SHIRT))
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.SHORTS))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UvFactor.Clothing.SHORTS_T_SHIRT.ordinal, userSettingsRepo.getClothing())
    }

    @Test
    fun continueButton_whenPressed_savesStateToRepo_andOnboardingIsComplete() = runTest {
        val collectedIsContinueSelected = mutableListOf<Boolean>()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            vm.isContinuePressed.collect { collectedIsContinueSelected.add(it) }
        }

        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.LONG_SLEEVE_SHIRT))
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.PANTS))
        vm.onEvent(ClothingEvent.ContinuePressed)

        assertTrue(collectedIsContinueSelected.first())
        assertEquals(5, userSettingsRepo.getClothing())
        assertTrue(userSettingsRepo.getIsOnboarded())

        collectJob.cancel()
    }
}