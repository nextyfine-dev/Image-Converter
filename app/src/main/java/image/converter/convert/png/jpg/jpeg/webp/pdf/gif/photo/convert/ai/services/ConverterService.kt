package image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import java.io.File
import java.io.IOException
import kotlin.math.pow

class ConverterService(private val context: Context) {
    fun getBitmapFromUri(imageUri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            null
        }
    }

    private fun getImageFilePath(imageUri: Uri): String? {
        try {
            val filePath: String?
            val cursor =
                context.applicationContext.contentResolver.query(imageUri, null, null, null, null)
            if (cursor == null) {
                filePath = imageUri.path
            } else {
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                filePath = if (columnIndex == -1) {
                    null
                } else {
                    cursor.getString(columnIndex)
                }
                cursor.close()
            }
            return filePath
        } catch (e: Exception) {
            return null
        }
    }


    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    fun rotateBitmapIfRequired(bitmap: Bitmap, imageUri: Uri): Bitmap? {
        val path = getImageFilePath(imageUri)
        return if (path != null) {
            val exif = ExifInterface(path)

            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } else {
            bitmap
        }
    }


    fun getBitmapFormat(format: String): Bitmap.CompressFormat {
        return when (format) {
            "png" -> Bitmap.CompressFormat.PNG
            "jpeg" -> Bitmap.CompressFormat.JPEG
            "webp" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.PNG
        }
    }


    fun changeQuality(n: Int): Int {
        val numDigits = n.toString().length
        return n / 10.0.pow((numDigits - 1).toDouble()).toInt()
    }

    fun getOutputPath(imageName: String, format: String, isImage: Boolean = false): String {
        return "${getOutputDir(isImage).absolutePath}/${imageName}.${format}"
    }

    private fun getOutputDir(isImage: Boolean): File {
        val rootDir =
            if (isImage) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_DOCUMENTS
        val mediaDir = File(
            Environment.getExternalStoragePublicDirectory(rootDir),
            "AI-Image-Converter"
        )
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        return if (mediaDir.exists())
            mediaDir else context.filesDir
    }


    private fun getPageSize(name: String): Rectangle {
        return when (name) {
            "LETTER" -> PageSize.LETTER
            "NOTE" -> PageSize.NOTE
            "LEGAL" -> PageSize.LEGAL
            "TABLOID" -> PageSize.TABLOID
            "EXECUTIVE" -> PageSize.EXECUTIVE
            "POSTCARD" -> PageSize.POSTCARD
            "A0" -> PageSize.A0
            "A1" -> PageSize.A1
            "A2" -> PageSize.A2
            "A3" -> PageSize.A3
            "A4" -> PageSize.A4
            "A5" -> PageSize.A5
            "A6" -> PageSize.A6
            "A7" -> PageSize.A7
            "A8" -> PageSize.A8
            "A9" -> PageSize.A9
            "A10" -> PageSize.A10
            "B0" -> PageSize.B0
            "B1" -> PageSize.B1
            "B2" -> PageSize.B2
            "B3" -> PageSize.B3
            "B4" -> PageSize.B4
            "B5" -> PageSize.B5
            "B6" -> PageSize.B6
            "B7" -> PageSize.B7
            "B8" -> PageSize.B8
            "B9" -> PageSize.B9
            "B10" -> PageSize.B10
            "ARCH_E" -> PageSize.ARCH_E
            "ARCH_D" -> PageSize.ARCH_D
            "ARCH_C" -> PageSize.ARCH_C
            "ARCH_B" -> PageSize.ARCH_B
            "ARCH_A" -> PageSize.ARCH_A
            "FLSA" -> PageSize.FLSA
            "FLSE" -> PageSize.FLSE
            "HALFLETTER" -> PageSize.HALFLETTER
            "_11X17" -> PageSize._11X17
            "ID_1" -> PageSize.ID_1
            "ID_2" -> PageSize.ID_2
            "ID_3" -> PageSize.ID_3
            "LEDGER" -> PageSize.LEDGER
            "CROWN_QUARTO" -> PageSize.CROWN_QUARTO
            "LARGE_CROWN_QUARTO" -> PageSize.LARGE_CROWN_QUARTO
            "DEMY_QUARTO" -> PageSize.DEMY_QUARTO
            "ROYAL_QUARTO" -> PageSize.ROYAL_QUARTO
            "CROWN_OCTAVO" -> PageSize.CROWN_OCTAVO
            "LARGE_CROWN_OCTAVO" -> PageSize.LARGE_CROWN_OCTAVO
            "DEMY_OCTAVO" -> PageSize.DEMY_OCTAVO
            "ROYAL_OCTAVO" -> PageSize.ROYAL_OCTAVO
            "SMALL_PAPERBACK" -> PageSize.SMALL_PAPERBACK
            "PENGUIN_SMALL_PAPERBACK" -> PageSize.PENGUIN_SMALL_PAPERBACK
            "PENGUIN_LARGE_PAPERBACK" -> PageSize.PENGUIN_LARGE_PAPERBACK
            else -> PageSize.A4
        }
    }

    fun convertImgToPdf(document: Document, imageUri: Uri, pdfPageSize: String, index: Int) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        val image = Image.getInstance(imageBytes)

        if (pdfPageSize == "Free Size") {
            document.pageSize = Rectangle(image.scaledWidth, image.scaledHeight)
        } else {
            document.pageSize = getPageSize(pdfPageSize)
        }
        val rotationAngle = image.rotation
        image.rotation = rotationAngle
        image.scaleToFit(document.pageSize.width, document.pageSize.height)
        val imageWidth = image.scaledWidth
        val imageHeight = image.scaledHeight

        val x = (document.pageSize.width - imageWidth) / 2
        val y = (document.pageSize.height - imageHeight) / 2
        image.setAbsolutePosition(x, y)

        if (index != 0) {
            document.add(image)
        }
        document.newPage()

        inputStream?.close()
    }


}