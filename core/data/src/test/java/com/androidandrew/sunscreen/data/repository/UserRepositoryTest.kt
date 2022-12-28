package com.androidandrew.sunscreen.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
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
class UserRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UserRepository
    private lateinit var databaseHolder: FakeDatabaseWrapper

    private val location = "12345"
    private val spf = 15
    private val isOnSnowOrWater = true

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
        repository = UserRepositoryImpl(
            userTrackingDao = databaseHolder.userTrackingDao,
            userSettingsDao = databaseHolder.userSettingsDao
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
        repository.setLocation(location)
        repository.setSpf(spf)
        repository.setIsOnSnowOrWater(isOnSnowOrWater)

        val actualLocation = repository.getLocation()
        val actualSpf = repository.getSpf()
        val actualIsOnSnowOrWater = repository.getIsOnSnowOrWater()

        assertEquals(location, actualLocation)
        assertEquals(spf, actualSpf)
        assertEquals(isOnSnowOrWater, actualIsOnSnowOrWater)
    }

    @Test
    fun getFlow_thenSet_getsUpdatedFlowValue() = runTest {
        val locationFlow = repository.getLocationFlow()
        val spfFlow = repository.getSpfFlow()
        val isOnSnowOrWaterFlow = repository.getIsOnSnowOrWaterFlow()

        repository.setLocation(location)
        repository.setSpf(spf)
        repository.setIsOnSnowOrWater(isOnSnowOrWater)

        assertEquals(location, locationFlow.first())
        assertEquals(spf, spfFlow.first())
        assertEquals(isOnSnowOrWater, isOnSnowOrWaterFlow.first())
    }
}