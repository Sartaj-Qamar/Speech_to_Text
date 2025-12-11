package com.codetech.speechtotext.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

@SuppressLint("StaticFieldLeak")
object PermissionUtils {
    private lateinit var context1: Context
    private const val toastMessage1 = "Some error occurred. Please try again!"
    private const val NEED_PERMISSION = "Need Permissions"

    private const val APP_NEED_PERMISSION =
        "This app needs permission to use this feature. You can grant them in app settings."
    private const val SETTING_TEXT = "Settings"
    fun checkPermission(
        context: Context,
        permissionArray: Array<String>,
        permissionListener: OnPermissionListener
    ) {

        context1 = context
        Dexter.withContext(context)
            .withPermissions(
                *permissionArray
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    if (report.areAllPermissionsGranted()) {
                        permissionListener.onPermissionSuccess()

                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog()

                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }


            }).withErrorListener {
                Log.d("TAG", "checkPermission: ${it.name}")
                Toast.makeText(context, toastMessage1, Toast.LENGTH_SHORT).show()
            }.check()
    }


    private fun showSettingsDialog() {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context1)
        builder.setTitle(NEED_PERMISSION)
        builder.setMessage(
            APP_NEED_PERMISSION
        )
        builder.setPositiveButton(
            SETTING_TEXT
        ) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", context1.packageName, null)
        intent.data = uri
        context1.startActivity(intent)
    }

    interface OnPermissionListener {
        fun onPermissionSuccess()

    }
}


val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
} else {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
}

val permissionsCamera = arrayOf(
    Manifest.permission.CAMERA
)