package com.example.network

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.network.databinding.ActivityEditarPerfilBinding
import com.example.network.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class editar_perfil : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarInfo()

        binding.btnActualizar.setOnClickListener(){
            validarInfo()
        }

        binding.FABCambiarImg.setOnClickListener {
            selec_imagen_de()
        }
    }

    private var nombres = ""
    private var f_nac = ""
    private var codigo = ""
    private var telefono = ""

    private fun validarInfo() {
        nombres = binding.EtNombres.text.toString().trim()
        f_nac = binding.EtFNac.text.toString().trim()
        codigo = binding.selectorCodigo.selectedCountryCodeWithPlus
        telefono = binding.EtTel.text.toString().trim()

        if(nombres.isEmpty()){
            Toast.makeText(this, "Ingrese sus nombres", Toast.LENGTH_SHORT).show()
        }else if(f_nac.isEmpty()){
            Toast.makeText(this, "Ingrese su fecha de nacimiento", Toast.LENGTH_SHORT).show()
        }else if(codigo.isEmpty()){
            Toast.makeText(this, "Seleccione un codigo", Toast.LENGTH_SHORT).show()
        }else if (telefono.isEmpty()){
            Toast.makeText(this, "Ingrese su numero de telefono", Toast.LENGTH_SHORT).show()
        }else{
            actualizarInfo()
        }
    }

    private fun actualizarInfo() {
        progressDialog.setMessage("Actualizando Información")

        val hashMap : HashMap<String,Any> = HashMap()
        hashMap["nombres"] = "${nombres}"
        hashMap["fecha_nac"] = "${f_nac}"
        hashMap["codigoTelefono"] = "${codigo}"
        hashMap["telefono"] = "${telefono}"

        val ref =FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Sus Datos se Actualizaron",Toast.LENGTH_SHORT).show()


            }
            .addOnFailureListener{
                e-> progressDialog.dismiss()
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
            }
    }


    ////////////////////////// F U N C I O N E S //////////////////////////

    //Funcion del PopMenu
    private fun selec_imagen_de() {
        val popmenu = PopupMenu(this, binding.FABCambiarImg)

        popmenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popmenu.menu.add(Menu.NONE, 2, 2, "Galeria")

        popmenu.show()

        popmenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                //Camara
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    concederPermisoCamara.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imagenGaleria()
                } else {
                    concederPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    //Funcion de Cargar la informacion y mostrarla en pantalla
    private fun cargarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = snapshot.child("Nombre").value?.toString() ?: "N/A"
                    val imagen = snapshot.child("urlImagenPerfil").value?.toString()
                    val f_nac = snapshot.child("fecha_nac").value?.toString() ?: "N/A"
                    val telefono = snapshot.child("telefono").value?.toString() ?: ""
                    val codTelefono = snapshot.child("codigoTelefono").value?.toString() ?: ""

                    //Setear la informacion en la vistas

                    binding.EtNombres.setText(nombres)
                    binding.EtFNac.setText(f_nac)
                    binding.EtTel.setText(telefono)

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.imgperfilRl)

                    } catch (e: Exception) {
                        Toast.makeText(this@editar_perfil, "${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    try {
                        val codigo = codTelefono.replace("+", "").toInt()
                        binding.selectorCodigo.setCountryForPhoneCode(codigo)

                    } catch (e: Exception) {
                        Toast.makeText(this@editar_perfil, "${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    //Funcion de subir la imagen a la base de datos
    private fun subirImageStorage() {
        progressDialog.setMessage("Subiendo imagen a la base de datos")
        progressDialog.show()

        val rutaImagen = "imagenesPerfil/" + firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.putFile((imageUri!!))
            .addOnSuccessListener { taskSnapShot ->
                val uriTask = taskSnapShot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagenCargada = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    actualizarImagenBD(urlImagenCargada)
                }

            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //Funcion de actualizar la imagen en la base de datos
    private fun actualizarImagenBD(urlImagenCargada: String) {
        progressDialog.setMessage("Actualizando Imagen")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        if (imageUri != null) {
            hashMap["urlImagenPerfil"] = urlImagenCargada
        }

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    applicationContext,
                    "Imagen de perfil actualizada",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    //Acceder a la galeria para obtener una imagen
    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    //Funcion para lanzar la camara
    private fun imageCamara() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripción_image")
        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        resultadoCamar_ARL.launch(intent)
    }

    //Concede os permisos de almacenamiento del dispositivo
    private val concederPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) {
                imagenGaleria()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento ha sido denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    //Funcion que concede los permisos a la camara
    private val concederPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
            var concedidoTodos = true
            for (seConcede in resultado.values) {
                concedidoTodos = concedidoTodos && seConcede
            }

            if (concedidoTodos) {
                imageCamara()
            } else {
                Toast.makeText(
                    this,
                    "Acepte los permisos de almacenamiento y camara para utilizar esta función",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    //Obtener foto mediante la camara
    private val resultadoCamar_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                subirImageStorage()
                /*
                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.img_perfil)
                        .into(binding.imgperfilRl)


                }catch (e:Exception){

                }*/
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }

        }

//obtiene la imagen de galeria y la setea a un variable
    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if(resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imageUri = data!!.data
                subirImageStorage()
                /*
                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.img_perfil)
                        .into(binding.imgperfilRl)


                }catch (e:Exception){

                }*/
            }else{
                Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()

            }
        }

}