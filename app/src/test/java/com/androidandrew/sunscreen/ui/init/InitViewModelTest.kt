package com.androidandrew.sunscreen.ui.init

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.util.LocationUtil
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class InitViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: InitViewModel
    private val mockRepository = mockk<UserRepositoryImpl>(relaxed = true)

    private fun createViewModel() {
        vm = InitViewModel(mockRepository, LocationUtil())
    }

    @Test
    fun init_whenRepoHasNoLocation_navigatesToLocationScreen() = runTest {
        coEvery { mockRepository.getLocation() } returns null

        createViewModel()

        assertEquals(R.id.action_initFragment_to_locationFragment, vm.navigate.first())
    }

    @Test
    fun init_whenRepoHasALocation_navigatesToMainScreen() = runTest {
        coEvery { mockRepository.getLocation() } returns "12345"

        createViewModel()

        assertEquals(R.id.action_initFragment_to_mainFragment, vm.navigate.first())
    }
}