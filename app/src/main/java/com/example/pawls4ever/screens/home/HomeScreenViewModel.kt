package com.example.pawls4ever.screens.home
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.example.pawls4ever.model.Note
import com.example.pawls4ever.model.Pet
import com.example.pawls4ever.model.User
import com.example.pawls4ever.navigation.ImageUploader
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeScreenViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isPetsLoading = MutableStateFlow(true)
    private val _isNotesLoading = MutableStateFlow(true)

    val isGlobalLoading: StateFlow<Boolean> = combine(
        _isPetsLoading,
        _isNotesLoading
    ) { petsLoading, notesLoading ->
        petsLoading || notesLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid
            if (userId == null) {
                Log.e("HomeScreenViewModel", "El usuario no esta registrado o el ID es incorrecto")
                return@launch
            }

            try {
                val document = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (document.exists()) {
                    val user = document.toObject(User::class.java)?.copy(userId = userId) ?: User(userId = userId)
                    _user.value = user

                    loadPets(user.pets)
                    loadNotes(user.usernotes)
                } else { //Ultima medida si el usuario no se creado bien, se crea uno default con todo vacio
                    Log.d("HomeScreenViewModel", "No existe el usuario, se crea uno default")
                    _user.value = User(userId = userId)
                    _isPetsLoading.value = false
                    _isNotesLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error al cargar los datos: ${e.message}", e)
            }
        }
    }

    //Cargar las mascotas
    private fun loadPets(petRefs: List<DocumentReference>) {
        _isPetsLoading.value = true
        viewModelScope.launch {
            try {
                if (petRefs.isEmpty()) {
                    _pets.value = emptyList()
                } else {
                    val petList = petRefs.mapNotNull { petRef ->
                        petRef.get().await().toObject(Pet::class.java)?.copy(petId = petRef.id)
                    }
                    _pets.value = petList
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error al cargar las mascotas: ${e.message}")
            } finally {
                _isPetsLoading.value = false
            }
        }
    }

    //Cargar las notas
    private fun loadNotes(noteRefs: List<DocumentReference>) {
        _isNotesLoading.value = true
        viewModelScope.launch {
            try {
                if (noteRefs.isEmpty()) {
                    _notes.value = emptyList()
                } else {
                    val noteList = noteRefs.mapNotNull { noteRef ->
                        val document = noteRef.get().await()
                        document.toObject(Note::class.java)?.copy(noteId = noteRef.id)
                    }.sortedByDescending { it.date } // Ordenamos por fecha
                    _notes.value = noteList
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error al cargar las notas: ${e.message}")
            } finally {
                _isNotesLoading.value = false
            }
        }
    }

    //Sing out
    fun signOut() {
        Firebase.auth.signOut()
        FirebaseFirestore.getInstance().terminate()
        FirebaseFirestore.getInstance().clearPersistence()
        Log.d("HomeScreenViewModel", "Usuario loged out correctamente")
    }

    fun updateProfileImage(context: Context, imageUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes != null) {
                    ImageUploader.uploadImageToImgur(imageBytes) { uploadedImageUrl ->
                        if (uploadedImageUrl != null) {
                            val userId = Firebase.auth.currentUser?.uid ?: return@uploadImageToImgur
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update("profileImage", uploadedImageUrl)
                                .addOnSuccessListener {
                                    _user.value = _user.value?.copy(profileImage = uploadedImageUrl)
                                    onComplete(true)
                                }
                                .addOnFailureListener {
                                    onComplete(false)
                                }
                        } else {
                            onComplete(false)
                        }
                    }
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    //Actualizar el nombre del usuario
    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch
            if (newName.isBlank()) {
                Log.d("HomeScreenViewModel", "El nombre no puede ir vacio")
                return@launch
            }
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("name", newName)
                    .await()
                _user.value = _user.value?.copy(name = newName)
                Log.d("HomeScreenViewModel", "El nombre se ha actualizado correctamente")
            } catch (e: Exception) {
                Log.d("HomeScreenViewModel", "Error al actualizar el nombre: ${e.message}")
            }
        }
    }
}