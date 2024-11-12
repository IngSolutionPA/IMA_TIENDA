package com.example.ima_tienda

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ComprasActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var comprasAdapter: ComprasAdapter
    private lateinit var productosDisponiblesAdapter: ProductosDisponiblesAdapter
    private lateinit var clienteTextView: TextView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var errorLottieView: LottieAnimationView
    private var cedula: String? = null // Almacenar la cédula del cliente
    private lateinit var logoutIcon: ImageView
    private lateinit var btnComprarProductos: CardView
    private lateinit var btnHistorialCompras: CardView
    private lateinit var btnHistorialPedidos: CardView
    private lateinit var textViewTotal: TextView
    private lateinit var pedidosAdapter: PedidosAdapter
    private var feriaSeleccionadaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras)
        window.statusBarColor = Color.parseColor("#1b914b") // cambiamos el status bar

        // Inicializar vistas
        apiService = ApiClient.getApiService()
        logoutIcon = findViewById(R.id.logout_icon)
        clienteTextView = findViewById(R.id.text_cliente)
        btnComprarProductos = findViewById(R.id.btn_comprar_productos)
        btnHistorialCompras = findViewById(R.id.btn_historial_compras)
        btnHistorialPedidos = findViewById(R.id.btn_historial_pedidos)
        textViewTotal = findViewById(R.id.textViewTotal)
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        feriaSeleccionadaId = sharedPreferences.getInt("feria_seleccionada", -1)


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
        errorLottieView = findViewById(R.id.errorLottieView)

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

        val btnProcesarPedido = findViewById<Button>(R.id.btn_procesar_pedido)
        btnProcesarPedido.setOnClickListener {
            procesarPedido()  // Método para procesar el pedido
        }

        btnHistorialPedidos.setOnClickListener {
            loadPedidos() // Mostrar historial de pedidos
        }


    }

    private fun loadPedidos() {
        loadingAnimation.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(this)

        pedidosAdapter = PedidosAdapter(emptyList(), onCancelarClick = { pedido ->
            cancelarPedido(pedido)
        }, onPagarClick = { pedido ->
            procesarPago(pedido) // Procesar el pago al hacer clic en pagar
        })
        recyclerView.adapter = pedidosAdapter

        apiService.obtenerHistorialPedidos(cedula!!).enqueue(object : Callback<List<Pedidos_pendientes>> {
            override fun onResponse(call: Call<List<Pedidos_pendientes>>, response: Response<List<Pedidos_pendientes>>) {
                if (response.isSuccessful) {
                    val pedidos = response.body()
                    if (pedidos != null) {
                        val pedidosAgrupados = groupPedidos(pedidos)
                        pedidosAdapter.updateData(pedidosAgrupados)
                    } else {
                        Toast.makeText(this@ComprasActivity, "No se encontraron pedidos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ComprasActivity, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                }
                loadingAnimation.visibility = View.GONE
                errorLottieView.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<Pedidos_pendientes>>, t: Throwable) {
                //Toast.makeText(this@ComprasActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                errorLottieView.visibility = View.VISIBLE
                loadingAnimation.visibility = View.GONE
            }
        })
    }




    private fun procesarPedido() {
        // Verificar que haya productos en el carrito
        val productosSeleccionados = productosDisponiblesAdapter.obtenerProductosSeleccionados()
        if (productosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona productos para comprar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar la animación de carga mientras se procesa el pedido
        loadingAnimation.visibility = View.VISIBLE

        // Aquí podrías agregar la lógica para procesar el pedido
        val total = calcularTotalDePedido(productosSeleccionados)  // Método que calcula el total del pedido

        // Crear el objeto de pedido
        val pedido = Pedido(
            cedulaCliente = cedula!!,
            feriaSeleccionadaId= feriaSeleccionadaId,
            totalPedido = total,
            fechaPedido = obtenerFechaActual(),
            productos = productosSeleccionados
        )

        // Llamar a la API para registrar el pedido
        apiService.registrarPedido(pedido).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                if (response.isSuccessful) {
                    // Pedido procesado correctamente
                    Toast.makeText(this@ComprasActivity, "Pedido procesado correctamente", Toast.LENGTH_SHORT).show()
                    // Limpiar carrito y actualizar la UI
                    resetCarrito()
                    loadPedidos()
                } else {
                    // Error al procesar el pedido
                    Toast.makeText(this@ComprasActivity, "Error al procesar el pedido", Toast.LENGTH_SHORT).show()
                }
                loadingAnimation.visibility = View.GONE
                errorLottieView.visibility = View.GONE
            }

            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                Toast.makeText(this@ComprasActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                loadingAnimation.visibility = View.GONE
                errorLottieView.visibility = View.GONE
            }
        })
    }

    private fun procesarPago(pedido: PedidoAgrupado) {
        loadingAnimation.visibility = View.VISIBLE

        // Aquí deberías agregar la lógica para procesar el pago
        // Ejemplo: llamar a un servicio de pago o redirigir a una pantalla de pago
        apiService.procesarPago(pedido.pedido_id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ComprasActivity, "Pago procesado correctamente", Toast.LENGTH_SHORT).show()
                    loadPedidos() // Recargar los pedidos
                } else {
                    Toast.makeText(this@ComprasActivity, "Error al procesar el pago", Toast.LENGTH_SHORT).show()
                }
                loadingAnimation.visibility = View.GONE
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ComprasActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                loadingAnimation.visibility = View.GONE
            }
        })
    }

    private fun cancelarPedido(pedido: PedidoAgrupado) {
        // Mostrar animación de carga
        loadingAnimation.visibility = View.VISIBLE

        apiService.cancelarPedido(pedido.pedido_id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ComprasActivity, "Pedido cancelado correctamente", Toast.LENGTH_SHORT).show()
                    loadPedidos() // Recargar la lista de pedidos
                } else {
                    Toast.makeText(this@ComprasActivity, "Error al cancelar el pedido", Toast.LENGTH_SHORT).show()
                }
                loadingAnimation.visibility = View.GONE
                errorLottieView.visibility = View.GONE
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ComprasActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                loadingAnimation.visibility = View.GONE
                errorLottieView.visibility = View.GONE
            }
        })
    }

    fun obtenerFechaActual(): String {
        val fecha = Calendar.getInstance().time // Obtiene la fecha y hora actual
        val formato = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) // Define el formato de fecha y hora en 12 horas con AM/PM
        return formato.format(fecha) // Devuelve la fecha y hora formateada como String
    }



    private fun calcularTotalDePedido(productosSeleccionados: List<ProductoSeleccionado>): Double {
        var total = 0.0
        for (productoSeleccionado in productosSeleccionados) {
            val producto = productoSeleccionado.producto
            val cantidad = productoSeleccionado.cantidad
            // Multiplicar precio por cantidad antes de sumar al total
            total += (producto.precio.toDoubleOrNull() ?: 0.0) * cantidad
        }
        return total
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
                        errorLottieView.visibility = View.GONE
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
                    errorLottieView.visibility = View.GONE
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
                        errorLottieView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@ComprasActivity,
                            "Error al cargar productos",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingAnimation.visibility = View.GONE
                        errorLottieView.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<ProductoDisponible>>, t: Throwable) {
                    Toast.makeText(
                        this@ComprasActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingAnimation.visibility = View.GONE
                    errorLottieView.visibility = View.GONE
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
