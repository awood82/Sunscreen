package com.androidandrew.sunscreen.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SunscreenDatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var databaseHolder: FakeDatabaseWrapper

    @Before
    fun setup() {
        databaseHolder = FakeDatabaseWrapper()
//        databaseHolder.clearDatabase()
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
        val userSetting =
            UserSetting(UserSettingsDao.LOCATION,
                "12345")
        databaseHolder.db.userSettingsDao.insert(userSetting)

        val dbSetting = databaseHolder.db.userSettingsDao.getOnce(UserSettingsDao.LOCATION)
        assertEquals("12345", dbSetting?.value)
    }

    @Test
    fun getSync_thenInsert_getsUpdatedFlowSetting() = runTest {
        val userSetting =
            UserSetting(UserSettingsDao.LOCATION,
                "12345")
        databaseHolder.db.userSettingsDao.insert(userSetting)

        val dbSetting = databaseHolder.db.userSettingsDao.getOnce(UserSettingsDao.LOCATION)

        assertEquals("12345", dbSetting?.value)
    }
}