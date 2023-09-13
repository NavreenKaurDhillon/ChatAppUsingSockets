package com.project.blackgirluser.util.imagepickerhelper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewbinding.ViewBinding
import com.example.chatappusingsockets.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


abstract class ImagePickerUtility<VB : ViewBinding> : AppCompatActivity(){

    private var _binding: VB? = null
    val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = getViewBinding()
        setContentView(binding.root)
        setOnCreate()
    }

    abstract fun getViewBinding(): VB

    abstract fun setOnCreate()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private var mActivity: Activity? = null
    var mVideoDialog: Boolean = false
    private var mCode = 0
    private lateinit var mImageFile: File

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )




    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.isNotEmpty()) {
                permissions.entries.forEach {
                    Log.d("permissions", "${it.key} = ${it.value}")
                }

                val readStorage = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                val writeStorage = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                val camera = permissions[Manifest.permission.CAMERA]

                if (readStorage == true && writeStorage == true && camera == true) {
                    Log.e("permissions", "Permission Granted Successfully")
                    imageDialog()

                } else {
                    Log.e("permissions", "Permission not granted")
                    checkPermissionDenied(permissions.keys.first())
                }

            }

        }


    private val videoCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data

                val selectedVideoPath = getPath(contentURI!!)
                selectedImage(selectedVideoPath, mCode)
            }
        }

    private val imageCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = Uri.fromFile(mImageFile)
                val picturePath = getAbsolutePath(uri)
                selectedImage(picturePath, mCode)
            }

        }


    private val videoGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data

                val selectedVideoPath = getPath(contentURI!!)
                /* val a = File(contentURI)*/
                val a = "/storage/emulated/0/Movies/Instagram/VID_53050323_203748_988.mp4"
                selectedImage(selectedVideoPath, mCode)
            }
        }

    private val imageGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                val picturePath = getAbsolutePath(uri!!)
                selectedImage(picturePath, mCode)
            }
        }

    open fun getImage(activity: Activity, code: Int, videoDialog: Boolean) {

        //*****videoDialog -> put false for pick the Image.*****
        //*****videoDialog -> put true for pick the Video.*****

        mActivity = activity
        mCode = code
        mVideoDialog = videoDialog

        when {
            hasPermissions(permissions) -> {
                Log.e("Permissions", "Permissions Granted")
                imageDialog()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                checkPermissionDenied(Manifest.permission.CAMERA)
            }
            else -> {
                Log.e("Permissions", "Request for Permissions")
                requestPermission()
            }
        }
    }


    private fun imageDialog() {
        val dialog = Dialog(mActivity!!)
        dialog.setContentView(R.layout.camera_gallery_popup)
//        dialog.window!!.attributes.windowAnimations = R.style.Theme_Dialog
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera = dialog.findViewById<TextView>(R.id.tvCamera)
        val cancel = dialog.findViewById<TextView>(R.id.tv_cancel)
        val gallery = dialog.findViewById<TextView>(R.id.tvGallery)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            if (mVideoDialog) {
                captureVideo()
            } else {
                captureImage()
            }

        }

        gallery.setOnClickListener {
            dialog.dismiss()
            if (mVideoDialog) {
                openGalleryForVideo()
            } else {
                openGalleryForImage()
            }
        }
        dialog.show()
    }


    private fun captureVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCameraLauncher.launch(intent)

    }

    private fun captureImage()
    {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        try {
            createImageFile(mActivity!!, imageFileName, ".jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(Objects.requireNonNull(applicationContext),
            "com.example.chatappusingsockets.provider", mImageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        imageCameraLauncher.launch(intent)
    }


    private fun openGalleryForVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        videoGalleryLauncher.launch(
            Intent.createChooser(intent, "Select Video")
        )

    }


    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI)
        imageGalleryLauncher.launch(intent)
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, name: String, extension: String) {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mImageFile = File.createTempFile(
            name,
            extension,
            storageDir
        )
    }


    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(mActivity!!, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionDenied(permissions: String) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle("alert").setMessage("permissionRequired")
                    .setPositiveButton(
                       "ok"
                    ) { dialog, which -> requestPermission() }
                    .setNegativeButton(
                        "cancel"
                    ) { dialog, which ->

                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, com.karumi.dexter.R.color.error_color_material_dark
                    )
                )
            }
            dialog.show()
        } else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle("alert").setMessage("permissionRequired")
                    .setCancelable(
                        false
                    )
                    .setPositiveButton("openSettings") { dialog, which ->
                        //finish()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                "com.example.chatappusingsockets.provider",
                                null
                            )
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, com.karumi.dexter.R.color.error_color_material_dark
                    )
                )
            }
            dialog.show()
        }
    }

    private fun requestPermission() {
        requestMultiplePermissions.launch(permissions)
    }


    //------------------------Return Uri file to String Path ------------------//
    @SuppressLint("Recycle")
    private fun getAbsolutePath(uri: Uri): String {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = mActivity!!.contentResolver.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }

    private fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? =
            mActivity!!.contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }

    abstract fun selectedImage(imagePath: String?, code: Int)
}