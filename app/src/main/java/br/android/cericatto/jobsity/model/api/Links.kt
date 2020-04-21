package br.android.cericatto.jobsity.model.api

data class Links(
    val show: CastCreditLink? = CastCreditLink(),
    val character: CastCreditLink? = CastCreditLink()
)