package com.example.weatherforecastapplication.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecastapplication.data.local.db.CityDatabase
import com.example.weatherforecastapplication.data.models.entities.CityLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
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

    @Test
    fun insertLocation_savesToDatabase_returnsListWithOneItem() = runTest {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        dao.insertLocation(location)

        val list = dao.getAllFavoriteLocations().first()

        assertThat(list.size, `is`(1))
        assertThat(list[0].cityName, `is`("Cairo"))
    }

    @Test
    fun deleteLocation_existingLocation_removesFromDatabase() = runTest {
        val location = CityLocation(lat = 30.0, lon = 31.0, cityName = "Cairo")
        dao.insertLocation(location)

        val insertedList = dao.getAllFavoriteLocations().first()
        val itemToDelete = insertedList[0]

        dao.deleteLocation(itemToDelete)

        val listAfterDelete = dao.getAllFavoriteLocations().first()

        assertThat(listAfterDelete.isEmpty(), `is`(true))
    }

    @Test
    fun getAllFavoriteLocations_emptyDatabase_returnsEmptyList() = runTest {
        val list = dao.getAllFavoriteLocations().first()

        assertThat(list.isEmpty(), `is`(true))
    }
}