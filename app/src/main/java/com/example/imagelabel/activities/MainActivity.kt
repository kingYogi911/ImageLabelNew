package com.example.imagelabel.activities

import android.app.ActionBar
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.imagelabel.R
import com.example.imagelabel.adapters.BottomSheetAdapter
import com.example.imagelabel.customViews.BoxActionListener
import com.example.imagelabel.customViews.PolygonView
import com.example.imagelabel.data.MyColor
import com.example.imagelabel.data.Polygon
import com.example.imagelabel.databinding.ActivityMainBinding
import com.example.imagelabel.databinding.BottomSheetCommonBinding
import com.example.imagelabel.databinding.PolygonDefaultBinding
import com.example.imagelabel.util.*
import com.example.imagelabel.util.UtilManager.Companion.dpToPx
import com.example.imagelabel.util.UtilManager.Companion.getUriForBitmap
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


@Suppress("ClassName")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).also {
            it.labelTypeIv.setOnClickListener(this)
            it.color.setOnClickListener(this)
            it.crop.setOnClickListener(this)
            it.upload.setOnClickListener(this)
        }
    }
    private var uri: Uri? = null
    private val polygonList by lazy { ArrayList<PolygonView>() }
    private var bsd: BottomSheetDialog? = null
    private var isImageCaptured = false
    private var imgPath: String? = null
    var activeLabel: PolygonView? = null
    var activeColor: MyColor? = null
    private val utilManager by lazy { UtilManager(this) }
    private val pd by lazy { AlertDialog.Builder(this).setView(ProgressBar(this).apply {
        layoutParams= ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        isIndeterminate=true
    }).setCancelable(false).create() }
    enum class BOTTOM_SHEET {
        POLYGON, COLOR
    }

    private val boxActionListener = object : BoxActionListener {
        override fun onRemovePressed(v: View) {
            activityMainBinding.container.removeView(v)
        }

        override fun onSelected(v: View) {
            polygonList.forEach { polygonView ->
                if (polygonView == v) {
                    activeLabel = polygonView
                    polygonView.showOptions()
                } else {
                    polygonView.hideOptions()
                }
            }
        }
    }

    private val polygonSelectedListener = object : PolygonSelectedListener {
        override fun polygonSelected(polygon: Polygon) {
            addPolygon(polygon)
            activityMainBinding.labelTypeIv.setImageResource(polygon.resId)
            bsd?.dismiss()
        }
    }

    private val colorSelectedListener = object : ColorSelectedListener {
        override fun colorSelected(color: MyColor) {
            activeColor = color
            activeLabel?.setTintColor(color.colorRes)
            activityMainBinding.color.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity,
                    color.colorRes
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        colorSelectedListener.colorSelected(MyColor(R.color.green, "Green"))
        activityMainBinding.labelTypeIv.setImageResource(R.drawable.hexagon)
        activeLabel = null
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume()")
        if (!isImageCaptured) {
            captureImage()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.label_type_iv -> showBottomSheet(BOTTOM_SHEET.POLYGON)
            R.id.color -> showBottomSheet(BOTTOM_SHEET.COLOR)
            R.id.crop -> CropImage.activity(uri)
                .start(this)
            R.id.upload -> {
                polygonList.forEach { it.hideOptions() }
                val bitmap = activityMainBinding.editableImageLayout.getFinalImage()
                //activityMainBinding.container.removeAllViews()
                //activityMainBinding.editableImageLayout.setImageBitmap(bitmap)
                this.uri = getUriForBitmap(bitmap)
                uploadFile()
            }
            R.id.editable_image_layout -> polygonList.forEach { it.hideOptions() }
        }
    }

    private fun captureImage() {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile("temp${Date().time}", ".png", storageDir).let {
            uri = FileProvider.getUriForFile(
                this,
                "com.example.imagelabel.fileprovider",
                it
            )
            Log.e("MAinActivity", "uri=$uri")
            imgPath = it.absolutePath
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(intent, Constants.IMAGE_CAPTURE_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("MainActivity", "onActivityResult")
        Log.e("MainActivity", "requestCode=$requestCode")
        Log.e("MainActivity","resultCode =$resultCode, ${resultCode == RESULT_OK}")
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Constants.IMAGE_CAPTURE_REQ_CODE -> {
                    Log.e("MainActivity", "resultCode=$resultCode")
                    val bitmap = BitmapFactory.decodeFile(imgPath)
                    activityMainBinding.editableImageLayout.setImageBitmap(bitmap)
                    isImageCaptured = true
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val bitmap = contentResolver.openInputStream(result.uri)!!.let {
                        BitmapFactory.decodeStream(it)
                    }
                    activityMainBinding.editableImageLayout.setImageBitmap(bitmap)
                }
                Constants.GOOGLE_SING_IN_REQ_CODE -> {
                    handleSingInResponseData(data)
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSingInResponseData(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnCompleteListener {
            println("Sign in successful,email:${it.result.email}")
            uploadFile()
        }.addOnCanceledListener {
            Toast.makeText(this, "Sing in failed!!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadFile() {
        if (utilManager.isUserSignedIn()) {
            pd.show()
            GlobalScope.launch {
                utilManager.uploadToDrive(uri!!,uploadCallBack)
            }
        } else {
            Log.e(TAG,"user not signed in, initiating sing in")
            utilManager.initiateGoogleSingIn()
        }
    }

    private val uploadCallBack=object :UploadCallBack{
        override fun onSuccess(jsonObject: JSONObject) {

            runOnUiThread {
                pd.dismiss()
                AlertDialog.Builder(this@MainActivity)
                    .setCancelable(false)
                    .setTitle("Image has been Saved to Drive SuccessFully")
                    .setMessage("You can continue with a new Image or Exit")
                    .setPositiveButton("Exit"
                    ) { _, _ -> this@MainActivity.finish()}
                    .setNegativeButton("Continue"){
                            _, _->
                        Intent(this@MainActivity,MainActivity::class.java).let {
                            startActivity(it)
                            this@MainActivity.finish()
                        }
                    }.show()
            }
        }

        override fun onFailure(e: Exception) {
            runOnUiThread {
                pd.dismiss()
                Toast.makeText(this@MainActivity,"Something Went Wrong",Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun addPolygon(polygon: Polygon) {
        polygonList.forEach { v ->
            v.hideOptions()
        }
        val pv = PolygonDefaultBinding.inflate(layoutInflater).root as PolygonView
        pv.x = (activityMainBinding.container.width / 2).toFloat()
        pv.y = (activityMainBinding.container.height / 2).toFloat()
        pv.isClickable = true
        pv.isFocusable = true
        pv.setData(polygon.resId, boxActionListener)
        activityMainBinding.container.addView(pv)
        pv.layoutParams.width = dpToPx(200f)
        pv.layoutParams.height = dpToPx(200f)
        polygonList.add(pv)
        activeLabel = pv
        activeColor?.let {
            activeLabel!!.setTintColor(it.colorRes)
        }
    }

    private fun showBottomSheet(type: BOTTOM_SHEET) {
        bsd = BottomSheetDialog(this)
        val list = when (type) {
            BOTTOM_SHEET.POLYGON -> arrayListOf(
                Polygon(R.drawable.square, "Square"),
                Polygon(R.drawable.circle, "Circle"),
                Polygon(R.drawable.triangle, "Triangle"),
                Polygon(R.drawable.hexagon, "Hexagon")
            )
            BOTTOM_SHEET.COLOR -> arrayListOf(
                MyColor(R.color.black, "Black"),
                MyColor(R.color.blue, "blue"),
                MyColor(R.color.green, "Green"),
                MyColor(R.color.yellow, "Yellow"),
                MyColor(R.color.red, "Red"),
                MyColor(R.color.violet, "Violet")
            )
        }
        BottomSheetCommonBinding.inflate(layoutInflater).apply {
            bottomSheetRv.adapter = BottomSheetAdapter(list, type).also {
                it.polygonSelectedListener = this@MainActivity.polygonSelectedListener
                it.colorSelectedListener = this@MainActivity.colorSelectedListener
            }
        }.also {
            bsd!!.setContentView(it.root)
        }
        bsd!!.show()
    }
    companion object{
        val TAG=MainActivity::class.simpleName
    }
}