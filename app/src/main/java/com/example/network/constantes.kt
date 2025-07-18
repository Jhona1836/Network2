package com.example.network

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Arrays
import java.util.HashMap
import java.util.Locale

object constantes {


    const val MENSAJE_TIPO_TEXTO = "TEXTO"
    const val MENSAJE_TIPO_IMAGEN = "IMAGEN"

    const val NOTIFICACION_DE_NUEVO_MENSAJE = "NOTIFICACION_DE_NUEVO_MENSAJE"
    const val FCM_SERVER_KEY =""

    val categorias = arrayOf(
        "Todos",
        "Servicio",
        "Trabajos"
    )


    val categoriasIcono = arrayOf(
        R.drawable.ic_todos_cat,
        R.drawable.ic_servicios_cat,
        R.drawable.ic_trabajos_cat

    )

    //Obtiene el tiempo actual en el cual se realiza la accion
    fun ObtenerTiempoDispositivo(): Long {
        return System.currentTimeMillis()
    }

    val tiempo = constantes.obtenerTiempoDis()

    fun obtenerTiempoDis() : Long{
        return System.currentTimeMillis()
    }


    fun obtenerFecha(tiempo: Long): String {

        val calendario = java.util.Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo

        return DateFormat.format("dd/MM/yyyy", calendario).toString()
    }

    fun agregarAnuncioFav (context : Context, idAnuncio : String){
        val firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = constantes.obtenerTiempoDis()

        val hashMap = HashMap<String, Any>()
        hashMap["idAnuncio"] = idAnuncio
        hashMap["tiempo"] = tiempo

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idAnuncio)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(context,
                    "Anuncio agregado a Favoritos",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context,
                    "${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    fun eliminarAnuncioFav (context: Context, idAnuncio: String){
        val firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idAnuncio)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context,
                    "Anuncio eliminado de Favoritos",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context,
                    "${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    fun mapaIntent (context: Context, latitud : Double, longitud : Double){
        val googleMapIntentUri = Uri.parse("http://maps.google.com/maps?daddr=$latitud,$longitud")

        val mapIntent = Intent(Intent.ACTION_VIEW, googleMapIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager)!=null){
            //La app de google maps si está instalada
            context.startActivity(mapIntent)
        }else{
            //Si la app de google maps no está instalada
            Toast.makeText(context, "No tienes instalada la aplicación de Google Maps",
                Toast.LENGTH_SHORT).show()
        }

    }

    fun llamarIntent (context: Context, tef : String){
        val intent = Intent(Intent.ACTION_CALL)
        intent.setData(Uri.parse("tel:$tef"))
        context.startActivity(intent)
    }

    fun smsIntent (context: Context, tel : String){
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("smsto:$tel"))
        intent.putExtra("sms_body", "")
        context.startActivity(intent)
    }

    fun rutaChat (receptorUid : String, emisorUid : String) : String{
        val arrayUid = arrayOf(receptorUid, emisorUid)
        Arrays.sort(arrayUid)
        //Nuestro uid [emisor]= 3dVRWIBr1mV7aNwEnzNQBIOEvu93
        //Uid del vendedor [receptor] = EutkJ9tjRUQoxuCwPM83ceeCFsy2
        //La ruta seria = 3dVRWIBr1mV7aNwEnzNQBIOEvu93_EutkJ9tjRUQoxuCwPM83ceeCFsy2
        return "${arrayUid[0]}_${arrayUid[1]}"

    }

    fun incrementarVistas(idAnuncio : String){
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var vistasActuales = "${snapshot.child("contadorVistas").value}"
                    if (vistasActuales == "" || vistasActuales == "null"){
                        vistasActuales = "0"
                    }

                    val nuevaVista = vistasActuales.toLong()+ 1

                    val hashMap = HashMap<String, Any>()
                    hashMap["contadorVistas"] = nuevaVista

                    val dbRef = FirebaseDatabase.getInstance().getReference("Anuncios")
                    dbRef.child(idAnuncio)
                        .updateChildren(hashMap)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun obtenerFechaHora(tiempo: Long) : String{
        val calendario = java.util.Calendar.getInstance(Locale.ENGLISH)
        calendario.timeInMillis = tiempo
        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendario).toString()
    }


}