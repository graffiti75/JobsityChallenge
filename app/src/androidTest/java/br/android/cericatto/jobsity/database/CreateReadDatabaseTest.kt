package br.android.cericatto.jobsity.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.android.cericatto.jobsity.database.LiveDataTestUtil.getValue
import br.android.cericatto.jobsity.model.api.Shows
import br.android.cericatto.jobsity.model.db.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * Source:
 * https://code.luasoftware.com/tutorials/android/android-instrumented-unit-test-with-room-using-memory/
 */

@RunWith(AndroidJUnit4::class)
class CreateReadDatabaseTest {
    private lateinit var db: AppDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider
            .getApplicationContext(), AppDatabase::class.java)
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testIfEmpty() {
        val liveData = db.showsDao().getAll()
        val items = getValue(liveData)
        assertThat(items.size, Is(0))
    }

    @Test
    fun testInsertedValue() {
        var item = Shows(name="Under the Dome")
        assertThat(item.name, Is("Under the Dome"))

        db.apply {
            beginTransaction()
            showsDao().insert(item)
            setTransactionSuccessful()
            endTransaction()
        }

        val items = getValue(db.showsDao().getAll())
        assertThat(items.size, Is(1))

        item = items[0]
        assertThat(item.name, Is("Under the Dome"))
    }
}