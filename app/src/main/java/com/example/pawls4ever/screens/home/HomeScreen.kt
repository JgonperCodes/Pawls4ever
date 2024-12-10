package com.example.pawls4ever.screens.home

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import com.example.pawls4ever.navigation.BottomNavigationBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.pawls4ever.model.Note
import com.example.pawls4ever.model.Pet
import com.example.pawls4ever.model.User
import androidx.navigation.NavHostController
import com.example.pawls4ever.navigation.Screens
import com.example.pawls4ever.ui.theme.Roboto
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val viewModel: HomeScreenViewModel = viewModel()

    val user by viewModel.user.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isGlobalLoading by viewModel.isGlobalLoading.collectAsState()

    val context = LocalContext.current
    var isUploadingImage by remember { mutableStateOf(false) }

    //TOP APPBAR Y BOTTOM AP BAR
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
                            "Home",
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
        Box(
            modifier = Modifier
                .padding(padding)
                .background(Color(0xFFF2B880))
                .fillMaxSize()
        ) {
            if (isGlobalLoading) {
                // Loading de la pantalla para que no se muestre como se carga la pantalla
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(80.dp)
                    )
                }
            } else {
                // Contenido principal de la pantalla
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Permite Scrollear
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    user?.let { nonNullUser ->
                        ProfileSection(
                            user = nonNullUser,
                            isUploadingImage = isUploadingImage,
                            onImageChange = { uri ->
                                isUploadingImage = true
                                viewModel.updateProfileImage(context, uri) { success ->
                                    isUploadingImage = false
                                    if (!success) {
                                        Toast.makeText(
                                            context,
                                            "Error al subir la imagen",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            onNameEdit = { newName -> viewModel.updateUserName(newName) },
                            onLogout = {
                                viewModel.signOut()
                                navController.navigate("LoginScreen") {
                                    popUpTo(0) // Limpia la ruta de salida
                                }
                            }
                        )
                    } ?: run {
                        // Placeholder por si el usuario no carga bien
                        Text("Cargando perfil...", color = Color.Black, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    //Seccion mascotas
                    PetsSection(
                        pets = pets,
                        navController = navController
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    //Seccion notas
                    NotesSection(
                        notes = notes,
                        navController = navController,
                        userId = user?.userId ?: "",
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

//Seccion del perfil del usuario
@Composable
fun ProfileSection(
    user: User,
    isUploadingImage: Boolean,
    onImageChange: (Uri) -> Unit,
    onNameEdit: (String) -> Unit,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember {
        mutableStateOf(
            user.name ?: "Sin nombre"
        )
    } //Por si no se detectase bien el nombre, ponemos uno de `placeholder

    LaunchedEffect(user.name) {
        name = user.name ?: "Sin nombre"
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                onImageChange(uri)
            }
        }

    //Almacenamos los datos de la imagen de perfil en un crad
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC98686)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 2.dp, bottom = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            //Imagen, nombre, edit y log out en esta fila
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Imagen de perfil
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { imagePickerLauncher.launch("image/*") } // Hace que se ejecute el image picker al clickar en la foto
                ) {
                    //Formato imagen
                    Image(
                        painter = rememberImagePainter(
                            data = user.profileImage,
                            builder = { crossfade(true) }
                        ),
                        contentDescription = "Profile Image",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    //Para enseñar la carga en la imagen mientras se sube
                    if (isUploadingImage) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color.White,
                                strokeWidth = 4.dp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))

                // Nombre y Email
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    //Cuando se pulsa el icono de lapiz, se muestra el textfield para editar el nombre
                    if (isEditing) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                Text(
                                    "Introduce tu nombre", style = TextStyle(
                                        fontFamily = Roboto,
                                        fontWeight = FontWeight.Light,
                                    )
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color(0xFFFFF4EC),
                                unfocusedContainerColor = Color(0xFFFFF4EC),
                                focusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            if (name.isNotBlank()) {
                                onNameEdit(name)
                                isEditing = false
                            }
                        }) {
                            Text("Guardar")
                        }
                    } else {
                        Text(
                            text = name,
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        //El email de lusuario
                        Text(
                            text = user.email ?: "",
                            color = Color.Gray,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
                Column {     // Icono para editar y volver a true el modo edicion de nombre del usuario
                    IconButton(
                        onClick = { isEditing = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = Color.White
                        )
                    }

                    // Log out
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D213C)),
                        modifier = Modifier
                            .size(33.dp)
                            .padding(start = 5.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

//Seccion de Mascotas
@Composable
fun PetsSection(pets: List<Pet>, navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        TubeText(
            text = "Tus Mascotas",
            textColor = Color.White,
            backgroundColor = Color(0xFF6D213C)
        )
        Spacer(modifier = Modifier.height(8.dp))

        //Card que se enseña por default si el usuario no ha almacenado ninguna mascota
        if (pets.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable { //Para que te lleve a crear una nueva mascota
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val userId = currentUser?.uid ?: ""
                        navController.navigate("${Screens.PetsScreen.name}/$userId")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E5))
            ) {
                //Componentes que almacena la card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //Imagen y texto para añadir
                        Image(
                            painter = rememberImagePainter("https://i.imgur.com/3jqQjz5.jpeg"),
                            contentDescription = "Add Pet",
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Añade a una mascota",
                            color = Color.Black,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            //Si no esta vacio el contenedor de pets, carga la funcion de añadir las cards con los pets existentes
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(pets) { pet ->
                    PetCard(pet = pet, navController = navController)
                }
            }
        }
    }
}

//Funcion para dar formato a las card de pets
@Composable
fun PetCard(pet: Pet, navController: NavController) {
    Card(
        modifier = Modifier
            .width(145.dp)
            .height(180.dp)
            .clickable { //Para que te lleve al pulsar a la pantalla de mascotas
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid ?: ""
                navController.navigate("${Screens.PetsScreen.name}/$userId")
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                // Imagen de mascota
                Image(
                    painter = rememberImagePainter(
                        data = pet.image ?: "https://i.imgur.com/3jqQjz5.jpeg"
                    ),
                    contentDescription = "Pet Image",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Nombre y detalles
                Text(
                    text = pet.name,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = pet.breed,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = "${pet.age} años",
                    color = Color.Gray,
                    style = TextStyle(
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Normal,
                    )
                )
            }
        }
    }
}

//Seccion de notas
@Composable
fun NotesSection(
    notes: List<Note>,
    navController: NavController,
    userId: String,
    viewModel: HomeScreenViewModel,
) {
    val homeScreenHandle = navController.currentBackStackEntry?.savedStateHandle
    val refreshHome = homeScreenHandle?.getLiveData<Boolean>("refreshHome")?.observeAsState()

    // Para recargar la nota más nueva por si ha editado
    LaunchedEffect(refreshHome?.value) {
        if (refreshHome?.value == true) {
            viewModel.loadUserData()
            homeScreenHandle?.set("refreshHome", false)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TubeText(
            text = "Último recuerdo añadido",
            textColor = Color.White,
            backgroundColor = Color(0xFF6D213C)
        )
        Spacer(modifier = Modifier.height(8.dp))

        //Nota placeholder por si fallaran la creacion default de notas y no hubieran
        if (notes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = rememberImagePainter("https://i.imgur.com/ztTHkog.jpeg"),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text("Empieza a guardar tus recuerdos con tu mejor amigo!", color = Color.Black)
                }
            }
        } else {
            val latestNote = notes.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Log.d(
                            "NotesSection",
                            "Navigating to NewMemoryScreen with noteId=${latestNote.noteId} and userId=$userId"
                        )
                        navController.navigate(
                            "${Screens.NewMemoryScreen.name}/$userId?isViewing=true&noteId=${latestNote.noteId}"
                        )
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = rememberImagePainter(latestNote.images.firstOrNull() ?: ""),
                        contentDescription = "Memory Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(latestNote.date ?: Date()),
                        color = Color.Gray
                    )
                    Text(
                        latestNote.title,
                        color = Color.Black,
                        style = TextStyle(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}


//Para dar formato a las etiquetas
@Composable
fun TubeText(text: String, textColor: Color, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
    }
}