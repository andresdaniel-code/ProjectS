package com.example.projects

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

// Pantalla principal de Registro de Usuario usando Jetpack Compose
// Esta es la Activity que se muestra cuando se abre la aplicación.
class RegisterJetpackComposeActivity : ComponentActivity() {

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance() // Para manejar registro e inicio de sesión
    private val db = FirebaseFirestore.getInstance() // Para guardar datos en la base de datos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que la app use toda la panalla
        enableEdgeToEdge()

        setContent {
            MaterialTheme {  // Tema visual de Material Design 3
                RegisterScreen(
                    auth = auth,
                    db = db,
                    onRegisterSuccess = {
                        Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                        finish()  // Cierra esta pantalla
                    },
                    onRegisterError = { message ->
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

// Esta es la pantalla de registro construida con Jetpack Compose.
// lo que el usuario ve y puede interactuar está aquí.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onRegisterSuccess: () -> Unit, // Función que se ejecuta cuando el registro es exitoso
    onRegisterError: (String) -> Unit // Función que se ejecuta cuando hay un error
) {
    val context = LocalContext.current // Obtiene el contexto actual de la aplicación

    // Variables que guardan lo que el usuario escribe en cada campo
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) } // Controla si se muestra el círculo de carga
    var errorMessage by remember { mutableStateOf("") } // Guarda mensajes de error para mostrar al usuario

    // Columna que organiza todos los elementos uno debajo del otro
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .padding(24.dp) // Espacio alrededor de los elementos
            .systemBarsPadding(), // Respeta las barras de estado y navegación
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre cada elemento
    ) {
        // Título de la pantalla
        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineMedium
        )

        // Campo para el nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo para el correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo para la edad
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo para la contraseña (oculta los caracteres)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Campo para confirmar la contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Muestra mensaje de error si existe
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        // Botón de registro
        Button(
            onClick = {
                // Cuando el usuario presiona el botón, se ejecuta el registro
                registerUser(
                    name = name.trim(),
                    email = email.trim(),
                    ageStr = age.trim(),
                    password = password,
                    confirmPassword = confirmPassword,
                    isLoading = { isLoading = it },
                    onSuccess = onRegisterSuccess,
                    onError = { msg ->
                        errorMessage = msg
                        onRegisterError(msg)
                    },
                    auth = auth,
                    db = db,
                    context = context
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading // Desactiva el botón mientras está cargando
        ) {
            if (isLoading) {
                // Muestra círculo de progreso mientras se registra
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Registrarse")
            }
        }
    }
}

//Función que realiza proceso de registro:
//1. Valida los datos
//2. Crea el usuario en Firebase Authentication
//3. Guarda los datos en Firestore
//4. Actualiza el nombre del usuario
private fun registerUser(
    name: String,
    email: String,
    ageStr: String,
    password: String,
    confirmPassword: String,
    isLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    context: android.content.Context
) {

    // VALIDACIONES
    if (name.isEmpty() || email.isEmpty() || ageStr.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        onError("Completa todos los campos")
        return
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("Ingresa un correo válido")
        return
    }

    val age = ageStr.toIntOrNull()
    if (age == null || age < 1 || age > 120) {
        onError("Ingresa una edad válida (1-120)")
        return
    }

    if (password != confirmPassword) {
        onError("Las contraseñas no coinciden")
        return
    }

    if (password.length < 6) {
        onError("La contraseña debe tener al menos 6 caracteres")
        return
    }

    //PROCESO DE REGISTRO
    isLoading(true) // Muestra el círculo de carga

    // Crea el usuario con email y contraseña en Firebase
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener
                val uid = user.uid

                // Datos que se guardarán en Firestore
                val usuario = hashMapOf<String, Any>(
                    "nombre" to name,
                    "email" to email,
                    "edad" to age,
                    "fechaRegistro" to System.currentTimeMillis()
                )

                // Guarda los datos en la colección "usuarios"
                db.collection("usuarios").document(uid)
                    .set(usuario)
                    .addOnSuccessListener {
                        // Actualiza el nombre visible del usuario en Firebase Auth
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener {
                                isLoading(false)
                                onSuccess() // Registro completado con éxito
                            }
                    }
                    .addOnFailureListener {
                        isLoading(false)
                        onError("Error al guardar datos en Firestore")
                    }
            } else {
                isLoading(false)
                onError("Error al registrarse: ${task.exception?.message ?: "Error desconocido"}")
            }
        }
}