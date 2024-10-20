package com.example.pdfscanner

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pdfscanner.databinding.ActivityMainBinding
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>

    // Request code for storage permissions
    private val storagePermissionCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register the scanner launcher in onCreate
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanResult?.pages?.forEach { page ->
                    saveToDownloads(page.imageUri)
                }
                scanResult?.pdf?.let { pdf ->
                    savePdfToDownloads(pdf.uri)
                }
            }
        }

        // Set button click listener
        binding.btnScanDocument.setOnClickListener {
            // Prepare the document scanner options
            val options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(SCANNER_MODE_FULL)
                .setGalleryImportAllowed(true)
                .setPageLimit(10)
                .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
                .build()

            val scanner = GmsDocumentScanning.getClient(options)

            // Get the scanning intent and launch it
            scanner.getStartScanIntent(this).addOnSuccessListener { intentSender ->
                // Launch the registered scannerLauncher with the intentSender
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API level 33) and above: Use the new media permissions
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    storagePermissionCode
                )
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 (Pie) and below: Request WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    storagePermissionCode
                )
            }
        } else {
            // Android 10 - 12: Use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    storagePermissionCode
                )
            }
        }
    }


    private fun saveToDownloads(imageUri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "scanned_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val resolver = contentResolver
        val outputStream = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
            resolver.openOutputStream(uri)
        }

        contentResolver.openInputStream(imageUri)?.use { inputStream ->
            outputStream?.use {
                inputStream.copyTo(it)
            }
        }

        Toast.makeText(this, "Image saved to Gallery", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            storagePermissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission was denied
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun savePdfToDownloads(pdfUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29 and above
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "scanned_document_${System.currentTimeMillis()}.pdf")
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = contentResolver
            val outputStream = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                resolver.openOutputStream(uri)
            }

            contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                outputStream?.use {
                    inputStream.copyTo(it)
                }
            }

            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
        } else {
            // API 28 and below
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, "scanned_document_${System.currentTimeMillis()}.pdf")
                put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = contentResolver
            val outputStream = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)?.let { uri ->
                resolver.openOutputStream(uri)
            }

            contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                outputStream?.use {
                    inputStream.copyTo(it)
                }
            }

            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
        }
    }
}