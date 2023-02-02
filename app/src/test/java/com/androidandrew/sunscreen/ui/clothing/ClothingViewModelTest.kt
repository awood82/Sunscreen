package com.androidandrew.sunscreen.ui.clothing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import com.androidandrew.sunscreen.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
    }

    @After
    fun tearDown() {
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    private fun createViewModel() {
        vm = ClothingViewModel(userSettingsRepo)
    }

    @Test
    fun topAndBottomCombos_mapToUvFactor() = runTest {
        createViewModel()
        // Nothing selected initially. Use default.
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(defaultUserClothing, userSettingsRepo.getClothing())

        // Selecting only one option will still use default for the other.
        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.NOTHING))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UserClothing(ClothingTop.NOTHING, defaultBottom), userSettingsRepo.getClothing())

        // Once top and bottom are selected, map to a combo.
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.NOTHING))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UserClothing(ClothingTop.NOTHING, ClothingBottom.NOTHING), userSettingsRepo.getClothing())

        // Keep shirt off, put shorts on
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.SHORTS))
        vm.onEvent(ClothingEvent.ContinuePressed)
        assertEquals(UserClothing(ClothingTop.NOTHING, ClothingBottom.SHORTS), userSettingsRepo.getClothing())
    }

    @Test
    fun continueButton_whenPressed_savesStateToRepo_andOnboardingIsComplete() = runTest {
        createViewModel()
        val collectedIsContinueSelected = mutableListOf<Boolean>()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            vm.isClothingDone.collect { collectedIsContinueSelected.add(it) }
        }

        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.LONG_SLEEVE_SHIRT))
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.PANTS))
        vm.onEvent(ClothingEvent.ContinuePressed)

        assertTrue(collectedIsContinueSelected.first())
        val expectedClothing = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.PANTS)
        assertEquals(expectedClothing, userSettingsRepo.getClothing())
        assertTrue(userSettingsRepo.getIsOnboarded())

        collectJob.cancel()
    }

    @Test
    fun initialSelectedItems_whenClothingIsInRepo_overridesDefaults() = runTest {
        val clothing = UserClothing(
            top = ClothingTop.LONG_SLEEVE_SHIRT,
            bottom = ClothingBottom.PANTS
        )
        assertNotEquals(defaultUserClothing, clothing)
        userSettingsRepo.setClothing(clothing)
        createViewModel()

        val clothingState = vm.clothingState.first()
        assertEquals(clothing.top, clothingState.selectedTop)
        assertEquals(clothing.bottom, clothingState.selectedBottom)
    }

    @Test
    fun clothingItems_whenSelected_updateState() = runTest {
        createViewModel()

        vm.onEvent(ClothingEvent.TopSelected(ClothingTop.LONG_SLEEVE_SHIRT))
        vm.onEvent(ClothingEvent.BottomSelected(ClothingBottom.PANTS))

        val clothingState = vm.clothingState.first()
        assertEquals(ClothingTop.LONG_SLEEVE_SHIRT, clothingState.selectedTop)
        assertEquals(ClothingBottom.PANTS, clothingState.selectedBottom)
    }
}