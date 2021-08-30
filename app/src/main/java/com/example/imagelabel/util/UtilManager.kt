package com.example.imagelabel.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.TypedValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.imagelabel.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class UtilManager(private val activity: Activity) {
    fun isUserSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(activity) != null
    }

    fun initiateGoogleSingIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .build()
        val client = GoogleSignIn.getClient(activity, options)
        activity.startActivityForResult(
            client.signInIntent,
            Constants.GOOGLE_SING_IN_REQ_CODE
        )
    }

    private fun getDriveServices(): Drive? {
        GoogleSignIn.getLastSignedInAccount(activity)?.let { googleSignInAccount ->
            val credentials =
                GoogleAccountCredential.usingOAuth2(activity, listOf(DriveScopes.DRIVE_FILE))
            credentials.selectedAccount = googleSignInAccount.account
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credentials
            ).setApplicationName(activity.getString(R.string.app_name)).build()
        }
        return null
    }

    fun uploadToDrive(uri: Uri, file_name: String, callBack: UploadCallBack) {
        try {
            val file: File = activity.getFileFromContentUri(uri)!!
            val gFile = com.google.api.services.drive.model.File().apply { name = file_name }
            val fileContent = FileContent(activity.contentResolver.getType(uri), file)
            val driveService = getDriveServices()
            val result = driveService!!.Files().create(gFile, fileContent).execute()
            callBack.onSuccess(JSONObject(result))
        } catch (e: Exception) {
            e.printStackTrace()
            callBack.onFailure(e)
        }
    }

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestAllPendingPermissions(): Int {
        val list = arrayListOf<String>()
        if (!checkPermission(Manifest.permission.INTERNET))
            list.add(Manifest.permission.INTERNET)
        if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return if (list.isEmpty()) 0 else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), Constants.ALL_PERMISSION_REQ_CODE)
            list.size
        }
    }

    companion object {
        fun Context.getFileFromContentUri(uri: Uri): File? {
            return try {
                val bytes = contentResolver.openInputStream(uri)!!.readBytes()
                val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File.createTempFile("temp${Date().time}", ".png")
                file.writeBytes(bytes)
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null;
            }
        }

        fun Context.getUriForBitmap(bitmap: Bitmap): Uri {
            val bytes = ByteArrayOutputStream().let { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("temp${Date().time}", ".png", dir)
            file.writeBytes(bytes)
            return FileProvider.getUriForFile(
                this,
                "com.example.imagelabel.fileprovider",
                file
            )
        }

        fun dpToPx(dp: Float): Int = Resources.getSystem().displayMetrics.let {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                it
            )
        }.toInt()
    }
}