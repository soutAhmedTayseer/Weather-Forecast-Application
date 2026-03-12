package com.example.weatherforecastapplication.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecastapplication.data.local.db.CityDatabase
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityLocationDaoTest {

    private lateinit var database: CityDatabase
    private lateinit var dao: CityLocationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CityDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.cityLocationDao()
    }

    @After
    fun teardown() = database.close()

    // Test 1
    @Test
    fun insertLocation_savesToDatabase_returnsInFlow() = runTest {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        dao.insertLocation(location)

        val list = dao.getAllFavoriteLocations().first()
        assertEquals(1, list.size)
        assertEquals("Cairo", list[0].cityName)
    }

    // Test 2 (🚨 FIXED!)
    @Test
    fun deleteLocation_removesFromDatabase() = runTest {
        // 1. Insert the raw location
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        dao.insertLocation(location)

        // 2. Fetch the inserted item so we have its REAL database ID!
        val insertedList = dao.getAllFavoriteLocations().first()
        val itemToDelete = insertedList[0]

        // 3. Delete using the fetched item
        dao.deleteLocation(itemToDelete)

        // 4. Assert it is now empty
        val listAfterDelete = dao.getAllFavoriteLocations().first()
        assertTrue(listAfterDelete.isEmpty())
    }

    // Test 3
    @Test
    fun getAllFavoriteLocations_returnsEmptyList_whenDatabaseIsEmpty() = runTest {
        val list = dao.getAllFavoriteLocations().first()
        assertTrue(list.isEmpty())
    }
}