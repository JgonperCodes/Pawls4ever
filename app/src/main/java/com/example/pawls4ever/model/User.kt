package com.example.pawls4ever.model
import com.google.firebase.firestore.DocumentReference

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val pets: List<DocumentReference> = emptyList(),
    val usernotes: List<DocumentReference> = emptyList()
) {
       fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "profileImage" to profileImage,
            "pets" to pets.map { it.path },
            "usernotes" to usernotes.map { it.path }
        )
    }
}
