package com.example.pawls4ever.model

data class Pet(
    val petId: String = "",
    val age: Int = 0,
    val breed: String = "",
    val gender: Boolean = true,
    val image: String? = null,
    val name: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "age" to this.age,
            "petId" to this.petId,
            "breed" to this.breed,
            "gender" to this.gender,
            "image" to this.image,
            "name" to this.name
        )
    }
}
