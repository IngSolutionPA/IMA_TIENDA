package com.example.ima_tienda

data class Feria(
    val id: Int = 0,                 // ID único de la feria, útil para identificar registros en una base de datos
    val nombre: String,               // Nombre de la feria
    val descripcion: String,          // Descripción de la feria
    val ubicacion: String             // Ubicación de la feria
)
