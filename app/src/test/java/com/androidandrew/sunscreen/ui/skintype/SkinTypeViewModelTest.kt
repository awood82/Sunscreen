package com.androidandrew.sunscreen.ui.skintype

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SkinTypeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockSettingsRepo = mockk<UserSettingsRepositoryImpl>(relaxed = true)
    private val vm = SkinTypeViewModel(mockSettingsRepo)

    @Test
    fun whenSkinType_isSelected_itIsSavedToRepo_andOnboardingisComplete() = runTest {
        vm.onEvent(SkinTypeEvent.Selected(4))

        coVerify { mockSettingsRepo.setSkinType(4) }
        coVerify { mockSettingsRepo.setIsOnboarded(true) }
    }

    @Test
    fun whenSkinType_isSelected_stateIsUpdated() = runTest {
        vm.onEvent(SkinTypeEvent.Selected(4))

        assertTrue(vm.isSkinTypeSelected.first())
    }
}