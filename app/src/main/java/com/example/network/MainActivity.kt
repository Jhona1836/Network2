package com.example.network

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import  com.example.network.Anuncios.crearAnuncio
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.network.Fragmentos.FragmentAgregar
import com.example.network.Fragmentos.FragmentChats
import com.example.network.Fragmentos.FragmentMisAnuncios
import com.example.network.Fragmentos.FragmentInicio
import com.example.network.Fragmentos.FragmentPerfil

import com.example.network.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Una manera mas rapida de poder relacionar el diseño con las
        //funciones sin necesidad de incluir el FindView.R.id
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()
        verFragmentInicio()


        binding.ButtonNV.setOnItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.pInicio -> {
                    verFragmentInicio()
                    true
                }

                R.id.pChat -> {
                    verFragmentChat()
                    true
                }

                R.id.pAgregar -> {
                    verFragmentAgregar()
                    true
                }

                R.id.pGuardado -> {
                    verFragmentGuardado()
                    true
                }

                R.id.pPerfil -> {
                    verfragmentPerfil()
                    true
                }

                else -> {
                    false
                }
            }

        }

            binding.FAB.setOnClickListener{
                val intent = Intent(this, crearAnuncio::class.java)
                intent.putExtra("Edicion", false)
                startActivity(intent)

            }
    }



    private fun comprobarSesion(){
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(this, Login_email::class.java))
            finishAffinity()
        }else{
            agregarFcmToken()
            solicitarPermisoNotificacion()
        }
    }

    private fun solicitarPermisoNotificacion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED){
                permisoNotificacion.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val permisoNotificacion =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ esConcedido->
            //Aqui se concede el permiso
        }


    private fun verFragmentInicio() {
        binding.TituloRL.text = "Inicio"
        val fragment = FragmentInicio()
       val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.Fragment1.id, fragment, "Fragment Inicio")
       fragmentTransition.commit()

    }

    private fun verFragmentChat() {
        binding.TituloRL.text = "Chat"
        val fragment = FragmentChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.Fragment1.id, fragment, "Fragment Chat")
        fragmentTransition.commit()
    }

    private fun verFragmentAgregar() {
        binding.TituloRL.text = "Publicar"
        val fragment = FragmentAgregar()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.Fragment1.id, fragment, "Fragment Publicar")
        fragmentTransition.commit()
    }

    private fun verFragmentGuardado() {
        binding.TituloRL.text = "Guardado"
        val fragment = FragmentMisAnuncios()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.Fragment1.id, fragment, "Fragment Guardado")
        fragmentTransition.commit()
    }

    private fun verfragmentPerfil() {
        binding.TituloRL.text = "Perfil"
        val fragment = FragmentPerfil()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.Fragment1.id, fragment, "Fragment Perfil")
        fragmentTransition.commit()
    }

    private fun agregarFcmToken(){
        val miUid = "${firebaseAuth.uid}"

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {fcmToken->
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = "$fcmToken"
                val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
                ref.child(miUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        //El token se agregó con éxito
                    }
                    .addOnFailureListener {e->
                        Toast.makeText(
                            this,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
