package com.example.ima_tienda

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GestionarFeriasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feriaAdapter: FeriaAdapter
    private lateinit var apiService: ApiService
    private val ferias = mutableListOf<Feria>()
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var logoutIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_ferias)

        apiService = ApiClient.getApiService()
        logoutIcon = findViewById(R.id.logout_icon)
        recyclerView = findViewById(R.id.feriaRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        obtenerFerias()


        findViewById<MaterialButton>(R.id.agregarFeriaButton).setOnClickListener {
            mostrarDialogoAgregarFeria()
        }
        // Configuración del botón de logout
        logoutIcon.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Eliminar datos de sesión de SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Regresar al LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Finaliza MainActivity
    }


    private fun obtenerFerias() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        apiService.obtenerFerias().enqueue(object : Callback<List<Feria>> {
            override fun onResponse(call: Call<List<Feria>>, response: Response<List<Feria>>) {
                if (response.isSuccessful) {
                    response.body()?.let { listaFerias ->
                        ferias.clear()
                        ferias.addAll(listaFerias)
                        configurarRecyclerView()
                    }
                    loadingAnimation.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    Log.e("API", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Feria>>, t: Throwable) {
                Log.e("API", "Fallo: ${t.message}")
                loadingAnimation.visibility = View.GONE
            }
        })
    }

    private fun mostrarDialogoAgregarFeria() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_feria, null)
        dialog.setView(dialogView)

        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditText)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.descripcionEditText)
        val ubicacionEditText = dialogView.findViewById<EditText>(R.id.ubicacionEditText)

        dialog.setTitle("Agregar Feria")
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = nombreEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val ubicacion = ubicacionEditText.text.toString()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && ubicacion.isNotEmpty()) {
                agregarFeria(nombre, descripcion, ubicacion)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
        dialog.show()
    }

    private fun agregarFeria(nombre: String, descripcion: String, ubicacion: String) {
        val feria = Feria(nombre = nombre, descripcion = descripcion, ubicacion = ubicacion)
        println(feria);
        apiService.agregarFeria(feria).enqueue(object : Callback<Feria> {
            override fun onResponse(call: Call<Feria>, response: Response<Feria>) {
                if (response.isSuccessful) {
                    obtenerFerias()
                    Toast.makeText(this@GestionarFeriasActivity, "Feria agregada con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API", "Error al agregar feria: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Feria>, t: Throwable) {
                Log.e("API", "Fallo al agregar feria: ${t.message}")
            }
        })
    }

    private fun configurarRecyclerView() {
        feriaAdapter = FeriaAdapter(ferias, { feria ->
            feria.id?.let { eliminarFeria(it) }
        }, { feria ->
            mostrarDialogoEditarFeria(feria)
        })
        recyclerView.adapter = feriaAdapter
    }

    private fun mostrarDialogoEditarFeria(feria: Feria) {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_feria, null)
        dialog.setView(dialogView)

        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditText)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.descripcionEditText)
        val ubicacionEditText = dialogView.findViewById<EditText>(R.id.ubicacionEditText)

        nombreEditText.setText(feria.nombre)
        descripcionEditText.setText(feria.descripcion)
        ubicacionEditText.setText(feria.ubicacion)

        dialog.setTitle("Editar Feria")
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = nombreEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val ubicacion = ubicacionEditText.text.toString()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && ubicacion.isNotEmpty()) {
                editarFeria(feria.id!!, nombre, descripcion, ubicacion)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
        dialog.show()
    }

    private fun editarFeria(id: Int, nombre: String, descripcion: String, ubicacion: String) {
        val feria = Feria(id = id, nombre = nombre, descripcion = descripcion, ubicacion = ubicacion)

        apiService.editarFeria(id, feria).enqueue(object : Callback<Feria> {
            override fun onResponse(call: Call<Feria>, response: Response<Feria>) {
                if (response.isSuccessful) {
                    obtenerFerias()
                    Toast.makeText(this@GestionarFeriasActivity, "Feria Actualizada con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API", "Error al editar feria: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Feria>, t: Throwable) {
                Log.e("API", "Fallo al editar feria: ${t.message}")
            }
        })
    }

    private fun eliminarFeria(id: Int) {
        // Crear un diálogo de confirmación
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar eliminación")
        builder.setMessage("¿Estás seguro de que quieres eliminar esta feria?")

        // Configurar el botón de confirmación
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            // Llamar al servicio API solo si se confirma
            apiService.eliminarFeria(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        obtenerFerias()
                        Toast.makeText(this@GestionarFeriasActivity, "Feria eliminada con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("API", "Error al eliminar feria: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("API", "Fallo al eliminar feria: ${t.message}")
                }
            })
            dialog.dismiss()
        }

        // Configurar el botón de cancelación
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Mostrar el diálogo de confirmación
        builder.show()
    }


}
