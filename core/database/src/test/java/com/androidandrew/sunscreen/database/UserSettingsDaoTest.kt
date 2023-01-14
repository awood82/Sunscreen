package com.androidandrew.sunscreen.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sunscreen.database.entity.UserSettingEntity
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
class UserSettingsDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var databaseHolder: FakeDatabaseWrapper

    private val string = "12345"
    private val int = "15"
    private val boolean = "true"

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        runBlocking {
            databaseHolder.tearDown()
        }
    }

    @Test
    fun insert_thenGet_retrievesSetting() = runTest {
        databaseHolder.db.userSettingsDao.insert(
            UserSettingEntity(id = 1, value = string)
        )
        databaseHolder.db.userSettingsDao.insert(
            UserSettingEntity(id = 2, value = int)
        )
        databaseHolder.db.userSettingsDao.insert(
            UserSettingEntity(id = 3, value = boolean)
        )

        val stringSetting = databaseHolder.db.userSettingsDao.getOnce(1)
        val intSetting = databaseHolder.db.userSettingsDao.getOnce(2)
        val booleanSetting = databaseHolder.db.userSettingsDao.getOnce(3)

        assertEquals(string, stringSetting?.value)
        assertEquals(int, intSetting?.value)
        assertEquals(boolean, booleanSetting?.value)
    }

    @Test
    fun getFlow_thenInsert_getsUpdatedFlowSetting() = runTest {
        val stringFlow = databaseHolder.db.userSettingsDao.getDistinctFlow(1)
        val intFlow = databaseHolder.db.userSettingsDao.getDistinctFlow(2)
        val booleanFlow = databaseHolder.db.userSettingsDao.getDistinctFlow(3)

        databaseHolder.db.userSettingsDao.insert(
            UserSettingEntity(id = 1, value = string)
        )
        databaseHolder.db.userSettingsDao.insert(
            UserSettingEntity(id = 2, value = int)
        )
        databaseHolder.db.userSettingsDao.insert(
            UserSettingEntity(id = 3, value = boolean)
        )

        assertEquals(string, stringFlow.first()?.value)
        assertEquals(int, intFlow.first()?.value)
        assertEquals(boolean, booleanFlow.first()?.value)
    }
}