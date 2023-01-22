package com.androidandrew.sunscreen.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.UserClothing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserSettingsRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UserSettingsRepository
    private lateinit var databaseHolder: FakeDatabaseWrapper

    private val isOnboarded = true
    private val location = "12345"
    private val skinType = 6
    private val clothing = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.PANTS)
    private val spf = 15
    private val isOnSnowOrWater = true

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
        repository = UserSettingsRepositoryImpl(
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
        repository.setIsOnboarded(isOnboarded)
        repository.setLocation(location)
        repository.setSkinType(skinType)
        repository.setClothing(clothing)
        repository.setSpf(spf)
        repository.setIsOnSnowOrWater(isOnSnowOrWater)

        val actualIsOnboarded = repository.getIsOnboarded()
        val actualLocation = repository.getLocation()
        val actualSkinType = repository.getSkinType()
        val actualClothing = repository.getClothing()
        val actualSpf = repository.getSpf()
        val actualIsOnSnowOrWater = repository.getIsOnSnowOrWater()

        assertEquals(isOnboarded, actualIsOnboarded)
        assertEquals(location, actualLocation)
        assertEquals(skinType, actualSkinType)
        assertEquals(clothing, actualClothing)
        assertEquals(spf, actualSpf)
        assertEquals(isOnSnowOrWater, actualIsOnSnowOrWater)
    }

    @Test
    fun getFlow_thenSet_getsUpdatedFlowValue() = runTest {
        val isOnboardedFlow = repository.getIsOnboardedFlow()
        val locationFlow = repository.getLocationFlow()
        val skinTypeFlow = repository.getSkinTypeFlow()
        val clothingFlow = repository.getClothingFlow()
        val spfFlow = repository.getSpfFlow()
        val isOnSnowOrWaterFlow = repository.getIsOnSnowOrWaterFlow()

        repository.setIsOnboarded(isOnboarded)
        repository.setLocation(location)
        repository.setSkinType(skinType)
        repository.setClothing(clothing)
        repository.setSpf(spf)
        repository.setIsOnSnowOrWater(isOnSnowOrWater)

        assertEquals(isOnboarded, isOnboardedFlow.first())
        assertEquals(location, locationFlow.first())
        assertEquals(skinType, skinTypeFlow.first())
        assertEquals(clothing, clothingFlow.first())
        assertEquals(spf, spfFlow.first())
        assertEquals(isOnSnowOrWater, isOnSnowOrWaterFlow.first())
    }

    @Test
    fun getValues_whenNotSet_areDefaultsNotNull() = runTest {
        val isOnboarded = repository.getIsOnboarded()
        val location = repository.getLocation()
        val skinType = repository.getSkinType()
        val clothing = repository.getClothing()
        val spf = repository.getSpf()
        val isOnSnowOrWater = repository.getIsOnSnowOrWater()

        assertNotNull(isOnboarded)
        assertNotNull(location)
        assertNotNull(skinType)
        assertNotNull(clothing)
        assertNotNull(spf)
        assertNotNull(isOnSnowOrWater)
    }

    @Test
    fun getValuesFlow_whenNotSet_areDefaultsNotNull() = runTest {
        val isOnboarded = repository.getIsOnboardedFlow()
        val location = repository.getLocationFlow()
        val skinType = repository.getSkinTypeFlow()
        val clothing = repository.getClothingFlow()
        val spf = repository.getSpfFlow()
        val isOnSnowOrWater = repository.getIsOnSnowOrWaterFlow()

        assertNotNull(isOnboarded.first())
        assertNotNull(location.first())
        assertNotNull(skinType.first())
        assertNotNull(clothing.first())
        assertNotNull(spf.first())
        assertNotNull(isOnSnowOrWater.first())
    }
}