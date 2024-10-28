package com.example.ima_tienda

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
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

class VerificarPersonaActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var scanResult: TextView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private var hasCaptured = false
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificar_persona)


        previewView = findViewById(R.id.previewView)
        scanResult = findViewById(R.id.scanResult)

        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()
        mediaPlayer = MediaPlayer.create(this, R.raw.pip)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS
            )
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
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
        dialog.setContentView(R.layout.dialog_form) // Asegúrate de que este sea el nombre correcto de tu archivo XML

        val layoutParams = dialog.window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT // O puedes usar un tamaño fijo en dp
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT // Ajusta según sea necesario
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
        cedulaTextView.text = details[0] // Cédula
        nombreTextView.text = "${details[1]} ${details[2]} ${details[3]}" // Nombre
        sexoTextView.text = details[4] // Sexo (se asume que está en el índice 3)
        provinciaTextView.text = details[5] // Provincia (se asume que está en el índice 4)
        val fechaNacimientoRaw = details[6] // "19960713"
        if (fechaNacimientoRaw.length == 8) { // Asegúrate de que la longitud sea correcta
            val año = fechaNacimientoRaw.substring(0, 4)
            val mes = fechaNacimientoRaw.substring(4, 6)
            val dia = fechaNacimientoRaw.substring(6, 8)
            val fechaFormateada = "$dia/$mes/$año" // Formato "dd/MM/yyyy"
            fechaNacimientoTextView.setText(fechaFormateada)
        }// Fecha de Nacimiento (se asume que está en el índice 5)
        nacionalidadTextView.text = details[7] // Nacionalidad (se asume que está en el índice 6)

        // Configurar el botón de cerrar
        closeButton.setOnClickListener {
            dialog.dismiss()
            clearScanResult() // Limpiar el resultado antes de reiniciar el escaneo
            restartScanning() // Reiniciar el análisis
        }
        dialog.setOnCancelListener {
            clearScanResult() // Limpiar el resultado antes de reiniciar el escaneo
            restartScanning() // Reiniciar el análisis
        }

        // Mostrar el diálogo
        dialog.show()
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