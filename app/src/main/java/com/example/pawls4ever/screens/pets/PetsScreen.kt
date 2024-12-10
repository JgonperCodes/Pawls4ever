package com.example.pawls4ever.screens.pets

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.pawls4ever.model.Pet
import com.example.pawls4ever.navigation.BottomNavigationBar
import com.example.pawls4ever.navigation.ImageUploader
import com.example.pawls4ever.ui.theme.Roboto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(navController: NavHostController, userId: String) {
    val viewModel: PetsViewModel = viewModel()
    val pets by viewModel.pets.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    var editingPetId by remember { mutableStateOf<String?>(null) }

    // Cargamos las mascotas una vez se accede a la pantalla
    LaunchedEffect(userId) {
        viewModel.loadPets(userId)
    }

    //TOP BAR Y BOTTOM BAR
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Mis mascotas",
                            color = Color.Black,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF4EC)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        //Controles que enseña si se está editando una mascota o no
        Box(
            modifier = Modifier
                .padding(padding)
                .background(Color(0xFFC98686))
                .fillMaxSize()
        ) {
            //Mientras se carga en la base de datos los datos las mascotas, mostramos una animación ded carga
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(80.dp)
                    )
                }
            } else {
                //Columna con controles en modo de edición
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    pets.forEach { pet ->
                        PetForm(
                            pet = pet,
                            isEditing = pet.petId == editingPetId,
                            onSave = { updatedPet ->
                                if (updatedPet.petId.isEmpty()) {
                                    viewModel.addPet(userId, updatedPet) { newPetId ->
                                        editingPetId = newPetId // Actualizamos los datos del pet actual al que estamos editando
                                    }
                                } else {
                                    viewModel.updatePet(updatedPet)
                                }
                                editingPetId = null //Salimos de edición nada mas se guarda
                            },
                            onDelete = {
                                viewModel.deletePet(userId, pet)
                                if (pet.petId == editingPetId) editingPetId = null // Salimos del modo edición a la hora de borrar la mascota
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.addPet(userId, Pet()) { newPetId ->
                                editingPetId = newPetId // Abrimos el nuevo pet en modo edición para que no nos guarde uno vacio
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D213C))
                    ) {
                        Text("+ Añadir otra mascota", color = Color.White)
                    }
                }
            }
        }
    }
}




//Formato de los TextField del formulario
@Composable
fun RoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = TextStyle(
            fontFamily = Roboto,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Start
        )) },
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE7CFBC), shape = RoundedCornerShape(16.dp))
            .padding(4.dp),
        shape = RoundedCornerShape(35.dp),
        isError = isError,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Gray,
            errorContainerColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}

//Formulario del Pet
@Composable
fun PetForm(
    pet: Pet,
    isEditing: Boolean = false,
    onSave: (Pet) -> Unit,
    onDelete: () -> Unit
) {
    var isEditingState by remember { mutableStateOf(isEditing) }
    var name by remember { mutableStateOf(pet.name) }
    var age by remember { mutableStateOf(pet.age.toString()) }
    var breed by remember { mutableStateOf(pet.breed) }
    var gender by remember { mutableStateOf(pet.gender) }
    var imageUri by remember { mutableStateOf(pet.image) }
    var isUploading by remember { mutableStateOf(false) }
    val defaultImage = "https://i.imgur.com/3jqQjz5.jpeg"
    val context = LocalContext.current
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                isUploading = true

                // Para convertir los bytes de la imagen que subimos a una URI
                val contentResolver = context.contentResolver
                try {
                    val inputStream = contentResolver.openInputStream(selectedUri)
                    val imageBytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (imageBytes == null) {
                        isUploading = false
                        Toast.makeText(context, "Fallo al subir la imagen", Toast.LENGTH_SHORT).show()
                    } else {
                        //Subida de imagen
                        ImageUploader.uploadImageToImgur(imageBytes) { uploadedImageUrl ->
                            isUploading = false
                            if (uploadedImageUrl != null) {
                                imageUri = uploadedImageUrl
                            } else {
                                Toast.makeText(context, "Error subiendo la imagen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    isUploading = false
                    Toast.makeText(context, "Error leyendo la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

//Card que contienen los datos de los pets y el formulario
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Gray)
                        .clickable(enabled = isEditingState && !isUploading) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberImagePainter(imageUri ?: defaultImage),
                        contentDescription = "Pet Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isEditingState) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFD6A8), shape = CircleShape)
                            .clickable { isEditingState = true }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.align(Alignment.Center),
                            tint = Color.Black
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFD6A8), shape = CircleShape)
                            .padding(8.dp)
                    ) {
                        IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.Center)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            //Si está en modo edicion se muestran los text field con formato
            if (isEditingState) {
                RoundedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    isError = name.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                RoundedTextField(
                    value = age,
                    onValueChange = { age = it.filter { char -> char.isDigit() } },
                    label = "Edad",
                    isError = age.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                RoundedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = "Raza",
                    isError = breed.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sexo:")
                    //Boton Boolean para indicar sexo, no se pueden escoger dos a la vez
                    Row {
                        Button(
                            onClick = { gender = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!gender) Color(0xFF6D213C) else Color(0xFFFFD6A8)
                            )
                        ) {
                            Text("♂", color = if (!gender) Color.White else Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { gender = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gender) Color(0xFF6D213C) else Color(0xFFFFD6A8)
                            )
                        ) {
                            Text("♀", color = if (gender) Color.White else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                //Boton que mientras los campos no estén vacios, guarda la mascota en el usuario
                Button(
                    onClick = {
                        if (name.isNotBlank() && age.isNotBlank() && breed.isNotBlank()) {
                            onSave(
                                pet.copy(
                                    name = name,
                                    age = age.toIntOrNull() ?: 0,
                                    breed = breed,
                                    gender = gender,
                                    image = imageUri
                                )
                            )
                            isEditingState = false
                        } else {
                            Toast.makeText(context, "Completa todos los campos de la mascota.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D213C))
                ) {
                    Text("Guardar", color = Color.White)
                }
            } else {
                LabeledValue(label = "Nombre", value = name)
                LabeledValue(label = "Edad", value = age)
                LabeledValue(label = "Raza", value = breed)
                LabeledValue(label = "Sexo", value = if (gender) "Hembra" else "Macho")
            }
        }
    }
}



// Formulario en modo no edición, para mostrar los datos
@Composable
fun LabeledValue(label: String, value: String, backgroundColor: Color = Color(0xFFE7CFBC)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        )
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth()
        ) {
            Text(
                text = value,
                style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, color = Color.Black)
            )
        }
    }
}