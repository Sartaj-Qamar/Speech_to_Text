package com.codetech.speechtotext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.codetech.speechtotext.databinding.FragmentOCRBinding
import com.codetech.speechtotext.databinding.FragmentTestBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream


class testFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentTestBinding
    private lateinit var cameraLayout: LinearLayout
    private lateinit var detectLayout: LinearLayout
    private lateinit var galleryLayout: LinearLayout
    private lateinit var imgView: ImageView
    private lateinit var textArea: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestBinding.inflate(inflater,container,false)
        detectLayout = binding.detect
        cameraLayout = binding.camera
        galleryLayout = binding.gallery

        imgView = binding.image
        textArea = binding.text

        detectLayout.setOnClickListener(this)
        cameraLayout.setOnClickListener(this)
        galleryLayout.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.detect -> {
                try {
                    runTextRecognition()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Görsel Seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.camera -> askCameraPermissions()
            R.id.gallery -> galleryAddPic()
        }
    }

    private companion object {
        const val REQUEST_IMAGE = 0
        const val CAMERA_PERM = 2
        const val REQUEST_GALLERY = 1
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE)
    }

    private fun askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERM)
        } else {
            dispatchTakePictureIntent()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(requireContext(), "Kamera İzni Kabul Edilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(mediaScanIntent, REQUEST_GALLERY)
    }

    private var galleryImage: Bitmap? = null

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val selectedImage = data?.extras?.get("data") as Bitmap
                    imgView.setImageBitmap(selectedImage)
                }
            }
            REQUEST_GALLERY -> {
                try {
                    val imageUri: Uri? = data?.data
                    val imageStream: InputStream? = imageUri?.let { requireActivity().contentResolver.openInputStream(it) }
                    galleryImage = BitmapFactory.decodeStream(imageStream)
                    imgView.setImageBitmap(galleryImage)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Görsel Galeriden Alınamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun processTextRecognition(visionText: Text) {
        val blocks: List<Text.TextBlock> = visionText.textBlocks
        if (blocks.isEmpty()) {
            Toast.makeText(requireContext(), "Görselde Metin Tespit Edilemedi", Toast.LENGTH_LONG).show()
            return
        }

        val text = StringBuilder()

        for (block in blocks) {
            val lines: List<Text.Line> = block.lines
            for (line in lines) {
                val elements: List<Text.Element> = line.elements
                for (element in elements) {
                    text.append(element.text).append(" ")
                }
            }
        }
        textArea.text = text.toString()
    }



    private fun runTextRecognition() {
        val bitmap = (imgView.drawable as BitmapDrawable).bitmap
        val rotationDegree = 0

        val image = InputImage.fromBitmap(bitmap, rotationDegree)
        val options = TextRecognizerOptions.Builder()
            .build()

        val recognizer: TextRecognizer = TextRecognition.getClient(options)

        val result: Task<Text> = recognizer.process(image)
            .addOnSuccessListener(OnSuccessListener { visionText ->
                processTextRecognition(visionText)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(requireContext(), "Başarısız İşlem", Toast.LENGTH_SHORT).show()
            })
    }
}