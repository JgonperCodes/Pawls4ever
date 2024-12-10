package com.example.pawls4ever.screens.pets
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.pawls4ever.model.Pet
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PetsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    //Cargar mascotas del usuario
    fun loadPets(userId: String) {
        if (userId.isEmpty()) return

        _isLoading.value = true
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val petRefs = userDoc.get("pets") as? List<DocumentReference> ?: emptyList()

                if (petRefs.isEmpty()) {
                    _pets.value = emptyList() // No pets for this user
                    _isLoading.value = false
                } else {
                    fetchPetsDetails(petRefs)
                }
            }
            .addOnFailureListener { e ->
                Log.e("PetsViewModel", "Failed to load user pets: ${e.message}")
                _pets.value = emptyList()
                _isLoading.value = false
            }
    }

    //Cargar datos de las mascotas del usuario
    private fun fetchPetsDetails(petRefs: List<DocumentReference>) {
        val petTasks = petRefs.map { it.get() }

        Tasks.whenAllSuccess<DocumentSnapshot>(petTasks)
            .addOnSuccessListener { snapshots ->
                val petList = snapshots.mapNotNull { doc ->
                    val pet = doc.toObject(Pet::class.java)
                    if (pet != null) {
                        pet.copy(petId = doc.id)
                    } else null
                }
                _pets.value = petList
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("PetsViewModel", "Failed to fetch pets: ${e.message}")
                _pets.value = emptyList()
                _isLoading.value = false
            }
    }

    //Añadir la mascota
    fun addPet(userId: String, pet: Pet, onAdded: (String) -> Unit) {
        val newPet = Pet(
            name = pet.name,
            age = pet.age,
            breed = pet.breed,
            gender = pet.gender,
            image = pet.image
        )

        firestore.collection("pets")
            .add(newPet.toMap())
            .addOnSuccessListener { petDoc ->
                addPetToUser(userId, petDoc) {
                    onAdded(petDoc.id) // Devolvemos el id de la mascota para luego poder editarlo
                }
            }
            .addOnFailureListener { e ->
                Log.e("PetsViewModel", "Fallo a la hora de añadir la mascota: ${e.message}")
            }
    }

    //Añadir la mascota al usuario
    private fun addPetToUser(userId: String, petDoc: DocumentReference, onComplete: () -> Unit) {
        firestore.collection("users")
            .document(userId)
            .update("pets", FieldValue.arrayUnion(petDoc))
            .addOnSuccessListener {
                loadPets(userId) // Recargamos el contenedor de mascotas
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e(
                    "PetsViewModel",
                    "Fallo a la hora de actualizar la mascota del usuario: ${e.message}"
                )
            }
    }

    //Actualizamos las mascotas
    fun updatePet(pet: Pet) {
        if (pet.petId.isEmpty()) return

        firestore.collection("pets")
            .document(pet.petId)
            .set(pet.toMap())
            .addOnSuccessListener {
                Log.d("PetsViewModel", "Mascota actualizada correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("PetsViewModel", "Fallo al actualizar la mascota: ${e.message}")
            }
    }

    //Borrar mascota
    fun deletePet(userId: String, pet: Pet) {
        if (pet.petId.isEmpty()) return
        val petDoc = firestore.collection("pets").document(pet.petId)

        petDoc.delete()
            .addOnSuccessListener {
                removePetFromUser(userId, petDoc)
            }
            .addOnFailureListener { e ->
                Log.e("PetsViewModel", "Fallo al intentar borrar la mascota: ${e.message}")
            }
    }

    private fun removePetFromUser(userId: String, petDoc: DocumentReference) {
        firestore.collection("users")
            .document(userId)
            .update("pets", FieldValue.arrayRemove(petDoc))
            .addOnSuccessListener {
                loadPets(userId)
            }
            .addOnFailureListener { e ->
                Log.e("PetsViewModel", "Fallo al actualizar la mascota: ${e.message}")
            }
    }
}