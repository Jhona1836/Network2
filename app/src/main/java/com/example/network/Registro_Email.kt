package com.example.network

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.network.databinding.ActivityRegistroEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registro_Email : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroEmailBinding
    private lateinit var progresDialog: ProgressDialog
    private lateinit var fireAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fireAuth = FirebaseAuth.getInstance()

        // Animación de carga
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Manten la Calma, estamos trabajando")
        progresDialog.setCanceledOnTouchOutside(false)

        binding.btnregistrar.setOnClickListener {
            validarInfo()
        }
    }

    private var email = ""
    private var pass = ""
    private var r_pass = ""

    private fun validarInfo() {
        email = binding.EtEmail.text.toString().trim()
        pass = binding.EtPassword.text.toString().trim()
        r_pass = binding.etrpass.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.EtEmail.error = "Email Invalido" //Si no presenta el formato correcto de email lo hace invalido
            binding.EtEmail.requestFocus() //Se centra en el Edittext mal
        }
        else if (email.isEmpty()){
            binding.EtEmail.error = "Ingrese un correo electronico"
            binding.EtEmail.requestFocus()
        }
        else if (r_pass.isEmpty()){
            binding.EtPassword.error = "Ingrese una contraseña"
            binding.EtPassword.requestFocus()
        }
        else if (pass.isEmpty()){
            binding.etrpass.error = "Repita la  contraseña"
            binding.etrpass.requestFocus()
        }
        else if(pass != r_pass){
            binding.etrpass.error = "Las contraseñas no coinciden"
            binding.etrpass.requestFocus()
        }
        else{
            registrarUsuario()
        }


    }

    private fun registrarUsuario() {
        progresDialog.setMessage("Creando cuenta")
        progresDialog.show()

        fireAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                llenarinfoDB()

            }
            .addOnFailureListener{e->
                progresDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se registro el usuario debido a ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun llenarinfoDB() {

        progresDialog.setMessage("Guardando Informacion")

        val obtenertiempo = constantes.ObtenerTiempoDispositivo()
        val emailUsuario = fireAuth.currentUser!!.email
        val idUsuario = fireAuth.uid

        val hashmap = HashMap<String, Any>()

        hashmap["Nombre"] = ""
        hashmap["codigoTelefono"] = ""
        hashmap["telefono"] = ""
        hashmap["urlImagenPerfil"] = ""
        hashmap["proveedor"] = "Email"
        hashmap["escribiendo"] = ""
        hashmap["tiempo"] = obtenertiempo
        hashmap["online"] = true
        hashmap["email"] = "${emailUsuario}"
        hashmap["uid"] = "${idUsuario}"
        hashmap["fecha_nac"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(idUsuario!!)
            .setValue(hashmap)
            .addOnSuccessListener {
                progresDialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()

            }
            .addOnFailureListener{ e->
                progresDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se registro debido a ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}