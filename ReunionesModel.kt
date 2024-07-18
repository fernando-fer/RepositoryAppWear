package com.example.proyectomes1.presentation

class ReunionesModel (
    var tipoD : String,
    var titulo: String,
    var fecha: String,
    var descripcion: String
)

data class LoginResponse
    (
    val done: Boolean,
    val message: String?,
)

data class LoginRequest(
    val matriculaAlum: String,
    val password: String

)
