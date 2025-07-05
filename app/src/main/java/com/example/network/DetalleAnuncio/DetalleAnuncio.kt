package com.example.network.DetalleAnuncio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.netwokr.Chat.ChatActivity
import com.example.network.Adaptadores.AdaptadorImagenSeleccionada
import com.example.network.Adaptadores.AdaptadorImgSlider
import com.example.network.Anuncios.crearAnuncio
import com.example.network.DetalleVendedor.DetalleVendedor
import com.example.network.MainActivity
import com.example.network.Modelo.ModeloAnuncio
import com.example.network.Modelo.ModeloImgSlider
import com.example.network.R
import com.example.network.constantes
import com.example.network.databinding.ActivityDetalleAnuncioBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetalleAnuncio : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleAnuncioBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnuncio = ""

    private var anuncioLatitud = 0.0
    private var anuncioLongitud = 0.0

    private var uidVendedor = ""
    private var telVendedor = ""

    private var favorito = false

    private lateinit var imagenSliderArrayList: ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleAnuncioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idAnuncio = intent.getStringExtra("idAnuncio") ?: ""


        //Funciones de los botones

        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        if (idAnuncio.isNotEmpty()) {

            comprobarAnuncioFav()
            cargarInfoAnuncio()
            cargarImgAnuncio()
        }

        binding.IbFav.setOnClickListener{
            if(favorito){
                constantes.eliminarAnuncioFav(this, idAnuncio)
            }
            else{
                constantes.agregarAnuncioFav(this,idAnuncio)
            }
        }

        binding.IbEliminar.setOnClickListener {
            val mAlertDialog = MaterialAlertDialogBuilder(this)
            mAlertDialog.setTitle("Eliminar anuncio")
                .setMessage("¿Estás seguro de eliminar este anuncio?")
                .setPositiveButton("Eliminar"){dialog, which->
                    eliminarAnuncio()
                }
                .setNegativeButton("Cancelar"){dialog, which->
                    dialog.dismiss()
                }.show()
        }

        binding.BtnMapa.setOnClickListener {
            constantes.mapaIntent(this, anuncioLatitud, anuncioLongitud)
        }

        binding.BtnLlamar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext,
                    android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    constantes.llamarIntent(this, numTel)
                }
            }else{
                permisoLlamada.launch(Manifest.permission.CALL_PHONE)
            }

        }

        binding.BtnSms.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext,
                    android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene un número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    constantes.smsIntent(this, numTel)
                }
            }else{
                permisoSms.launch(android.Manifest.permission.SEND_SMS)
            }

        }


        binding.BtnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            startActivity(intent)
        }



        binding.IbEditar.setOnClickListener{
            opcionesDialog()
        }

        binding.IvInfoVendedor.setOnClickListener {
            val intent = Intent(this, DetalleVendedor::class.java)
            intent.putExtra("uidVendedor", uidVendedor)
            Toast.makeText(this,"El uid del vendedor es ${uidVendedor}",Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

    }


    //Funciones

    private fun opcionesDialog() {
        val popupMenu = PopupMenu(this, binding.IbEditar)

        popupMenu.menu.add(Menu.NONE,0,0, "Editar")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {item->
            val itemId = item.itemId

            if (itemId == 0){
                //Editar
                val intent = Intent(this, crearAnuncio::class.java)
                intent.putExtra("Edicion", true)
                intent.putExtra("idAnuncio", idAnuncio)
                startActivity(intent)
            }

            return@setOnMenuItemClickListener true
        }

    }

    private fun cargarInfoAnuncio() {
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloAnuncio = snapshot.getValue(ModeloAnuncio::class.java) ?: return

                    uidVendedor = modeloAnuncio.uid
                    binding.TvTitulo.text = modeloAnuncio.titulo
                    binding.TvDescr.text = modeloAnuncio.descripcion
                    binding.TvDireccion.text = modeloAnuncio.direccion
                    binding.TvCat.text = modeloAnuncio.categoria
                    binding.TvVistas.text = modeloAnuncio.contadorVistas.toString()
                    binding.TvFecha.text = constantes.obtenerFecha(modeloAnuncio.tiempo)

                    anuncioLatitud = modeloAnuncio.latitud
                    anuncioLongitud = modeloAnuncio.longitud

                    if (uidVendedor == firebaseAuth.uid) {
                        mostrarOpcionesParaPropietario()
                    } else {
                        mostrarOpcionesParaUsuarios()
                    }

                    cargarInfoVendedor()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun mostrarOpcionesParaPropietario() {
        binding.IbEditar.visibility = View.VISIBLE
        binding.IbEliminar.visibility = View.VISIBLE
        binding.BtnMapa.visibility = View.GONE
        binding.BtnLlamar.visibility = View.GONE
        binding.BtnSms.visibility = View.GONE
        binding.BtnChat.visibility = View.GONE
        binding.TxtDescrVendedor.visibility = View.VISIBLE
        binding.perfilVendedor.visibility = View.VISIBLE
    }

    private fun mostrarOpcionesParaUsuarios() {
        binding.IbEditar.visibility = View.GONE
        binding.IbEliminar.visibility = View.GONE
        binding.BtnMapa.visibility = View.VISIBLE
        binding.BtnLlamar.visibility = View.VISIBLE
        binding.BtnSms.visibility = View.VISIBLE
        binding.BtnChat.visibility = View.VISIBLE
        binding.TxtDescrVendedor.visibility = View.VISIBLE
        binding.perfilVendedor.visibility = View.VISIBLE
    }

    private fun cargarInfoVendedor() {
        if (uidVendedor.isEmpty()) return

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val telefono = snapshot.child("telefono").value?.toString() ?: ""
                    val codTel = snapshot.child("codigoTelefono").value?.toString() ?: ""
                    val nombres = snapshot.child("nombres").value?.toString() ?: ""
                    val imagenPerfil = snapshot.child("urlImagenPerfil").value?.toString() ?: ""
                    val tiempoReg = snapshot.child("tiempo").value as? Long ?: 0L

                    telVendedor = "$codTel$telefono"
                    binding.TvNombres.text = nombres
                    binding.TvMiembro.text = constantes.obtenerFecha(tiempoReg)

                    Glide.with(this@DetalleAnuncio)
                        .load(imagenPerfil)
                        .placeholder(R.drawable.img_perfil)
                        .into(binding.ImgPerfil)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarImgAnuncio(){
        imagenSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSliderArrayList.clear()
                    for (ds in snapshot.children){
                        try {
                            val modeloImgSlider = ds.getValue(ModeloImgSlider::class.java)
                            imagenSliderArrayList.add(modeloImgSlider!!)
                        }catch (e:Exception){

                        }
                    }

                    val adaptadorImgSlider = AdaptadorImgSlider(this@DetalleAnuncio,imagenSliderArrayList)
                    binding.imagenSliderVP.adapter = adaptadorImgSlider

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun comprobarAnuncioFav(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}").child("Favoritos").child(idAnuncio)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorito = snapshot.exists()

                    if (favorito){
                        //Favorito = true
                        binding.IbFav.setImageResource(R.drawable.ic_anuncio_es_favorito)
                    }else{
                        //Favorito = false
                        binding.IbFav.setImageResource(R.drawable.ic_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun eliminarAnuncio(){
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .removeValue()
            .addOnSuccessListener {
                startActivity(Intent(this@DetalleAnuncio, MainActivity::class.java))
                finishAffinity()
                Toast.makeText(
                    this,
                    "Se eliminó el anuncio con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private val permisoLlamada =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ conceder->
            if (conceder){
                //True
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    constantes.llamarIntent(this, numTel)
                }
            }else{
                Toast.makeText(this@DetalleAnuncio,
                    "El permiso de realizar llamadas telefónicas no está concedida, por favor habilítela en los ajustes del dispositivo",
                    Toast.LENGTH_SHORT).show()
            }
        }

    private val permisoSms =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ conceder->
            if (conceder){
                //true
                val numTel = telVendedor
                if (numTel.isEmpty()){
                    Toast.makeText(this@DetalleAnuncio,
                        "El vendedor no tiene un número telefónico",
                        Toast.LENGTH_SHORT).show()
                }else{
                    constantes.smsIntent(this, numTel)
                }
            }else{
                //false
                Toast.makeText(this@DetalleAnuncio,
                    "El permiso de envío de mensajes SMS no está concedido, por favor habilítelo en los ajustes del teléfono",
                    Toast.LENGTH_SHORT).show()
            }

        }

}
