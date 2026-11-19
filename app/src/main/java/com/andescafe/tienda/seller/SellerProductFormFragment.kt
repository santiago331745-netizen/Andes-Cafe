package com.andescafe.tienda.seller

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.firebase.firestore.*
import com.andescafe.tienda.R
import com.andescafe.tienda.common.*
import com.andescafe.tienda.data.*

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import java.io.File

/**
 * Crear o editar un producto. La imagen se toma con la cámara o se elige de la galería
 * y se reduce y guarda como data URI Base64 en el documento del producto (Firestore), sin Storage.
 */
class SellerProductFormFragment : Fragment(R.layout.fragment_seller_product_form) {

    private var productId = ""
    private var imageUri: Uri? = null          // imagen nueva elegida por el usuario
    private var cameraUri: Uri? = null         // destino temporal de la foto
    private var currentImageUrl = ""           // URL ya guardada (al editar)
    private var sellerName = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { imageUri = uri; showPreview(uri) }
    }
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { imageUri = cameraUri; cameraUri?.let { showPreview(it) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Repo.loadProfile { sellerName = it?.name ?: "" }

        productId = arguments?.getString("id") ?: ""
        if (productId.isNotEmpty()) {
            Repo.db.collection("products").document(productId).get().addOnSuccessListener { d ->
                val p = d.toObject(Product::class.java) ?: return@addOnSuccessListener
                if (!isAdded) return@addOnSuccessListener
                view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName).setText(p.name)
                view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDesc).setText(p.description)
                view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPrice).setText(p.price.toString())
                view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStock).setText(p.stock.toString())
                currentImageUrl = p.imageUrl
                if (p.imageUrl.isNotEmpty()) showPreview(p.imageUrl)
            }
        }

        view.findViewById<View>(R.id.btnGallery).setOnClickListener { pickImage.launch("image/*") }
        view.findViewById<View>(R.id.btnCamera).setOnClickListener {
            // La cámara escribe en un archivo temporal compartido mediante FileProvider
            val dir = File(requireContext().cacheDir, "images").apply { mkdirs() }
            val file = File.createTempFile("prod_", ".jpg", dir)
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            cameraUri = uri
            takePicture.launch(uri)
        }
        view.findViewById<View>(R.id.btnCancel).setOnClickListener { findNavController().popBackStack() }
        view.findViewById<View>(R.id.btnSave).setOnClickListener { save(view) }
    }

    /** Muestra la imagen elegida o descargada en la vista previa. */
    private fun showPreview(model: Any) {
        val iv = requireView().findViewById<ImageView>(R.id.ivPreview)
        iv.setPadding(0, 0, 0, 0)
        iv.imageTintList = null
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this).load(model).into(iv)
    }

    /** Valida, convierte la imagen (si hay una nueva) y guarda el documento del producto. */
    private fun save(view: View) {
        val name = view.str(R.id.etName)
        val price = view.str(R.id.etPrice).toLongOrNull()
        val stock = view.str(R.id.etStock).toLongOrNull()
        if (name.isEmpty() || price == null || stock == null || price <= 0) {
            toast("Completa nombre, precio (mayor a 0) y existencias"); return
        }
        val id = productId.ifEmpty { Repo.db.collection("products").document().id }
        val btn = view.findViewById<View>(R.id.btnSave)
        btn.isEnabled = false

        fun persist(url: String) {
            val p = Product(id, name, view.str(R.id.etDesc), price, stock, url, Repo.uid(), sellerName)
            Repo.db.collection("products").document(id).set(p)
                .addOnSuccessListener { toast("Producto guardado"); findNavController().popBackStack() }
                .addOnFailureListener { btn.isEnabled = true; toast("Error: ${it.localizedMessage}") }
        }

        val uri = imageUri
        if (uri == null) persist(currentImageUrl) else {
            // Sin Firebase Storage: la imagen se reduce y se guarda como data URI dentro del documento
            Thread {
                val dataUri = try { encodeImage(uri) } catch (e: Exception) { null }
                activity?.runOnUiThread {
                    if (dataUri == null) { btn.isEnabled = true; toast("No se pudo leer la imagen") } else persist(dataUri)
                }
            }.start()
        }
    }

    /**
     * Reduce la imagen a un máximo de 640 px, la comprime en JPEG (calidad 70) y la devuelve como
     * data URI Base64. Así pesa unos 40-90 KB y cabe en un documento de Firestore (límite 1 MiB).
     */
    private fun encodeImage(uri: Uri): String {
        val ctx = requireContext()
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)!!.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1280 || bounds.outHeight / sample > 1280) sample *= 2
        val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = ctx.contentResolver.openInputStream(uri)!!.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) }!!
        val scale = 640f / maxOf(bmp.width, bmp.height)
        val scaled = if (scale < 1f) android.graphics.Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
        val out = java.io.ByteArrayOutputStream()
        scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
        return "data:image/jpeg;base64," + android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
    }
}
