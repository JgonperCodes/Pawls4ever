package com.example.pawls4ever.screens.newMemory
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.pawls4ever.model.Note
import com.example.pawls4ever.navigation.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pawls4ever.model.Pet
import com.example.pawls4ever.ui.theme.Roboto

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMemoryScreen(
    navController: NavController,
    userId: String,
    isViewing: Boolean = false,
    noteId: String? = null
) {
    val viewModel: NewMemoryScreenViewModel = viewModel()
    val pets by viewModel.getPetsForUser(userId).collectAsState(initial = emptyList())
    val noteState by viewModel.getNoteById(noteId).collectAsState(initial = null)
    val context = LocalContext.current
    // Variables mutables para resto de campos
    var isEditing by remember { mutableStateOf(!isViewing) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var happyMemory by remember { mutableStateOf(0) }
    var favorite by remember { mutableStateOf(false) }
    var images by remember { mutableStateOf(mutableListOf<String>()) }
    var selectedPets by remember { mutableStateOf(mutableListOf<Pet>()) }
    var showPetDialog by remember { mutableStateOf(false) }
    var isImageUploading by remember { mutableStateOf(false) }


    // Picker de la imagen para subir la imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isImageUploading = true
            viewModel.uploadImageToImgur(context, uri) { uploadedImageUrl ->
                isImageUploading = false
                if (uploadedImageUrl != null) {
                    images.add(uploadedImageUrl)
                } else {
                    Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Conseguimos los datos al cargar la nota, obtenemos el id
    LaunchedEffect(noteState, pets) {
        noteState?.let { note ->
            title = note.title
            content = note.content
            happyMemory = note.happyMemory
            favorite = note.favorite
            images = note.images.toMutableList()
            val petIds = note.petTag.map { it.id }
            Log.d("NewMemoryScreen", "Notas con tags de mascotas: $petIds")
            selectedPets = pets.filter { pet -> petIds.contains(pet.petId) }.toMutableList()
            Log.d("NewMemoryScreen", "Mascotas seleccionadas: $selectedPets")
        }
    }

    //TOP APP BAR Y BOTTOM BAR
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
                            if (isViewing && !isEditing) "Recuerdo" else "Nuevo Recuerdo",
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
                actions = {
                    if (isViewing && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF4EC)
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        //Fondo que cambia en base si se esta en edición o viendo la nota
        containerColor = if (!isViewing) Color(0xFFF2B880) else Color(0xFFC98686)
    ) { padding ->
        if (isViewing && noteState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        //Columna que almacena el resto de los componentes
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Card que almacena los componentes de la "hoja" de texto, para que prezca una nota
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        //Slider para cargar la felicidad del recuerdo de 0 a 10
                        Text(
                            text = "Felicidad del recuerdo: ",
                            color = Color.Black,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                            )
                        )
                        Slider(
                            value = happyMemory.toFloat(),
                            onValueChange = { happyMemory = it.toInt() },
                            valueRange = 0f..10f,
                            steps = 10,
                            modifier = Modifier.weight(1f),
                            enabled = isEditing,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6D213C),
                                activeTrackColor = Color(0xFFC98686),
                                inactiveTrackColor = Color(0xFFF2B880),
                                activeTickColor = Color(0xFF6D213C),
                                inactiveTickColor = Color(0xFFFFD6A8)
                            )
                        )
                        Text(
                            text = " $happyMemory/10", color = Color.Black, style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Input para el titulo
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = {
                            //Variable para que el titulo del texto desaparezca, muy importante
                            if (title.isEmpty()) {
                                Text(
                                    "Título",
                                    color = Color.Black,
                                    style = TextStyle(
                                        fontFamily = Roboto,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 25.sp,
                                        textAlign = TextAlign.Start
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEditing,
                        textStyle = TextStyle(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                        ), colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Gray,
                            errorContainerColor = Color.Transparent,
                            errorIndicatorColor = Color.Red
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Contenido de la nota
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        label = {
                            //Misma condición para poder visualizar la nota
                            if (content.isEmpty()) {
                                Text(
                                    "Contenido",
                                    color = Color.Black,
                                    style = TextStyle(
                                        fontFamily = Roboto,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Start
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        placeholder = { Text("Escribe el contenido aquí...") },
                        enabled = isEditing,
                        maxLines = Int.MAX_VALUE,
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
                            errorIndicatorColor = Color.Red
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seccion de imagenes
                    Text(
                        text = "Imágenes: ",
                        style = TextStyle(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = Color.Black,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(images.size) { index ->
                            val imageUrl = images[index]
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Gray)
                                    .clickable(enabled = isEditing) { //Clickando en la imagen se borra la imagen clickada
                                        images = images
                                            .toMutableList()
                                            .apply { remove(imageUrl) }
                                        viewModel.removeImageFromNote(noteState?.noteId, imageUrl)
                                    }
                            ) {
                                Image(
                                    painter = rememberImagePainter(imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        if (isImageUploading) {
                            // Mientras la imagen carga se muestra la imacion encima del contenedor
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 4.dp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isEditing) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B880))
                        ) {
                            Text(if (isImageUploading) "Subiendo..." else "Añadir Imagenes")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Switch recuerdo Favorito
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Favorito: ", color = Color.Black, style = TextStyle(
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = favorite, onCheckedChange = { favorite = it }, enabled = isEditing,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF6D213C),
                        uncheckedThumbColor = Color(0xFFB0BEC5),
                        checkedTrackColor = Color(0xFFFFECB3),
                        uncheckedTrackColor = Color(0xFFCFD8DC),
                        disabledCheckedThumbColor = Color(0xFF6D213C).copy(alpha = 0.4f),
                        disabledUncheckedThumbColor = Color(0xFFB0BEC5).copy(alpha = 0.4f),
                        disabledCheckedTrackColor = Color(0xFFFFECB3).copy(alpha = 0.4f),
                        disabledUncheckedTrackColor = Color(0xFFCFD8DC).copy(alpha = 0.4f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            //Fila que contiene la etiquetación de mascotas
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mascotas etiquetadas:",
                        style = TextStyle(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start
                        ),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    // Para mostrar las mascotas etiquetadas
                    if (selectedPets.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedPets) { pet ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF6D213C))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pet.name,
                                        style = TextStyle(
                                            fontFamily = Roboto,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (isViewing) "No hay mascotas etiquetadas." else "No hay mascotas seleccionadas.",
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            ),
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seccion para mostrar el botón de etiquetar solo editando
                    if (isEditing) {
                        Button(
                            onClick = { showPetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7CFBC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Elegir Mascotas", color = Color.White)
                        }

                        // Seccion para etiquetar las mascotas obteniendo las mascotas
                        if (showPetDialog) {
                            AlertDialog(
                                onDismissRequest = { showPetDialog = false },
                                confirmButton = {
                                    Button(
                                        onClick = { showPetDialog = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFFE7CFBC
                                            )
                                        )
                                    ) {
                                        Text("Hecho", color = Color.White)
                                    }
                                },
                                title = {
                                    Text(
                                        text = "Seleccionar Mascotas",
                                        style = TextStyle(
                                            fontFamily = Roboto,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                text = {
                                    LazyColumn {
                                        items(pets) { pet ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (selectedPets.contains(pet)) {
                                                            selectedPets =
                                                                selectedPets
                                                                    .toMutableList()
                                                                    .apply { remove(pet) }
                                                        } else {
                                                            selectedPets =
                                                                selectedPets
                                                                    .toMutableList()
                                                                    .apply { add(pet) }
                                                        }
                                                    }
                                                    .padding(8.dp)
                                            ) {
                                                Checkbox(
                                                    checked = selectedPets.contains(pet),
                                                    onCheckedChange = {
                                                        if (it) {
                                                            selectedPets =
                                                                selectedPets.toMutableList()
                                                                    .apply { add(pet) }
                                                        } else {
                                                            selectedPets =
                                                                selectedPets.toMutableList()
                                                                    .apply { remove(pet) }
                                                        }
                                                    }
                                                )
                                                Text(
                                                    text = pet.name,
                                                    style = TextStyle(
                                                        fontFamily = Roboto,
                                                        fontWeight = FontWeight.Normal,
                                                        fontSize = 16.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            //Botones de borrar y guardar
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isEditing) {
                    // Solo se enseña el botón de borrar cuando se esta viendo la nota
                    if (noteState != null) {
                        Button(
                            onClick = {
                                noteState?.let { note ->
                                    viewModel.deleteNoteForUser(userId, note.noteId)
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "refresh",
                                        true
                                    )
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D213C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Eliminar", color = Color.White, style = TextStyle(
                                    fontFamily = Roboto,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                )
                            )
                        }
                    }
                } else {
                    // Solo se enseña el botón de guardar cuando se esta viendo la nota
                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                // Para que no se guarden notas vacias
                                Toast.makeText(
                                    context,
                                    "El título y contenido no pueden estar vacíos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                //Guardamos las notas en el usuario y su referencia
                                viewModel.saveNoteForUser(
                                    userId,
                                    noteState?.copy(
                                        title = title,
                                        content = content,
                                        happyMemory = happyMemory,
                                        favorite = favorite,
                                        images = images,
                                        petTag = selectedPets.map {
                                            FirebaseFirestore.getInstance().collection("pets")
                                                .document(it.petId)
                                        }
                                    ) ?: Note(
                                        title = title,
                                        content = content,
                                        happyMemory = happyMemory,
                                        favorite = favorite,
                                        images = images,
                                        petTag = selectedPets.map {
                                            FirebaseFirestore.getInstance().collection("pets")
                                                .document(it.petId)
                                        }
                                    )
                                )
                            }
                            // Señal para refrescar el resto de las pantallas
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "refreshHome",
                                true
                            )
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "refreshAllMemories",
                                true
                            )
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (noteState == null) Color(
                                0xFFC98686
                            ) else Color(0xFFF2B880)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Guardar Recuerdo", style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}