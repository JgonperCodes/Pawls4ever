package com.example.pawls4ever.screens.newMemory
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawls4ever.model.Note
import com.example.pawls4ever.model.Pet
import com.example.pawls4ever.navigation.ImageUploader
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

class NewMemoryScreenViewModel : ViewModel() {
    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    //Obtenemos la nota mediante su id declarado en firebase
    fun getNoteById(noteId: String?): StateFlow<Note?> {
        if (!noteId.isNullOrEmpty() && _note.value == null) {
            viewModelScope.launch {
                try {
                    Log.d("NewMemoryScreenVM", "Intentando conseguir la nota con ID: $noteId")
                    val document = FirebaseFirestore.getInstance()
                        .collection("notes")
                        .document(noteId.split("/").last())
                        .get()
                        .await()

                    val fetchedNote = document.toObject(Note::class.java)?.copy(noteId = document.reference.path)
                    Log.d("NewMemoryScreenVM", "Nota conseguida: $fetchedNote")
                    _note.value = fetchedNote
                } catch (e: Exception) {
                    Log.e("NewMemoryScreenVM", "Error obteniendo las notas: ${e.message}")
                }
            }
        }
        return _note
    }
    //Obtenemos las mascotas mediante el usuario
    fun getPetsForUser(userId: String): StateFlow<List<Pet>> {
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
                val petReferences = userDoc["pets"] as? List<DocumentReference> ?: emptyList()
                // Obtenemos las mascotas mediante la coleccion de mascotas
                val pets = petReferences.mapNotNull { petRef ->
                    petRef.get().await().toObject(Pet::class.java)?.copy(petId = petRef.id)
                }
                _pets.value = pets
            } catch (e: Exception) {
                Log.e("NewMemoryScreenVM", "Error obteniendo las mascotas: ${e.message}")
            }
        }
        return _pets
    }

    //Subir imagen a Imgur mediante archivo de utilidad
    fun uploadImageToImgur(context: Context, uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes != null) {
                    ImageUploader.uploadImageToImgur(imageBytes, onResult)
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("NewMemoryScreenVM", "Error subiendo la imagen: ${e.message}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    //Guardar o actualizar nota del usuario
    fun saveNoteForUser(userId: String, note: Note) {
        viewModelScope.launch {
            try {
                val notesCollection = FirebaseFirestore.getInstance().collection("notes")
                val noteRef = if (note.noteId.isNotEmpty()) {
                    // Actualiza la nota si ya existe
                    FirebaseFirestore.getInstance().document(note.noteId)
                } else {
                    // O crea una nueva si está no existe
                    notesCollection.document()
                }

                // Guarda los datos de la nota actual
                val updatedNote = note.copy(
                    noteId = noteRef.path,
                    date = note.date ?: Date() // Asigna la Date actual al guardarse a la nota
                )

                // Guarda la nota al array de referencias de la nota
                noteRef.set(updatedNote).await()
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("usernotes", FieldValue.arrayUnion(noteRef))
                    .await()

                Log.d("NewMemoryScreenVM", "Nota guardada correctamente")
            } catch (e: Exception) {
                Log.e("NewMemoryScreenVM", "Error guardando la nota: ${e.message}")
            }
        }
    }


    //Borrar la imagen de la nota
    fun removeImageFromNote(noteId: String?, imageUrl: String) {
        viewModelScope.launch {
            try {
                noteId?.let {
                    val noteRef = FirebaseFirestore.getInstance().collection("notes").document(noteId)
                    noteRef.update("images", FieldValue.arrayRemove(imageUrl)).await()
                    Log.d("NewMemoryScreenVM", "Imagen borrada correctamente")
                }
            } catch (e: Exception) {
                Log.e("NewMemoryScreenVM", "Error removing image: ${e.message}")
            }
        }
    }

    //Borrar nota del usuario
    fun deleteNoteForUser(userId: String, noteId: String) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .document(noteId)
                    .delete()
                    .await()

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("usernotes", FieldValue.arrayRemove(FirebaseFirestore.getInstance().document(noteId)))
                    .await()

                Log.d("NewMemoryScreenVM", "Nota borrada correctamente")
            } catch (e: Exception) {
                Log.e("NewMemoryScreenVM", "Error borrando la nota: ${e.message}")
            }
        }
    }
}