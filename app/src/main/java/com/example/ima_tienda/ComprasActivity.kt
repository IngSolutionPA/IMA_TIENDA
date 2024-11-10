package com.example.ima_tienda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComprasActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var comprasAdapter: ComprasAdapter
    private lateinit var productosDisponiblesAdapter: ProductosDisponiblesAdapter
    private lateinit var clienteTextView: TextView
    private lateinit var loadingAnimation: LottieAnimationView
    private var cedula: String? = null // Almacenar la cédula del cliente
    private lateinit var logoutIcon: ImageView
    private lateinit var btnComprarProductos: Button
    private lateinit var btnHistorialCompras: Button
    private lateinit var textViewTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras)

        // Inicializar vistas
        apiService = ApiClient.getApiService()
        logoutIcon = findViewById(R.id.logout_icon)
        clienteTextView = findViewById(R.id.text_cliente)
        btnComprarProductos = findViewById(R.id.btn_comprar_productos)
        btnHistorialCompras = findViewById(R.id.btn_historial_compras)
        textViewTotal = findViewById(R.id.textViewTotal)

        // Configuración de RecyclerView para el historial de compras
        recyclerView = findViewById(R.id.recycler_compras)
        recyclerView.layoutManager = LinearLayoutManager(this)
        comprasAdapter = ComprasAdapter(emptyMap())
        recyclerView.adapter = comprasAdapter

        // Configuración de RecyclerView para productos disponibles
         productosDisponiblesAdapter = ProductosDisponiblesAdapter(emptyList()) { total ->
            // Actualizamos el TextView con el total actualizado
            textViewTotal.text = "Total: \$${"%.2f".format(total)}"
        }

        // Obtener cédula
        cedula = intent.getStringExtra("CEDULA") ?: ""
        clienteTextView.text = "Cliente: ${cedula}"
        loadingAnimation = findViewById(R.id.loadingAnimation)

        // Asignar listeners a los botones
        btnComprarProductos.setOnClickListener {
            showAvailableProducts() // Mostrar productos disponibles
        }

        btnHistorialCompras.setOnClickListener {
            loadCompras() // Mostrar historial de compras
        }

        // Configuración del botón de logout
        logoutIcon.setOnClickListener {
            logout()
        }
        showAvailableProducts()

        val btnEliminarCarrito = findViewById<Button>(R.id.btn_eliminar_carrito)
        btnEliminarCarrito.setOnClickListener {
            resetCarrito()  // Llamar al método para restablecer el carrito
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadCompras() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.layoutManager =
            LinearLayoutManager(this) // Asegúrate de que esté en LinearLayoutManager para el historial de compras
        recyclerView.adapter =
            comprasAdapter // Asegúrate de que está mostrando el adaptador correcto
        recyclerView.visibility = View.GONE
        if (cedula != null) {
            apiService.obtenerComprasPorCedula(cedula!!).enqueue(object : Callback<List<Compra>> {
                override fun onResponse(
                    call: Call<List<Compra>>,
                    response: Response<List<Compra>>
                ) {
                    if (response.isSuccessful) {
                        val compras = response.body()
                        if (compras != null) {
                            val comprasAgrupadas = compras.groupBy { it.id }
                            comprasAdapter.updateData(comprasAgrupadas)
                        } else {
                            Toast.makeText(
                                this@ComprasActivity,
                                "No se encontraron compras",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        loadingAnimation.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@ComprasActivity,
                            "Error al cargar compras",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Compra>>, t: Throwable) {
                    Toast.makeText(this@ComprasActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loadingAnimation.visibility = View.GONE
                }
            })
        }
    }

    private fun showAvailableProducts() {
        // Mostrar la animación de carga
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.layoutManager =
                //GridLayoutManager(this, 2) // Cambiar a GridLayoutManager para productos (2 columnas)
            LinearLayoutManager(this)
        recyclerView.adapter = productosDisponiblesAdapter // Cambiar al adaptador de productos
        recyclerView.visibility = View.GONE

        // Verificar que la cédula no sea nula
        if (cedula != null) {
            // Llamada a la API para obtener los productos disponibles según el query
            apiService.obtenerProductosDisponibles(cedula!!).enqueue(object : Callback<List<ProductoDisponible>> {
                override fun onResponse(
                    call: Call<List<ProductoDisponible>>,
                    response: Response<List<ProductoDisponible>>
                ) {
                    if (response.isSuccessful) {
                        val productos = response.body()
                        if (productos != null && productos.isNotEmpty()) {
                            // Actualizar el adaptador con la lista de productos disponibles
                            productosDisponiblesAdapter.updateData(productos)
                        } else {
                            Toast.makeText(
                                this@ComprasActivity,
                                "No hay productos disponibles",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        loadingAnimation.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@ComprasActivity,
                            "Error al cargar productos",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingAnimation.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<ProductoDisponible>>, t: Throwable) {
                    Toast.makeText(
                        this@ComprasActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingAnimation.visibility = View.GONE
                }
            })
        } else {
            Toast.makeText(this, "Error: Cédula no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetCarrito() {
        // Vaciar el carrito
        productosDisponiblesAdapter.clearSelectedProducts()

        // Restablecer el total a cero
        textViewTotal.text = "Total: \$0.00"

        // Actualizar la vista de RecyclerView
        productosDisponiblesAdapter.notifyDataSetChanged()
    }

}
