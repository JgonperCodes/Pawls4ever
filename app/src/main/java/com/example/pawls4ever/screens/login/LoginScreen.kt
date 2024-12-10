package com.example.pawls4ever.screens.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawls4ever.R
import com.example.pawls4ever.navigation.Screens
import com.example.pawls4ever.ui.theme.Roboto
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginScreenViewModel? =  androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val showLoginForm = rememberSaveable { mutableStateOf(true) }

    // Google Sing Up con nuestro token
    val context = LocalContext.current
    val token = "431150964514-c8alhnu8t3e3c7mgukr0lvk6f4c5n84b.apps.googleusercontent.com"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel?.signInWithGoogleCredential(credential) { success, isNewUser, userExists ->
                if (success) {
                    when { //Mensaje a la hora de iniciar de nuevo sesión con Google o crear cuenta
                        isNewUser -> {
                            Toast.makeText(context, "Bienvenido a Pawls4Ever!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.HomeScreen.name)
                        }
                        userExists -> {
                            Toast.makeText(context, "Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.HomeScreen.name)
                        }
                        else -> {
                            Toast.makeText(context, "Error al crear la cuenta.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Error con Login de Google", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (ex: ApiException) {
            Log.d("MyLogin", "GoogleSignIn failed: ${ex.statusCode}")
        }
    }
//Detalle pata con surface y Box para que no cuente su espacio para el resto de los componentes
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFDE9D9)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = "Detalle Pata",
                modifier = Modifier.size(80.dp).padding(5.dp).align(Alignment.TopEnd)
            )
        }
//Columna que contiene el resto de componentes
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (showLoginForm.value) "¡Bienvenido de nuevo!" else "Comienza a escribir tus aventuras con tu compañero",
                style = TextStyle(
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF6C4B5E),
                modifier = Modifier.padding( top =  if(!showLoginForm.value) 50.dp else 50.dp,  start = if(showLoginForm.value) 90.dp else 0.dp),
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Letras Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Seccion del form
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color(0xFFC98686),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = if (showLoginForm.value) 16.dp else 2.dp)
                        .verticalScroll(rememberScrollState()) //Para Scrollear la Columna con los campos y no toda la pantalla
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showLoginForm.value) "Log in" else "Sign Up",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Black,
                                fontSize = if (showLoginForm.value) 45.sp else 50.sp
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(start = 16.dp)
                        )

                        if (!showLoginForm.value) {
                            Box(
                                modifier = Modifier.size(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.singupimage),
                                    contentDescription = "Imagen de Sign-Up",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        if (showLoginForm.value) {
                            Image(
                                painter = painterResource(id = R.drawable.loginimage),
                                contentDescription = "Imagenes LoginySingUp",
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }

                    // El form para sing up o login in dependiendo del boolean que manda el usuario
                    UserForm(isCreateAccount = !showLoginForm.value) { email, password, name ->
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                Toast.makeText(context, "Por favor, ingresa un correo válido", Toast.LENGTH_SHORT).show()
                                return@UserForm
                            }
                            if (password.length < 6) {
                                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                                return@UserForm
                            }
                            if (!showLoginForm.value && name.isNullOrEmpty()) {
                                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                                return@UserForm
                            }
                            if (showLoginForm.value) {
                                viewModel?.signInWithEmailAndPassword(email, password) { success ->
                                    if (success) {
                                        navController.navigate(Screens.HomeScreen.name)
                                    } else {
                                        Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                viewModel?.createUserWithEmailAndPassword(email, password, name) { success, errorMessage ->
                                    if (success) {
                                        navController.navigate(Screens.HomeScreen.name)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            errorMessage ?: "Error al crear cuenta",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }


                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign-In Fila
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Transparent)
                                .clickable {
                                    val googleSignInOptions = GoogleSignInOptions
                                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(token) //Para que al clickar nos salga el form de Google
                                        .requestEmail()
                                        .build()
                                    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
                                    launcher.launch(googleSignInClient.signInIntent)
                                },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Icon",
                                modifier = Modifier.size(60.dp).padding(8.dp)
                            )
                            Text(
                                text = if (showLoginForm.value) "Login con Google" else "Sign Up con Google",
                                style = TextStyle(
                                    fontFamily = Roboto,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 20.sp
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }


                    Spacer(modifier = Modifier.height(10.dp))

                    // Switch entre Login/Sign Up texto
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val text1 =
                            if (showLoginForm.value) "¿No tienes cuenta?" else "¿Ya tienes cuenta?"
                        val text2 = if (showLoginForm.value) "Sign Up" else "Log in"
                        Text(
                            text = text1,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Normal,
                            ),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = text2,
                            style = TextStyle(
                                fontFamily = Roboto,
                                fontWeight = FontWeight.Bold,
                            ), color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.clickable {
                                showLoginForm.value = !showLoginForm.value
                            }
                        )
                    }
                }
            }
        }
    }
}

