package com.androidandrew.sunscreen.ui.location

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.util.LocationUtil
import io.mockk.coVerify
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
class LocationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: LocationViewModel
    private val mockRepository = mockk<UserRepositoryImpl>(relaxed = true)

    private fun createViewModel() {
        vm = LocationViewModel(mockRepository, LocationUtil())
    }

    @Test
    fun onSearchLocation_whenValidZipCode_navigatesToMainScreen() = runTest {
        createViewModel()

        vm.onSearchLocation("10001")

        assertEquals(R.id.action_locationFragment_to_mainFragment, vm.navigationId.first())
        coVerify { mockRepository.setLocation("10001") }
    }

    @Test
    fun onSearchLocation_whenInvalidZipCode_doesNotNavigate() = runTest {
        createViewModel()

        vm.onSearchLocation("1")

        assertEquals(0, vm.navigationId.first())
    }
}