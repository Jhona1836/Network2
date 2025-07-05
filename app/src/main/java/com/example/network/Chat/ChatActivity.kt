package com.example.netwokr.Chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.network.R
import com.example.network.constantes
import com.example.network.databinding.ActivityChatBinding
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.redsystemstudio.appcomprayventa.Adaptadores.AdaptadorChat
import com.redsystemstudio.appcomprayventa.Modelo.ModeloChat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.security.GeneralSecurityException

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    private var uidVendedor = ""

            private var miUid = ""
    private var miNombre = ""
    private var recibimosToken = ""

    private var chatRuta = ""
    private var imagenUri : Uri ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        uidVendedor = intent.getStringExtra("uidVendedor")!!
        miUid = firebaseAuth.uid!!

        chatRuta = constantes.rutaChat(uidVendedor, miUid)

        cargarMiInformacion()
        cargarInfoVendedor()
        cargarMensajes()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.adjuntarFAB.setOnClickListener {
            seleccionarImgDialog()
        }

        binding.enviarFAB.setOnClickListener {
            validarInfo()
        }
    }

    private fun cargarMiInformacion (){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    miNombre = "${snapshot.child("nombres").value}"

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun cargarMensajes() {
        val mensajeArrayList = ArrayList<ModeloChat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRuta)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajeArrayList.clear()
                    for (ds : DataSnapshot in snapshot.children){
                        try {
                            val modeloChat = ds.getValue(ModeloChat::class.java)
                            mensajeArrayList.add(modeloChat!!)
                        }catch (e:Exception){

                        }
                    }
                    val adaptadorChat = AdaptadorChat(this@ChatActivity, mensajeArrayList)
                    binding.chatsRv.adapter = adaptadorChat

                    binding.chatsRv.setHasFixedSize(true)
                    var linearLayoutManeger = LinearLayoutManager(this@ChatActivity)
                    linearLayoutManeger.stackFromEnd = true
                    binding.chatsRv.layoutManager = linearLayoutManeger
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun validarInfo() {
        val mensaje = binding.EtMensajeChat.text.toString().trim()
        val tiempo = constantes.obtenerTiempoDis()

        if (mensaje.isEmpty()){
            Toast.makeText(this,"Ingrese un mensaje", Toast.LENGTH_SHORT).show()
        }else{
            enviarMensaje(constantes.MENSAJE_TIPO_TEXTO, mensaje, tiempo)
        }
    }

    private fun cargarInfoVendedor(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidVendedor)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val nombres = "${snapshot.child("nombres").value}"
                        val imagen = "${snapshot.child("urlImagenPerfil").value}"
                        val estado = "${snapshot.child("estado").value}"
                        recibimosToken = "${snapshot.child("fcmToken").value}"

                        binding.TxtNombreVendedorChat.text = nombres
                        binding.TxtEstadoChat.text = estado

                        try {
                            Glide.with(applicationContext)
                                .load(imagen)
                                .placeholder(R.drawable.img_perfil)
                                .into(binding.toolbarIv)
                        }catch (e: Exception){

                        }


                    }catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun seleccionarImgDialog(){
        val popupMenu = PopupMenu(this, binding.adjuntarFAB)

        popupMenu.menu.add(Menu.NONE,1,1, "Cámara")
        popupMenu.menu.add(Menu.NONE,2,2, "Galería")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem->
            val itemId = menuItem.itemId
            if (itemId == 1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    concederPermisoCamara.launch(arrayOf(android.Manifest.permission.CAMERA))
                }else{
                    concederPermisoCamara.launch(arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            }else if (itemId == 2){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    imagenGaleria()
                }else{
                    concederPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            true
        }

    }

    private fun imagenGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data
                subirImgStorage()
            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val concederPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ esConcedido->
            if (esConcedido){
                imagenGaleria()
            }else{
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento ha sido denegada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun abrirCamara(){
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Descripcion_imagen")

        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        resultadoCamara_ARL.launch(intent)
    }

    private val resultadoCamara_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                //Subir imagen
                subirImgStorage()
            }else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val concederPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ resultado->
            var concedidoTodos = true
            for (seConcede in resultado.values){
                concedidoTodos = concedidoTodos && seConcede
            }
            if (concedidoTodos){
                abrirCamara()
            }else{
                Toast.makeText(
                    this,
                    "El permiso de la cámara o almacenamiento ha sido denegado, o ambos fueron denegados",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun subirImgStorage(){
        progressDialog.setMessage("Subiendo imagen")
        progressDialog.show()

        val tiempo = constantes.obtenerTiempoDis()
        val nombreRutaImg = "ImagenesChat/$tiempo"

        val storageRef = FirebaseStorage.getInstance().getReference(nombreRutaImg)
        storageRef.putFile(imagenUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                val urlImagen = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    enviarMensaje(constantes.MENSAJE_TIPO_IMAGEN,urlImagen, tiempo)
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "No se pudo subir la imagen debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



    private fun obtenerAccesToken(): String? {
        return try {
            // Abrir el archivo de credenciales
            val servicioCuenta = applicationContext.assets.open("services-account.json")
            Log.d("FCM", "Archivo de credenciales abierto correctamente")

            // Obtener las credenciales de Google
            val googleCredentials = GoogleCredentials.fromStream(servicioCuenta)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            Log.d("FCM", "Credenciales de Google obtenidas correctamente")

            // Refrescar el token si es necesario
            googleCredentials.refreshIfExpired()
            Log.d("FCM", "Token refrescado si era necesario")

            // Obtener el valor del token de acceso
            val accessToken = googleCredentials.accessToken.tokenValue
            Log.d("FCM", "Token de acceso obtenido: $accessToken")

            accessToken
        } catch (e: IOException) {
            Log.e("FCM", "Error al abrir el archivo de credenciales: ${e.message}")
            null
        } catch (e: GeneralSecurityException) {
            Log.e("FCM", "Error de seguridad al obtener las credenciales: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("FCM", "Error desconocido al obtener el token de acceso: ${e.message}")
            null
        }
    }


    private fun enviarMensaje(tipoMensaje : String, mensaje : String, tiempo : Long){
        progressDialog.setMessage("Enviando mensaje")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String, Any>()

        hashMap["idMensaje"] = "$keyId"
        hashMap["tipoMensaje"] = "$tipoMensaje"
        hashMap["mensaje"] = "$mensaje"
        hashMap["emisorUid"] = "$miUid"
        hashMap["receptorUid"] = "$uidVendedor"
        hashMap["tiempo"] = tiempo

        refChat.child(chatRuta)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.EtMensajeChat.setText("")

                if (tipoMensaje == constantes.MENSAJE_TIPO_TEXTO){
                    prepararNotificacion(mensaje)
                }else{
                    prepararNotificacion("Se envió una imagen")
                }
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se pudo enviar el mensaje debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun prepararNotificacion(mensaje: String){
        val notificationJo = JSONObject()

        val messageJo = JSONObject()

        val notificationPayload = JSONObject()

        val messageData = JSONObject()

        try {

            notificationPayload.put("title", "Nuevo mensaje")
            notificationPayload.put("body", mensaje)


                    messageData.put("notificationType", "nuevo_mensaje")
            messageData.put("senderUid", firebaseAuth.uid)

            messageJo.put("token", recibimosToken)
            messageJo.put("notification", notificationPayload)
            messageJo.put("data", messageData)

            notificationJo.put("message", messageJo)

        }catch (e:Exception){
            e.printStackTrace()
        }
        enviarNotificacion(notificationJo)

    }

    private fun enviarNotificacion(notificationJo: JSONObject) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://fcm.googleapis.com/v1/projects/newokr-76603/messages:send"
            val accessToken = obtenerAccesToken()  // Obtener el token de acceso

            if (accessToken != null) {
                withContext(Dispatchers.Main) {
                    val jsonObjectRequest = object : JsonObjectRequest(
                        Method.POST,
                        url,
                        notificationJo,
                        { response -> Log.d("FCM", "Notificación enviada con éxito: $response") },
                        { error -> Log.e("FCM", "Error al enviar la notificación: ${error.message}") }
                    ) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-Type"] = "application/json"
                            headers["Authorization"] = "Bearer $accessToken"
                            return headers
                        }
                    }
                    Volley.newRequestQueue(this@ChatActivity).add(jsonObjectRequest)
                }
            } else {
                Log.e("Error", "No se pudo obtener el token de acceso")
            }
        }
    }


    private fun actualizarEstado(estado : String){
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios").child(firebaseAuth.uid!!)
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado
        ref!!.updateChildren(hashMap)

    }

    override fun onResume() {
        super.onResume()
        actualizarEstado("online")
    }

    override fun onPause() {
        super.onPause()
        actualizarEstado("offline")
    }



}