//Formulario del usuario que cambia en base el boolean al pulsar Sing up o Log out
@Composable
fun UserForm(isCreateAccount: Boolean, onDone: (String, String, String?) -> Unit) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val nombre = rememberSaveable { mutableStateOf("") }
    val passwordVisible = rememberSaveable { mutableStateOf(false) }
    val validInput = remember(email.value, password.value, nombre.value) {
        email.value.trim().isNotEmpty() &&
                password.value.trim().isNotEmpty() &&
                (if (isCreateAccount) nombre.value.trim().isNotEmpty() else true)
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isCreateAccount) {
            Text(
                text = "Nombre",
                color = Color.White,
                style = TextStyle(
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Normal,
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 65.dp, top = 2.dp),
                fontSize = 18.sp
            )
            NombreInput(nombreState = nombre)
        }

        Text(
            text = "Email",
            color = Color.White,
            style = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 65.dp, top = 10.dp),
            fontSize = 18.sp
        )
        EmailInput(emailState = email)

        Text(
            text = "Password",
            color = Color.White,
            style = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 65.dp, top = 10.dp),
            fontSize = 18.sp
        )
        PasswordInput(passwordState = password, passwordVisible = passwordVisible)

        SubmitButton(
            textId = if (isCreateAccount) "Crear cuenta" else "Iniciar sesión",
            inputValido = validInput
        ) {
            Log.d("UserForm", "Submit clicked")
            onDone(
                email.value.trim(),
                password.value.trim(),
                if (isCreateAccount) nombre.value.trim() else null
            )
            keyboardController?.hide()
        }
    }
}

//Funcion para hacer boton prestablecido
@Composable
fun SubmitButton(
    textId: String,
    inputValido: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            Log.d("SubmitButton", "Button clickado")
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 80.dp, end = 80.dp),
        shape = RoundedCornerShape(50),
        enabled = inputValido,
        colors = ButtonDefaults.buttonColors(
            contentColor = Color(0xFFF2B880),
            containerColor = Color(0xFFF2B880),
            disabledContentColor = Color(0xFFF2B880),
            disabledContainerColor = Color(0xFFF2B880),
        )
    ) {
        Text(
            text = textId,
            color = Color.White,
            style = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
            ),
            modifier = Modifier.padding(8.dp),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

//Inputs de cada fields
@Composable
fun NombreInput(nombreState: MutableState<String>,
                labelId: String = "Nombre"
) {
    InputField(
        valuestate = nombreState,
        labelId = labelId,
        keyboardType = KeyboardType.Text
    )
}

@Composable
fun EmailInput(
    emailState: MutableState<String>,
    labelId: String = "Email"
) {
    InputField(
        valuestate = emailState,
        labelId = labelId,
        keyboardType = KeyboardType.Email
    )
}

//Modelo prestablecido de field
@Composable
fun InputField(
    valuestate: MutableState<String>,
    labelId: String,
    keyboardType: KeyboardType,
    isSingleLine: Boolean = true,
) {
    TextField(
        value = valuestate.value,
        onValueChange = { valuestate.value = it },
        label = {
            Text(
                text = labelId, color = Color(0xFFE7CFBC), style = TextStyle(
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp
                )
            )
        },
        singleLine = isSingleLine,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, top = 10.dp),
        shape = RoundedCornerShape(50),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            errorTextColor = Color.Black,
            focusedContainerColor = Color(0xFFFFF4EC),
            unfocusedContainerColor = Color(0xFFFFF4EC),
            disabledContainerColor = Color(0xFFFFF4EC),
            errorContainerColor = Color(0xFFFFF4EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

//Password input prestablecido
@Composable
fun PasswordInput(
    passwordState: MutableState<String>,
    labelId: String = "Contraseña",
    passwordVisible: MutableState<Boolean>
) {
    val visualTransformation = if (passwordVisible.value)
        VisualTransformation.None
    else PasswordVisualTransformation()

    TextField(
        value = passwordState.value,
        onValueChange = { passwordState.value = it },
        label = {
            Text(
                text = labelId, color = Color(0xFFE7CFBC), style = TextStyle(
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp
                )
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, top = 10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = visualTransformation,
        trailingIcon = {
            PasswordVisibleIcon(passwordVisible)
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            errorTextColor = Color.Black,
            focusedContainerColor = Color(0xFFFFF4EC),
            unfocusedContainerColor = Color(0xFFFFF4EC),
            disabledContainerColor = Color(0xFFFFF4EC),
            errorContainerColor = Color(0xFFFFF4EC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

//Para mostrar el icono de visibilidad y cambiar el texto de passsword
@Composable
fun PasswordVisibleIcon(
    passwordVisible: MutableState<Boolean>,
    iconColor: Color = Color(0xFF6D213C)
) {
    val image = if (passwordVisible.value)
        Icons.Default.VisibilityOff
    else
        Icons.Default.Visibility

    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
        Icon(
            imageVector = image,
            contentDescription = "Toggle Password Visibility",
            tint = iconColor
        )
    }
}

