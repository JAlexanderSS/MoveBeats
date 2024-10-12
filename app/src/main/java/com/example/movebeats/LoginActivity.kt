package com.example.movebeats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configuración de inicio de sesión con Google
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Reemplazar con el client ID correcto desde google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        firebaseAuth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya ha iniciado sesión
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Si el usuario ya está autenticado, redirigir a la MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Configurar el botón de inicio de sesión
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            signIn()
        }
    }

    // Iniciar el proceso de inicio de sesión con Google
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 100)
    }

    // Manejo del resultado del inicio de sesión
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error en el inicio de sesión", e)
                Toast.makeText(this, "Error en el inicio de sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Autenticación con Firebase usando Google
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Inicio de sesión exitoso, redirigir a la pantalla principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar si el usuario ya ha iniciado sesión
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Si el usuario ya está autenticado, redirigir a la MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
