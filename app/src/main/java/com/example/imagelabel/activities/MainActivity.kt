package com.example.imagelabel.activities

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.example.imagelabel.R
import com.example.imagelabel.adapters.BottomSheetAdapter
import com.example.imagelabel.adapters.ColorSelectedListener
import com.example.imagelabel.adapters.PolygonSelectedListener
import com.example.imagelabel.customViews.BoxActionListener
import com.example.imagelabel.customViews.PolygonView
import com.example.imagelabel.data.MyColor
import com.example.imagelabel.data.Polygon
import com.example.imagelabel.databinding.ActivityMainBinding
import com.example.imagelabel.databinding.BottomSheetCommonBinding
import com.example.imagelabel.databinding.PolygonDefaultBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.imagelabel.data.Constants
import android.content.pm.ResolveInfo

import android.content.pm.PackageManager

import android.os.Build
import androidx.core.content.ContextCompat
import com.theartofdev.edmodo.cropper.CropImage
import android.R.attr.data
import android.content.ContentResolver


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
    private var x: Float = 0f
    private var y: Float = 0f
    private var bsd: BottomSheetDialog? = null
    private var isImageCaptured = false
    var imgPath: String? = null
    var activeLabel: PolygonView? = null
    var activeColor: MyColor? = null

    public enum class BOTTOM_SHEET {
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
            R.id.upload -> {//TODO Incomplete
                polygonList.forEach { it.hideOptions() }
                val bitmap = activityMainBinding.editableImageLayout.getFinalImage()
                activityMainBinding.container.removeAllViews()
                activityMainBinding.editableImageLayout.setImageBitmap(bitmap)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                val resInfoList: List<ResolveInfo> = getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    grantUriPermission(
                        packageName,
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        }
        startActivityForResult(intent, Constants.IMAGE_CAPTURE_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("MainActivity", "onActivityResult")
        Log.e("MainActivity", "requestCode=$requestCode")
        if (requestCode == Constants.IMAGE_CAPTURE_REQ_CODE) {
            Log.e("MainActivity", "resultCode=$resultCode")
            if (resultCode == Activity.RESULT_OK) {
                val bitmap = BitmapFactory.decodeFile(imgPath)
                activityMainBinding.editableImageLayout.setImageBitmap(bitmap)
                isImageCaptured = true
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                val bitmap = contentResolver.openInputStream(result.uri)!!.let {
                    BitmapFactory.decodeStream(it)
                }
                activityMainBinding.editableImageLayout.setImageBitmap(bitmap)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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

    fun showBottomSheet(type: BOTTOM_SHEET) {
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

    fun dpToPx(dp: Float): Int = Resources.getSystem().displayMetrics.let {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            it
        )
    }.toInt()
}