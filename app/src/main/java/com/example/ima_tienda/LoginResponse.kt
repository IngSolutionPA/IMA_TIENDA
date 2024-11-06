package com.example.ima_tienda

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val nivel: Int,
    val nombre: String,
    val ferias: List<Feria>, // Lista de ferias asignadas al usuario
    val feria_seleccionada: String,
)