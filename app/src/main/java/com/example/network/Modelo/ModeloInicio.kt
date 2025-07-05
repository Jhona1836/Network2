package com.example.network.Modelo


class ModeloAnuncio {

    var id : String = ""
    var uid : String = ""
    var categoria : String = ""
    var direccion : String = ""
    var titulo : String = ""
    var descripcion : String = ""
    var tiempo : Long = 0
    var latitud = 0.0
    var longitud = 0.0
    var favorito = false
    var contadorVistas = 0

    constructor()
    constructor(
        id: String,
        uid: String,
        direccion: String,
        titulo: String,
        descripcion: String,
        tiempo: Long,
        latitud: Double,
        longitud: Double,
        favorito: Boolean,
        contadorVistas: Int
    ) {
        this.id = id
        this.uid = uid
        this.direccion = direccion
        this.titulo = titulo
        this.descripcion = descripcion
        this.tiempo = tiempo
        this.latitud = latitud
        this.longitud = longitud
        this.favorito = favorito
        this.contadorVistas = contadorVistas
    }


}