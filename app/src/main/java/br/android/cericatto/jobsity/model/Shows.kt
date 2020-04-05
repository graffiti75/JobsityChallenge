package br.android.cericatto.jobsity.model

data class Shows(
    val id: Long,
    val name: String,
    val image: Image,
    val schedule: Schedule,
    val genres: List<String>?,
    val summary: String
)