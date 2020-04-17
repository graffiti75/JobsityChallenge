package br.android.cericatto.jobsity.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.android.cericatto.jobsity.model.api.Shows

@Database(version = 1, entities = [Shows::class])
@TypeConverters(CustomTypeConverter::class)
/*
abstract class AppDatabase : RoomDatabase() {
    abstract fun showsDao(): ShowsDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app-database").build()
    }
}
*/
abstract class AppDatabase : RoomDatabase() {
    abstract fun showsDao(): ShowsDao

    companion object {
        var TEST_MODE = false
        private const val databaseName = "app-database"

        private var db: AppDatabase? = null
        private var dbInstance: ShowsDao? = null

        fun getInstance(context: Context): ShowsDao {
            if (dbInstance == null) {
                if (TEST_MODE) {
                    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                        .allowMainThreadQueries().build()
                    dbInstance = db?.showsDao()
                } else {
                    db = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
                    dbInstance = db?.showsDao()
                }
            }
            return dbInstance!!
        }

        fun close() {
            db?.close()
        }
    }
}