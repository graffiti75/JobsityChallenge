package br.android.cericatto.jobsity.model.api

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Shows(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var uid: Int = 0,
    val id: Int = 1,
    val name: String? = "Under the Dome",
    val image: Image? = Image(),
    val schedule: Schedule = Schedule(),
    val genres: List<String>? = listOf("Drama", "Science-Fiction", "Thriller"),
    val summary: String? = "<p><b>Under the Dome</b> is the story of a small town that is suddenly " +
        "and inexplicably sealed off from the rest of the world by an enormous transparent dome. " +
        "The town's inhabitants must deal with surviving the post-apocalyptic conditions while " +
        "searching for answers about the dome, where it came from and if and when it will go away.</p>",
    var officialSite: String? = "http://www.cbs.com/shows/under-the-dome/",
    var favorite: Boolean = false
)