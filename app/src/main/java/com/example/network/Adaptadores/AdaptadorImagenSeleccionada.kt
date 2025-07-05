package com.example.network.Adaptadores

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.network.Modelo.ModeloImageSeleccionada
import com.example.network.R
import com.example.network.databinding.ItemImagenesSeleccionadasBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class AdaptadorImagenSeleccionada(
    private val context : Context,
    private val imagenesSelecArrayList : ArrayList<ModeloImageSeleccionada>,
    private val idAnuncio : String
): Adapter<AdaptadorImagenSeleccionada.HolderImagenSeleccionada>(){

    private lateinit var binding : ItemImagenesSeleccionadasBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {
        binding = ItemImagenesSeleccionadasBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagenSeleccionada(binding.root)
    }

    override fun getItemCount(): Int {
        return imagenesSelecArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {
        val modelo = imagenesSelecArrayList[position]

        if (modelo.deInternet){
            /*Haremos lectura de las imágenes traidas desde Firebase*/
            try {
                val imagenUrl = modelo.imagenUrl
                Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.item_imagen)
                    .into(binding.itemImage)
            }catch (e: Exception){

            }
        }else{
            /*Haremos lectura de las imagenes seleccionadas desde la galería o tomadas desde la cámara*/
            try {
                val imagenUri = modelo.imagenUri
                Glide.with(context)
                    .load(imagenUri)
                    .placeholder(R.drawable.item_imagen)
                    .into(holder.item_imagen)
            }catch (e:Exception){

            }
            holder.btn_cerrar.setOnClickListener {
                if (modelo.deInternet){
                   eliminarImgFirebase(modelo, holder, position)
                }else{
                    imagenesSelecArrayList.remove(modelo)
                    notifyDataSetChanged()
                }

            }
        }




    }

    private fun eliminarImgFirebase(modelo: ModeloImageSeleccionada, holder: AdaptadorImagenSeleccionada.HolderImagenSeleccionada, position: Int) {
        val idImagen = modelo.id

        /*La imagen se eliminará en la base de datos - Realtime database*/
        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").child(idImagen)
            .removeValue()
            .addOnSuccessListener {
                try {
                    imagenesSelecArrayList.remove(modelo)
                    eliminarImgStorage(modelo) /*La imagen también se eliminará del storage*/
                    notifyItemRemoved(position)
                }catch (e:Exception){

                }
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarImgStorage(modelo: ModeloImageSeleccionada) {
        val rutaImagen = "Anuncios/"+modelo.id

        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "La imagen se ha eliminado",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}",Toast.LENGTH_SHORT).show()
            }

    }

    inner class HolderImagenSeleccionada(itemView : View) : ViewHolder(itemView){
        var item_imagen = binding.itemImage
        var btn_cerrar = binding.cerrarItem
    }


}