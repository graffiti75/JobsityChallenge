package br.android.cericatto.jobsity.model.api

data class Schedule(
    val time: String,
    val days: List<String>?
)