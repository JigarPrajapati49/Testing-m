package com.frenzin.machinemaintain.utills

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ImageUtils {

    companion object{

        private val TAG = "ImageUtils"

        fun getOutputMediaFile(): File? {

            // External sdcard location
            val mediaStorageDir = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "cache_image"
            )

            Log.e(TAG, "mediaStorageDir: $mediaStorageDir")

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(
                        "Merchant", "Oops! Failed create "
                                + "cache_image" + " directory"
                    )
                }
            }
            // Create a media file name
            val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())

            return File(
                mediaStorageDir.path + File.separator
                        + "IMG_" + timeStamp + "_" + Random().nextInt() + ".jpg"
            )
        }

        /**
         * Get a file path from a Uri. This will get the the path for Storage Access
         * Framework Documents, as well as the _data field for the MediaStore and
         * other file-based ContentProviders.
         *
         * @param context The context.
         * @param uri     The Uri to query.
         * @author paulburke
         */
        fun getPath(context: Context, uri: Uri): String? {
            Log.e(TAG, "getPath : $uri")
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            var isDoc = false
            var docId = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                isDoc = DocumentsContract.isDocumentUri(context, uri)
                if (isDoc) docId = DocumentsContract.getDocumentId(uri)
            }
            // DocumentProvider
            if (isKitKat && isDoc) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(docId)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if (("image" == type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if (("video" == type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if (("audio" == type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                if (isGooglePhotosUri(uri)) return uri.lastPathSegment else if (isNewGooglePhotosUri(
                        uri
                    )
                ) {
                    return copyFile(context, uri)
                }
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun copyFile(context: Context, uri: Uri): String? {
            var filePath: String
            var inputStream: InputStream? = null
            var outStream: BufferedOutputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val file = getOutputMediaFile()
                filePath = file!!.absolutePath
                outStream = BufferedOutputStream(FileOutputStream(filePath))
                val buf = ByteArray(2048)
                var len: Int
                while ((inputStream!!.read(buf).also { len = it }) > 0) {
                    outStream.write(buf, 0, len)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                filePath = ""
            } catch (e: NullPointerException) {
                e.printStackTrace()
                filePath = ""
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    outStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return filePath
        }

        private fun isNewGooglePhotosUri(uri: Uri): Boolean {
            return ("com.google.android.apps.photos.contentprovider" == uri.authority)
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return ("com.android.externalstorage.documents" == uri.authority)
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return ("com.android.providers.downloads.documents" == uri.authority)
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri): Boolean {
            return ("com.android.providers.media.documents" == uri.authority)
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return ("com.google.android.apps.photos.content" == uri.authority)
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        private fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    (uri)!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        fun getImageUri(inContext: Context?, inImage: Bitmap): Uri {

            val tempFile = File.createTempFile("temprentpk", ".png")
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val bitmapData = bytes.toByteArray()
            val fileOutPut = FileOutputStream(tempFile)
            fileOutPut.write(bitmapData)
            fileOutPut.flush()
            fileOutPut.close()
            return Uri.fromFile(tempFile)
        }

        fun hasPermission(context: Context?, permissions: Array<String?>?): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) return false
                }
            }
            return true
        }

    }
}