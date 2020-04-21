package br.android.cericatto.jobsity.model.api

data class Person(
    val id: String = "172658",
    val name: String? = "Lauren Bush Lauren",
    val image: Image? = Image()
) {
    override fun toString(): String {
        return "Person(id='$id', name=$name, image=$image)"
    }
}