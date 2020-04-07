package br.android.cericatto.jobsity.model.api

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Shows(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var uid: Int = 0,
    val id: Int,
    val name: String,
    val image: Image,
    val schedule: Schedule,
    val genres: List<String>?,
    val summary: String,
    var favorite: Boolean = false
)