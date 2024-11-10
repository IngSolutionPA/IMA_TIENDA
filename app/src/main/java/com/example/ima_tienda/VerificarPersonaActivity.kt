package com.example.ima_tienda

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VerificarPersonaActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var scanResult: TextView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var apiService: ApiService
    private var hasCaptured = false
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var logoutIcon: ImageView
    private var feriaSeleccionadaId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificar_persona)
        val consultaButton: Button = findViewById(R.id.button_consultar_cedula)

        val cedulaInput = findViewById<EditText>(R.id.cedulaInput)

        apiService = ApiClient.getApiService()
        logoutIcon = findViewById(R.id.logout_icon)
        previewView = findViewById(R.id.previewView)
        scanResult = findViewById(R.id.scanResult)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()
        mediaPlayer = MediaPlayer.create(this, R.raw.pip)
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        feriaSeleccionadaId = sharedPreferences.getInt("feria_seleccionada", -1)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS
            )
        }
        // Configuración del botón de logout
        logoutIcon.setOnClickListener {
            logout()
        }

        consultaButton.setOnClickListener {
            // Obtén la cédula ingresada en el campo de texto
            val cedula = cedulaInput.text.toString().trim()

            verificarUsuario(cedula) { usuario ->
                if (usuario == null) {
                    showManualRegistrationDialog(cedula) // Mostrar formulario
                } else {
                    val intent = Intent(this, ComprasActivity::class.java)
                    intent.putExtra("CEDULA", cedula)
                    startActivity(intent)
                }
            }
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Obtener la rotación de la pantalla (Display)
            val rotation = windowManager.defaultDisplay.rotation

            val preview = Preview.Builder()
                .setTargetRotation(rotation) // Configura la rotación correcta
                .build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetRotation(rotation) // Configura la rotación para el análisis de imagen
                .build().also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        when (barcode.valueType) {
                            Barcode.TYPE_TEXT -> {
                                val scannedData = barcode.displayValue
                                scanResult.text = scannedData
                                println(scannedData)

                                if (!hasCaptured) {
                                    mediaPlayer.start()
                                    hasCaptured = true

                                    // Llama a la función para mostrar el modal
                                    if (scannedData != null) {
                                        showDetailsDialog(scannedData)
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    hasCaptured = false
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun showDetailsDialog(scannedData: String) {
        // Separar los datos del código QR
        val details = scannedData.split("|")

        // Crear el diálogo
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_form)

        val layoutParams = dialog.window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams

        // Vincular los elementos del layout
        val cedulaTextView: TextView = dialog.findViewById(R.id.text_cedula)
        val nombreTextView: TextView = dialog.findViewById(R.id.text_nombre)
        val sexoTextView: TextView = dialog.findViewById(R.id.text_sexo)
        val provinciaTextView: TextView = dialog.findViewById(R.id.text_provincia)
        val fechaNacimientoTextView: TextView = dialog.findViewById(R.id.text_fecha_nacimiento)
        val nacionalidadTextView: TextView = dialog.findViewById(R.id.text_nacionalidad)
        val closeButton: Button = dialog.findViewById(R.id.button_close)

        // Asignar valores a los TextViews
        cedulaTextView.text = details[0]
        nombreTextView.text = "${details[1]} ${details[2]} ${details[3]}"
        sexoTextView.text = details[4]
        provinciaTextView.text = details[5]
        val fechaNacimientoRaw = details[6]
        if (fechaNacimientoRaw.length == 8) {
            val año = fechaNacimientoRaw.substring(0, 4)
            val mes = fechaNacimientoRaw.substring(4, 6)
            val dia = fechaNacimientoRaw.substring(6, 8)
            val fechaFormateada = "$dia/$mes/$año"
            fechaNacimientoTextView.text = fechaFormateada
        }
        nacionalidadTextView.text = details[7]

        // Comprobar si el usuario existe usando Retrofit
        val cedula = details[0] // Cédula
        val cliente =   "${details[1]} ${details[2]} ${details[3]}"
        verificarUsuario(cedula) { usuario ->
            if (usuario == null) {
                // El usuario no existe, agregarlo a la base de datos
                agregarUsuario(cedula, cliente, details[5], fechaNacimientoRaw, details[7], getCurrentDate())
            } else {
                val intent = Intent(this, ComprasActivity::class.java)
                intent.putExtra("CEDULA", cedula)
                startActivity(intent)
                //Toast.makeText(this, "Consultando historial de compras...", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el botón de cerrar
        closeButton.setOnClickListener {
            dialog.dismiss()
            clearScanResult()
            restartScanning()
        }

        dialog.setOnCancelListener {
            clearScanResult()
            restartScanning()
        }


        // Mostrar el diálogo
        dialog.show()
    }


    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Método para verificar si el usuario existe usando Retrofit
    private fun verificarUsuario(cedula: String, callback: (Usuario?) -> Unit) {
        apiService.verificarUsuario(cedula).enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body() // Esto debería ser una lista
                    if (usuarios != null && usuarios.isNotEmpty()) {
                        callback(usuarios[0]) // Retorna el primer usuario encontrado
                    } else {
                        callback(null) // No se encontró el usuario
                    }
                } else {
                    callback(null) // Manejar error si no es exitosa
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                //Toast.makeText(this@VerificarPersonaActivity, "Error en la verificación: ${t.message}", Toast.LENGTH_SHORT).show()
                // despues con calma verificas como solucionar el error cuando no existe
                callback(null)
            }
        })
    }



    private fun showManualRegistrationDialog(cedula: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manual_registration)


        // Ajustar el tamaño del diálogo
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% del ancho de la pantalla
            WindowManager.LayoutParams.WRAP_CONTENT // Ajusta la altura automáticamente
        )

        val cedulaEditText: EditText = dialog.findViewById(R.id.edit_cedula)
        val nombreEditText: EditText = dialog.findViewById(R.id.edit_nombre)
        val provinciaEditText: EditText = dialog.findViewById(R.id.edit_provincia)
        val fechaNacimientoEditText: EditText = dialog.findViewById(R.id.edit_fecha_nacimiento)
        val nacionalidadEditText: EditText = dialog.findViewById(R.id.edit_nacionalidad)
        val saveButton: Button = dialog.findViewById(R.id.button_save)
        cedulaEditText.setText(cedula)
        // Configurar el DatePickerDialog para la fecha de nacimiento
        fechaNacimientoEditText.setOnClickListener {
            showDatePickerDialog(fechaNacimientoEditText)
        }

        saveButton.setOnClickListener {
            val cedula = cedulaEditText.text.toString()
            val nombre = nombreEditText.text.toString()
            val provincia = provinciaEditText.text.toString()
            val fechaNacimiento = fechaNacimientoEditText.text.toString()
            val nacionalidad = nacionalidadEditText.text.toString()

            // Obtener la fecha actual como fecha de registro en formato "yyyy-MM-dd"
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Validar que todos los campos están completos
            if (cedula.isNotEmpty() && nombre.isNotEmpty() && provincia.isNotEmpty() &&
                fechaNacimiento.isNotEmpty() && nacionalidad.isNotEmpty()) {

                // Verificar si el usuario ya existe
                verificarUsuario(cedula) { usuario ->
                    if (usuario == null) {
                        // El usuario no existe, agregarlo a la base de datos
                        agregarUsuario(cedula, nombre, provincia, fechaNacimiento, nacionalidad, fechaRegistro)
                        dialog.dismiss()
                    } else {
                        // Informar que el usuario ya existe

                        val intent = Intent(this, ComprasActivity::class.java)
                        intent.putExtra("CEDULA", cedula)
                        startActivity(intent)
                        Toast.makeText(this, "El usuario ya existe.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    // Método para mostrar el DatePickerDialog
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Formatear la fecha seleccionada
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            editText.setText(formattedDate) // Establecer la fecha en el EditText
        }, year, month, day)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // No permitir fechas futuras
        datePickerDialog.show()
    }

    private fun agregarUsuario(
        cedula: String,
        nombre: String,
        provincia: String,
        fecha_nacimiento: String,
        nacionalidad: String,
        fecha_registro: String
    ) {
        val usuario = Usuario(
            cedula = cedula,
            nombre = nombre,
            provincia = provincia,
            fecha_nacimiento = fecha_nacimiento,
            nacionalidad = nacionalidad,
            fecha_registro = fecha_registro
        )

        apiService.agregarUsuario(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@VerificarPersonaActivity, "Usuario agregado con éxito", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@VerificarPersonaActivity, ComprasActivity::class.java)
                    intent.putExtra("CEDULA", cedula)
                    startActivity(intent)
                } else {
                    Log.e("API", "Error al agregar usuario: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Log.e("API", "Fallo al agregar usuario: ${t.message}")
            }
        })
    }

    private fun restartScanning() {
        hasCaptured = false // Reiniciar la captura
        // Re-iniciar el análisis de imágenes
        startCamera() // Puedes optar por reiniciar el análisis de cámara aquí si es necesario
    }

    private fun clearScanResult() {
        scanResult.text = "Resultado del escaneo"
        hasCaptured = false
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
        mediaPlayer.release()
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}