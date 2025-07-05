package com.example.network.Adaptadores

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.network.DetalleAnuncio.DetalleAnuncio
import com.example.network.Filtro.FiltrarAnuncio
import com.example.network.Modelo.ModeloAnuncio
import com.example.network.R
import com.example.network.constantes
import com.example.network.databinding.ItemAnuncioNuevaVersionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AdaptadorAnuncio : RecyclerView.Adapter<AdaptadorAnuncio.HolderAnuncio>, Filterable{

    private lateinit var binding : ItemAnuncioNuevaVersionBinding

    private var context : Context
    var anuncioArrayList : ArrayList<ModeloAnuncio>
    private var firebaeAuth : FirebaseAuth
    private var filtroLista : ArrayList<ModeloAnuncio>
  private var filtro : FiltrarAnuncio ?= null

    constructor(context: Context, anuncioArrayList: ArrayList<ModeloAnuncio>) {
        this.context = context
        this.anuncioArrayList = anuncioArrayList
        firebaeAuth = FirebaseAuth.getInstance()
        this.filtroLista = anuncioArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAnuncio {
        binding = ItemAnuncioNuevaVersionBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAnuncio(binding.root)
    }

    override fun getItemCount(): Int {
        return anuncioArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAnuncio, position: Int) {
        val modeloAnuncio = anuncioArrayList[position]

        val titulo = modeloAnuncio.titulo
        val descripcion = modeloAnuncio.descripcion
        val direccion = modeloAnuncio.direccion
        val tiempo = modeloAnuncio.tiempo

        val formatoFecha = constantes.obtenerFecha(tiempo)

        cargarPrimeraImgAnuncio(modeloAnuncio,holder)

        comprobarFavorito(modeloAnuncio, holder)

        holder.Tv_titulo.text = titulo
        holder.Tv_descripcion.text = descripcion
        holder.Tv_direccion.text = direccion


      holder.itemView.setOnClickListener {
           val intent = Intent(context, DetalleAnuncio::class.java)
           intent.putExtra("idAnuncio", modeloAnuncio.id)
          context.startActivity(intent)
        }



//
        holder.Ib_fav.setOnClickListener {
            val favorito = modeloAnuncio.favorito

            if (favorito){
                //Favorito = true
                constantes.eliminarAnuncioFav(context, modeloAnuncio.id)
            }else{
                //Favorito = false
              constantes.agregarAnuncioFav(context, modeloAnuncio.id)
            }
        }
    }

    private fun comprobarFavorito(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaeAuth.uid!!).child("Favoritos").child(modeloAnuncio.id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorito = snapshot.exists()
                    modeloAnuncio.favorito = favorito

                    if (favorito){
                      holder.Ib_fav.setImageResource(R.drawable.ic_anuncio_es_favorito)
                    }else{
                        holder.Ib_fav.setImageResource(R.drawable.ic_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private fun cargarPrimeraImgAnuncio(modeloAnuncio: ModeloAnuncio, holder: AdaptadorAnuncio.HolderAnuncio) {
        val idAnuncio = modeloAnuncio.id

        val ref = FirebaseDatabase.getInstance().getReference("Anuncios")
        ref.child(idAnuncio).child("Imagenes").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val imagenUrl = "${ds.child("imagenUrl").value}"
                        try {
                            Glide.with(context)
                                .load(imagenUrl)
                                .placeholder(R.drawable.ic_imagen)
                                .into(holder.imagenIv)
                        }catch (e:Exception){

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    inner class HolderAnuncio(itemView : View) : RecyclerView.ViewHolder(itemView){
        var imagenIv = binding.imagenIv
        var Tv_titulo = binding.TvTitulo
        var Tv_descripcion = binding.TvDescripcion
        var Tv_direccion = binding.TvDireccion
        var Ib_fav = binding.IbFav
    }


   override fun getFilter(): Filter {
       if (filtro == null){
                  filtro = FiltrarAnuncio(this, filtroLista)
       }
               return filtro as FiltrarAnuncio
            }


       }
