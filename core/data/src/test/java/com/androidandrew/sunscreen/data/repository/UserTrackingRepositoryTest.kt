package com.androidandrew.sunscreen.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.database.UserTracking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserTrackingRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UserTrackingRepository
    private lateinit var databaseHolder: FakeDatabaseWrapper

    private val date = FakeData.localDate.toString()
    private val userTracking = UserTracking(
        date = date,
        burnProgress = 20.0,
        vitaminDProgress = 500.0
    )

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
        repository = UserTrackingRepositoryImpl(
            userTrackingDao = databaseHolder.userTrackingDao
        )
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        runBlocking {
            databaseHolder.tearDown()
        }
    }

    @Test
    fun insert_thenGet_retrievesValue() = runTest {
        repository.setUserTracking(userTracking)

        val actualUserTracking = repository.getUserTracking(date)

        assertEquals(userTracking, actualUserTracking)
    }

    @Test
    fun getFlow_thenSet_getsUpdatedFlowValue() = runTest {
        val userTrackingFlow = repository.getUserTrackingFlow(date)

        repository.setUserTracking(userTracking)

        assertEquals(userTracking, userTrackingFlow.first())
    }
}