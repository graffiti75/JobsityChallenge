package br.android.cericatto.jobsity.model.db

import androidx.lifecycle.LiveData
import androidx.room.*
import br.android.cericatto.jobsity.model.api.Shows

@Dao
interface ShowsDao {
    @Insert
    fun insert(show: Shows): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(shows: List<Shows>): List<Long>

    @Query("SELECT * FROM Shows")
    fun getAll(): LiveData<List<Shows>>

    @Query("SELECT * FROM Shows WHERE favorite = 1")
    fun getFavorites(): LiveData<List<Shows>>

    @Query("SELECT * FROM Shows WHERE name LIKE '%' || :search || '%'")
    fun getShowByName(search: String?): LiveData<List<Shows>>

    @Query("SELECT * FROM Shows ORDER BY name ASC")
    fun getShowByTitle(): LiveData<List<Shows>>

    @Query("SELECT * FROM Shows WHERE id = :id")
    fun getShow(id: Int): LiveData<Shows>

    @Delete
    fun delete(show: Shows): Int

    @Query("DELETE FROM Shows")
    fun deleteAll()

    @Update
    fun update(show: Shows)

    @Query("SELECT count(*) FROM Shows")
    fun getShowsCount(): Int
}