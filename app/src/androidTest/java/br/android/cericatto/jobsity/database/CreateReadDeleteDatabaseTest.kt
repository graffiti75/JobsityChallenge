package br.android.cericatto.jobsity.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.android.cericatto.jobsity.database.LiveDataTestUtil.getValue
import br.android.cericatto.jobsity.model.api.Shows
import br.android.cericatto.jobsity.model.db.AppDatabase
import br.android.cericatto.jobsity.model.db.ShowsDao
import org.junit.*
import org.junit.runner.RunWith

/**
 * Source:
 * https://medium.com/@chandilsachin/room-with-unit-test-in-kotlin-4ad31a39a291
 */
@RunWith(AndroidJUnit4::class)
class CreateReadDeleteDatabaseTest {
    private var showsDao: ShowsDao? = null

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true
        showsDao = AppDatabase.getInstance(ApplicationProvider.getApplicationContext())
    }

    @After
    fun closeDb() {
        AppDatabase.close()
    }

    @Test
    fun shouldInsertShows() {
        val shows = Shows(uid = 1, id = 1, name = "Under the Dome")
        showsDao?.insert(shows)
        val showsTest = getValue(showsDao?.getShow(shows.id)!!)
        Assert.assertEquals(shows.name, showsTest.name)
    }

    @Test
    fun shouldDeleteAllData(){
        showsDao?.deleteAll()
        Assert.assertEquals(showsDao?.getShowsCount(), 0)
    }
}