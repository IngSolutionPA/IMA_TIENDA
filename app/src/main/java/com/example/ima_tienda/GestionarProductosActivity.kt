package com.example.ima_tienda

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class GestionarProductosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var apiService: ApiService // Asegúrate de inicializar esto correctamente
    private val productos = mutableListOf<Producto>()
    private lateinit var imagenPreview: ImageView // Para mostrar la imagen seleccionada
    private var imagenUri: Uri? = null // Variable para almacenar la URI de la imagen seleccionada


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_productos) // Asegúrate de que este layout existe

        // Inicializa tu apiService aquí
        apiService = ApiClient.getApiService() // Reemplaza con tu método para obtener la instancia de ApiService

        recyclerView = findViewById(R.id.productosRecyclerView) // Asegúrate de tener un RecyclerView en tu layout
        recyclerView.layoutManager = LinearLayoutManager(this)

        obtenerProductos() // Llama a la función para obtener productos

        findViewById<MaterialButton>(R.id.agregarProductoButton).setOnClickListener {
            mostrarDialogoAgregarProducto() // Muestra el dialog para agregar producto
        }

    }

    private fun obtenerProductos() {
        apiService.obtenerProductos().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    response.body()?.let { listaProductos ->
                        productos.clear() // Limpiar la lista actual
                        productos.addAll(listaProductos) // Agregar nuevos productos
                        configurarRecyclerView() // Configura el RecyclerView
                    }
                } else {
                    Log.e("API", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Log.e("API", "Fallo: ${t.message}")
            }
        })
    }


    private fun mostrarDialogoAgregarProducto() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_producto, null)
        dialog.setView(dialogView)

        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditText)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.descripcionEditText)
        val precioEditText = dialogView.findViewById<EditText>(R.id.precioEditText)
        val seleccionarImagenButton = dialogView.findViewById<Button>(R.id.seleccionarImagenButton)

        imagenPreview = dialogView.findViewById<ImageView>(R.id.imagenPreview) // Asigna aquí

        seleccionarImagenButton.setOnClickListener {
            // Código para abrir el selector de imágenes
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        dialog.setTitle("Agregar Producto")
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = nombreEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val precio = precioEditText.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && precio != null && imagenUri != null) {
                agregarProducto(nombre, descripcion, precio, imagenUri!!)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("Cancelar") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                if (::imagenPreview.isInitialized) { // Verifica si imagenPreview está inicializado
                    imagenPreview.setImageURI(uri) // Muestra la imagen seleccionada
                    imagenPreview.visibility = View.VISIBLE // Muestra el ImageView
                    imagenUri = uri // Almacena la URI de la imagen seleccionada
                } else {
                    // Manejo si imagenPreview no está inicializado
                    Log.e("GestionarProductosActivity", "imagenPreview no está inicializado")
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    private fun getFileFromUri(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                val filePath = if (columnIndex != -1) it.getString(columnIndex) else null

                // Asegúrate de que el filePath no sea nulo antes de crear el objeto File
                return filePath?.let { path -> File(path) }
            }
        }
        return null // Retorna null si no se pudo obtener el archivo
    }


    private fun agregarProducto(nombre: String, descripcion: String, precio: Double, imagenUri: Uri) {
        val imagenFile = getFileFromUri(imagenUri)
        if (imagenFile != null) {
            val nombrePart = nombre.toRequestBody("text/plain".toMediaType())
            val descripcionPart = descripcion.toRequestBody("text/plain".toMediaType())
            val precioPart = precio.toString().toRequestBody("text/plain".toMediaType())
            val imagenPart = imagenFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagenMultipart = MultipartBody.Part.createFormData("imagen", imagenFile.name, imagenPart)

            apiService.agregarProductos(nombrePart, descripcionPart, precioPart, imagenMultipart)
                .enqueue(object : Callback<Producto> {
                    override fun onResponse(call: Call<Producto>, response: Response<Producto>) {
                        if (response.isSuccessful) {
                            obtenerProductos() // Opcional, para refrescar la lista de productos
                            Toast.makeText(this@GestionarProductosActivity, "Producto agregado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("API", "Error al agregar producto: ${response.code()}")
                            Log.e("API", "Respuesta: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<Producto>, t: Throwable) {
                        Log.e("API", "Fallo al agregar producto: ${t.message}")
                    }
                })
        } else {
            Log.e("Error", "No se pudo obtener el archivo de la URI")
        }
    }


    private fun configurarRecyclerView() {
        productoAdapter = ProductoAdapter(productos, { producto ->
            producto.id?.let { eliminarProducto(it) } // Llama a la función para eliminar el producto
        }, { producto ->
            mostrarDialogoEditarProducto(producto) // Llama a la función para editar el producto
        })
        recyclerView.adapter = productoAdapter
    }
    private fun mostrarDialogoEditarProducto(producto: Producto) {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_producto, null)
        dialog.setView(dialogView)

        val nombreEditText = dialogView.findViewById<EditText>(R.id.nombreEditText)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.descripcionEditText)
        val precioEditText = dialogView.findViewById<EditText>(R.id.precioEditText)
        val imagenImageView = dialogView.findViewById<ImageView>(R.id.imagenPreview)
        val seleccionarImagenButton = dialogView.findViewById<Button>(R.id.seleccionarImagenButton)

        // Prellenar los campos con los datos actuales del producto
        nombreEditText.setText(producto.nombre)
        descripcionEditText.setText(producto.descripcion)
        precioEditText.setText(producto.precio.toString())

        // Cargar la imagen usando Glide
        Glide.with(dialogView.context) // Usar el contexto del diálogo
            .load(producto.imagen) // Usar la ruta de la imagen almacenada
            .into(imagenImageView) // Cargar en el ImageView del diálogo

        imagenImageView.visibility = View.VISIBLE // Muestra el ImageView
        imagenPreview = dialogView.findViewById<ImageView>(R.id.imagenPreview) // Asigna aquí
        seleccionarImagenButton.setOnClickListener {
            // Código para abrir el selector de imágenes
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }



        // Configura el botón para seleccionar una nueva imagen
        seleccionarImagenButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        dialog.setTitle("Editar Producto")
        dialog.setPositiveButton("Guardar") { _, _ ->
            val nombre = nombreEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val precio = precioEditText.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && descripcion.isNotEmpty() && precio != null) {
                // Llama a editarProducto con la imagenUri actual
                editarProducto(producto.id!!, nombre, descripcion, precio, imagenUri) // Pasa la imagenUri actual
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
        dialog.show()
    }



    private fun editarProducto(id: Int, nombre: String, descripcion: String, precio: Double, imagenUri: Uri?) {
        val nombreRequestBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
        val descripcionRequestBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val precioRequestBody = precio.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagenPart: MultipartBody.Part? = imagenUri?.let { uri ->
            val file = getFileFromUri(uri) // Obtén el archivo desde el Uri
            file?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imagen", it.name, requestFile)
            }
        }

        apiService.editarProducto(id, nombreRequestBody, descripcionRequestBody, precioRequestBody, imagenPart).enqueue(object : Callback<Producto> {
            override fun onResponse(call: Call<Producto>, response: Response<Producto>) {
                if (response.isSuccessful) {
                    obtenerProductos() // Opcional, para refrescar la lista de productos
                    Toast.makeText(this@GestionarProductosActivity, "Producto Actualizado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API", "Error al editar producto: ${response.code()}")
                    Log.e("API", "Respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Producto>, t: Throwable) {
                Log.e("API", "Fallo al editar producto: ${t.message}")
            }
        })
    }




    private fun eliminarProducto(id: Int) {
        // Crear un diálogo de confirmación
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar eliminación")
        builder.setMessage("¿Estás seguro de que quieres eliminar este Producto?")

        // Configurar el botón de confirmación
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            // Llamar al servicio API solo si se confirma
            apiService.eliminarProducto(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        obtenerProductos()
                        Toast.makeText(this@GestionarProductosActivity, "Producto eliminado con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("API", "Error al eliminar Producto: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("API", "Fallo al eliminar el Producto: ${t.message}")
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
