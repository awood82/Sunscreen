package com.androidandrew.sunscreen.ui.skintype

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SkinTypeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: SkinTypeViewModel
    private lateinit var fakeDatabaseHolder: FakeDatabaseWrapper
    private lateinit var userSettingsRepo: UserSettingsRepository

    @Before
    fun setup() {
        fakeDatabaseHolder = FakeDatabaseWrapper()
        runBlocking {
            fakeDatabaseHolder.clearDatabase()
        }
        userSettingsRepo = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
        vm = SkinTypeViewModel(userSettingsRepo)
    }

    @After
    fun tearDown() {
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    @Test
    fun whenSkinType_isSelected_itIsSavedToRepo_andOnboardingisComplete() = runTest {
        vm.onEvent(SkinTypeEvent.Selected(4))

        assertEquals(4, userSettingsRepo.getSkinType())
        assertTrue(userSettingsRepo.getIsOnboarded())
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
}