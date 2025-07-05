package com.example.network

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.network.databinding.ActivityLoginEmailBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class Login_email : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding
    private lateinit var fireAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fireAuth = FirebaseAuth.getInstance()
        comprobarSeccion() // Verifica si el usuario ya está autenticado

        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.INVISIBLE

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.network.R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.IniciarGoogle.setOnClickListener {
            googleLogin()
        }

        binding.BtnIngresar.setOnClickListener {
            validarInfo()
        }

        binding.TvRecuperar.setOnClickListener {
            startActivity(Intent(this@Login_email, RecuperarPassword::class.java))
        }

        binding.TxtRegistrarme.setOnClickListener {
            startActivity(Intent(this@Login_email, Registro_Email::class.java)) // Cambiado a Registro_Email
        }
    }

    private fun googleLogin() {
        val GoogleSignInIntent = mGoogleSignInClient.signInIntent
        GoogleSignInARL.launch(GoogleSignInIntent)
    }

    private val GoogleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                autenticacionGoogle(cuenta.idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en el inicio de sesión de Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun autenticacionGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        fireAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultAuth ->
                if (resultAuth.additionalUserInfo!!.isNewUser) {
                    llenarinfodn()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error en la autenticación de Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun llenarinfodn() {
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE

        val obtenertiempo = constantes.ObtenerTiempoDispositivo()
        val emailUsuario = fireAuth.currentUser!!.email
        val idUsuario = fireAuth.uid
        val nombreUsuario = fireAuth.currentUser?.displayName

        val hashmap = HashMap<String, Any>()
        hashmap["Nombre"] = "$nombreUsuario"
        hashmap["codigoTelefono"] = ""
        hashmap["telefono"] = ""
        hashmap["urlImagenPerfil"] = ""
        hashmap["proveedor"] = "Google"
        hashmap["escribiendo"] = ""
        hashmap["tiempo"] = obtenertiempo
        hashmap["online"] = true
        hashmap["email"] = "$emailUsuario"
        hashmap["uid"] = "$idUsuario"
        hashmap["fecha_nac"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(idUsuario!!)
            .setValue(hashmap)
            .addOnSuccessListener {
                progressBar.visibility = View.INVISIBLE
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this, "No se registró debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var email = ""
    private var password = ""

    private fun validarInfo() {
        email = binding.EtEmail.text.toString().trim()
        password = binding.EtPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmail.error = "Correo Electrónico Inválido"
            binding.EtEmail.requestFocus()
        } else if (email.isEmpty()) {
            binding.EtEmail.error = "Por favor, Ingresa un correo"
            binding.EtEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.EtPassword.error = "Por favor ingresa una contraseña"
            binding.EtPassword.requestFocus()
        } else {
            loginUsuario()
        }
    }

    private fun loginUsuario() {
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE

        fireAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressBar.visibility = View.INVISIBLE
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this, "No se pudo iniciar sesión debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun comprobarSeccion() {
        if (fireAuth.currentUser != null) {
            // Si el usuario ya está autenticado, redirigir a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() // Finaliza la actividad actual
        }
    }
}
