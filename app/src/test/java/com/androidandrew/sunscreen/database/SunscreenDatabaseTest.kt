package com.androidandrew.sunscreen.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabase
import com.androidandrew.sunscreen.util.getOrAwaitValue
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

    private lateinit var databaseHolder: FakeDatabase

    @Before
    fun setup() {
        databaseHolder = FakeDatabase()
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
        val userSetting = UserSetting(UserSettingsDao.LOCATION, "12345")
        databaseHolder.db.userSettingsDao.insert(userSetting)

        val dbSetting = databaseHolder.db.userSettingsDao.getOnce(UserSettingsDao.LOCATION)
        assertEquals("12345", dbSetting?.value)
    }

    @Test
    fun getSync_thenInsert_getsUpdatedLiveDataSetting() = runTest {
        val dbLive = databaseHolder.db.userSettingsDao.get(UserSettingsDao.LOCATION)

        val userSetting = UserSetting(UserSettingsDao.LOCATION, "12345")
        databaseHolder.db.userSettingsDao.insert(userSetting)

        assertEquals("12345", dbLive.getOrAwaitValue()?.value)
    }
}