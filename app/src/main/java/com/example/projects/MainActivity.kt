package com.example.projects

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projects.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // Binding para acceder a las vistas del layout de forma segura y eficiente
    private lateinit var binding: ActivityMainBinding
    // Instancia de Firebase Authentication para manejar el registro de usuarios
    private lateinit var auth: FirebaseAuth
    // Instancia de Firebase Firestore para guardar datos del usuario
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflamos el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilitamos el modo Edge-to-Edge (para que el contenido se extienda bajo las barras del sistema)
        enableEdgeToEdge()

        // Ajustamos los paddings para evitar que el contenido se superponga con las barras de navegación y estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializamos Firebase Authentication y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Botón para regresar a la pantalla anterior
        binding.backButton.setOnClickListener {
            finish()
        }

        // Botón de registro que llama a la función principal
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    // Función principal que maneja el registro de usuarios
    private fun registerUser() {
        // Obtenemos los valores ingresados por el usuario y eliminamos espacios en blanco
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()

        // Validaciones
        // Verificamos que ningún campo esté vacío
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Validamos que el email tenga un formato correcto
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Validamos que la edad sea un número entre 1 y 120
        val age = ageStr.toIntOrNull()
        if (age == null || age < 1 || age > 120) {
            Toast.makeText(this, "Ingresa una edad válida (1-120)", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificamos que las contraseñas coincidan
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificamos que la contraseña tenga al menos 6 caracteres
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostramos el ProgressBar y desactivamos el botón para evitar múltiples clics
        showLoading(true)


        //REGISTRO CON FIREBASE
        // Creamos el usuario con email y contraseña en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid == null) {
                        showLoading(false)
                        Toast.makeText(this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    // Datos que se guardarán en Firestore (sin fecha de registro)
                    val usuario = hashMapOf(
                        "nombre" to name,
                        "email" to email,
                        "edad" to age,
                    )

                    // Guardamos los datos del usuario en la colección "usuarios" de Firestore
                    db.collection("usuarios")
                        .document(uid)
                        .set(usuario)
                        .addOnSuccessListener {
                            // Actualizamos el displayName del usuario en Firebase Auth
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { profileTask ->
                                    showLoading(false)

                                    if (profileTask.isSuccessful) {
                                        Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                                        finish() // Solo salimos si todo salió bien
                                    } else {
                                        Toast.makeText(this, "Usuario creado, pero no se pudo actualizar el nombre", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            //eliminar el usuario de Auth si falla Firestore
                        }
                } else {
                    // Error al crear el usuario (email ya existe, contraseña débil, etc.)
                    showLoading(false)
                    val errorMsg = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "Error al registrarse: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Muestra u oculta el ProgressBar y habilita/deshabilita el botón de registro
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }
}