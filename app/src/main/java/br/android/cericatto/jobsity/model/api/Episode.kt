package br.android.cericatto.jobsity.model.api

data class Episode(
    val id: Int,
    val name: String?,
    val season: Int?,
    val number: Int?,
    val summary: String?,
    val image: Image?
)