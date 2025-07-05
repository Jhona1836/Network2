package com.example.network.Filtro

import android.widget.Filter
import com.example.network.Adaptadores.AdaptadorAnuncio
import com.example.network.Modelo.ModeloAnuncio
import java.util.Locale

class FiltrarAnuncio (
    private val adaptador : AdaptadorAnuncio,
    private val filtroLista : ArrayList<ModeloAnuncio>
): Filter()
{
    override fun performFiltering(filtro: CharSequence?): FilterResults {
        var filtro = filtro
        val resultados = FilterResults()

        if(!filtro.isNullOrEmpty()){
            filtro = filtro.toString().uppercase(Locale.getDefault())
            val filtroModelo =  arrayListOf<ModeloAnuncio>()
            for (i in filtroLista.indices){
                if (filtroLista[i].descripcion.uppercase(Locale.getDefault()).contains(filtro) ||
                    filtroLista[i].categoria.uppercase(Locale.getDefault()).contains(filtro)||
                    filtroLista[i].titulo.uppercase(Locale.getDefault()).contains(filtro)){
                    filtroModelo.add(filtroLista[i])
                }
            }
            resultados.count = filtroModelo.size
            resultados.values = filtroModelo
        }else{
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
        }


    override fun publishResults(filtro: CharSequence?, resultados: FilterResults?) {
        if (resultados != null) {
            adaptador.anuncioArrayList = resultados.values as ArrayList<ModeloAnuncio>
        }
        adaptador.notifyDataSetChanged()
    }

}