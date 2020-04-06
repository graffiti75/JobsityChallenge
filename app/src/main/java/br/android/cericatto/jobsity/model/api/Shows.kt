package br.android.cericatto.jobsity.model.api

data class Shows(
    val id: Int,
    val name: String,
    val image: Image,
    val schedule: Schedule,
    val genres: List<String>?,
    val summary: String
)