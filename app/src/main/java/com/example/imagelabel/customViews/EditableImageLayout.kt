package com.example.imagelabel.customViews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.example.imagelabel.R
import com.example.imagelabel.databinding.EditableImageLayoutBinding

class EditableImageLayout : RelativeLayout {
    private var bitmap: Bitmap? = null
    private val binding: EditableImageLayoutBinding

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        binding = EditableImageLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        binding.iv.setImageResource(R.drawable.ic_launcher_background)
    }

    fun loadTestData() {
        val bitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)
        setImageBitmap(bitmap);
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        //this.bitmap = bitmap;
        binding.iv.setImageBitmap(bitmap)
        invalidate();
    }


    fun getFinalImage(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val cv = Canvas(bitmap)
        this.draw(cv)
        return bitmap;
    }

}