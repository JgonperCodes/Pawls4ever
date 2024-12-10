package com.example.pawls4ever.screens.allMemorys
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawls4ever.model.Note
import com.example.pawls4ever.model.Pet
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AllMemorysScreenViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val _isLoadingNotes = MutableStateFlow(false)
    private val _isLoadingPets = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = combine(_isLoadingNotes, _isLoadingPets) { notesLoading, petsLoading ->
        notesLoading || petsLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true) //Para luego indicar si se está cargando los datos y mostrar la animación

    //Recuperar las notas del usuario
    fun fetchNotes(userId: String) {
        _isLoadingNotes.value = true
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val noteReferences = userDoc["usernotes"] as? List<DocumentReference> ?: emptyList()

                val notes = noteReferences.mapNotNull { noteRef ->
                    val document = noteRef.get().await()
                    document.toObject(Note::class.java)?.copy(noteId = noteRef.id)
                }.sortedByDescending { it.date }

                _notes.value = notes
            } catch (e: Exception) {
                Log.e("AllMemorysScreenVM", "Error al obtener las notas: ${e.message}")
            } finally {
                _isLoadingNotes.value = false
            }
        }
    }

    //Para obtnere las mascotas
    fun fetchPets(userId: String) {
        _isLoadingPets.value = true
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val petReferences = userDoc["pets"] as? List<DocumentReference> ?: emptyList()

                val pets = petReferences.mapNotNull { petRef ->
                    petRef.get().await().toObject(Pet::class.java)?.copy(petId = petRef.id)
                }

                _pets.value = pets
            } catch (e: Exception) {
                Log.e("AllMemorysScreenVM", "Error obteniendo las mascotas: ${e.message}")
            } finally {
                _isLoadingPets.value = false
            }
        }
    }
}
