package com.example.pawls4ever.screens.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class LoginScreenViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

    fun signInWithGoogleCredential(
        credential: AuthCredential,
        callback: (Boolean, Boolean, Boolean) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val firestore = FirebaseFirestore.getInstance()

                    // Comprobamos si el usuario existe
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                callback(true, false, true)
                            } else {
                                // Creamos uno nuevo
                                val email = task.result?.user?.email ?: ""
                                val displayName =
                                    task.result?.user?.displayName ?: email.substringBefore("@")
                                createUser(displayName, email) { userCreated ->
                                    if (userCreated) { //Creado
                                        callback(true, true, false)
                                    } else {//No  creado
                                        callback(false, false, false)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            callback(false, false, false) // Fallo al comprobar existencia
                        }
                } else {
                    callback(false, false, false) //Fallo en el SingUp
                }
            }
    }

    //Registrarse con email y contraseña
    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        callback: (Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MyLogin", "Se ha registrado contraseña y email!")
                    callback(true)
                } else {
                    Log.d("MyLogin", "Error registrandose en: ${task.exception?.message}")
                    callback(false)
                }
            }
    }

    //Crear usuario y email
    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        name: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        if (_loading.value == true) return

        _loading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createUser(name, email) { userCreated ->
                        if (userCreated) {
                            signInWithEmailAndPassword(email, password) { success ->
                                callback(success, null)
                            }
                        } else {
                            callback(false, "Error al crear usuario")
                        }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    callback(false, errorMessage)
                }
                _loading.value = false
            }
    }

    private fun createUser(displayName: String?, email: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(false)
        val firestore = FirebaseFirestore.getInstance()

        // Definimos una nota por defecto para el nuevo usuario que se almacenara automaticamente
        val defaultNote = hashMapOf(
            "date" to Timestamp.now(),
            "images" to listOf("https://i.imgur.com/ztTHkog.jpeg"),
            "title" to "Empieza a guardar tus recuerdos con tu mejor amigo!",
            "content" to """
            Pantalla de Inicio: ¡Tu base de operaciones!
            Aquí es donde empieza toda la diversión.
            "Tus mascotas": Si todavía no has añadido a tu fiel compañero, haz clic en el adorable icono de perrito. ¡Es tan sencillo como darle un nombre, edad, raza y sexo!
            "Último recuerdo añadido": Aquí podrás ver el recuerdo más reciente que hayas guardado. Si aún no tienes recuerdos, no te preocupes, ¡es el momento de crear uno!

            Pantalla "Tus mascotas": ¡El club VIP de tu peludo!
            Añade información personalizada: Completa los datos de tu mascota y haz que cobre vida en la app. ¿Tienes más de un amigo peludo? ¡No hay problema! Toca el botón “+ Añadir otra mascota” para darles a todos su espacio en el estrellato.
            Editar detalles: Si te equivocas, dale al icono de lápiz para actualizar cualquier dato. ¡Tu mascota siempre al día!

            Pantalla "Todos los recuerdos": Donde los momentos mágicos se quedan para siempre
            "Últimos entradas": Aquí encontrarás los recuerdos más recientes que hayas creado. Revívelos cuando quieras con un simple clic.
            "Entradas importantes": Marca tus momentos favoritos como "importantes" y los tendrás siempre a la mano. ¡Los mejores recuerdos merecen ser destacados!

            Pantalla "Añadir un nuevo Recuerdo": ¡Haz magia con un clic!
            Título y descripción: Dale un título creativo a tu recuerdo y escribe sobre ese momento tan especial.
            Felicidad del recuerdo: Desliza el control de "felicidad" para calificar la intensidad del momento. ¿10/10 felicidad? ¡Genial!
            Elige mascota: Si tienes varias mascotas, selecciona cuál es la protagonista de este recuerdo.
            Favorito: ¿Este recuerdo es top? Activa el interruptor de “Favorito” para guardarlo en la sección de importantes.

            Iconos del menú: Navega como un pro
            Inicio (Casa): Vuelve al centro de mando de tu app.
            Tus Mascotas (Huella): Gestiona toda la información de tus amigos peludos.
            Crear Recuerdo (Lápiz): Captura esos momentos inolvidables al instante.
            Todos los Recuerdos (Libro): Explora, revive y organiza todos tus recuerdos.

            ¡Consejos Pro de la App!
            No olvides explorar y experimentar: La app está diseñada para ser tu diario digital de momentos felices con tus mascotas. No hay reglas, solo diversión.
            Hazlo tuyo: Personaliza todo, desde las fotos de tus mascotas hasta los detalles de tus recuerdos.
            Comparte la felicidad: Aunque la app es para ti, puedes contarle a tus amigos sobre esos momentos increíbles. ¡Los recuerdos compartidos siempre son mejores!
        """.trimIndent(),
            "favorite" to true,
            "happiness" to 0,
            "idNote" to "", // Luego actualizaremos esto
            "petTag" to emptyList<DocumentReference>()
        )

        //Añadimos la nota por defecto al usuario
        firestore.collection("notes").add(defaultNote)
            .addOnSuccessListener { noteRef ->
                // Actualizamos el id de Nota
                noteRef.update("idNote", noteRef.id)
                    .addOnSuccessListener {
                        // Creamos el usuario en la BD
                        val user = hashMapOf(
                            "userId" to userId,
                            "name" to (displayName
                                ?: email.substringBefore("@")),//Nombre por si se ha registrado con google, tenga al menos el del gmail pero sin el resto de la dirección
                            "email" to email,
                            "profileImage" to "https://i.imgur.com/0hBQ7gf.png",
                            "pets" to emptyList<DocumentReference>(),
                            "usernotes" to listOf(noteRef) // Añadimos la referencia al documento
                        )

                        //Guardamos el objeto al FireStore
                        firestore.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                Log.d(
                                    "MyLogin",
                                    "Useario creado correctamente con nota por defecto"
                                )
                                callback(true)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(
                                    "MyLogin",
                                    "Error al guardar el usuario: ${exception.message}"
                                )
                                callback(false)
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "MyLogin",
                            "Error actualizando el ID de la nota: ${exception.message}"
                        )
                        callback(false)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("MyLogin", "Error creando la nota por defecto: ${exception.message}")
                callback(false)
            }
    }
}