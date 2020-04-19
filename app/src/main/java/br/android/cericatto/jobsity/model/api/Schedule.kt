package br.android.cericatto.jobsity.model.api

data class Schedule(
    val time: String? = "22:00",
    val days: List<String>? = listOf("Thursday")
)