package com.example.imagelabel.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.imagelabel.databinding.PolygonBoxLayoutBinding

@SuppressLint("ClickableViewAccessibility")
class PolygonView : LinearLayoutCompat {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var tempX = 0f
    private var tempY = 0f
    private var isOptionsVisible = true
    private var isClicked = false
    private val polygonBoxLayoutBinding by lazy {
        PolygonBoxLayoutBinding.inflate(
            LayoutInflater.from(
                context
            ), this, true
        )
    }
    private var boxActionListener: BoxActionListener? = null;

    init {
        polygonBoxLayoutBinding.removeBt.setOnClickListener {
            boxActionListener?.onRemovePressed(this@PolygonView)
        }

        polygonBoxLayoutBinding.sizeBt.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempX = motionEvent.x;tempY = motionEvent.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val disX = motionEvent.x - tempX;
                    val disY = motionEvent.y - tempY;
                    val (t, l) = listOf(left, top);

                    val lp = this@PolygonView.layoutParams.apply {
                        this@apply.width += (disX * 0.5).toInt()
                        this@apply.height += (disY * 0.5).toInt()
                    }
                    this@PolygonView.layoutParams = lp
                    this@PolygonView.left = l
                    this@PolygonView.top = t
                }
            }
            true
        }

        polygonBoxLayoutBinding.imagePolygon.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempX = motionEvent.x
                    tempY = motionEvent.y
                    isClicked = true
                }
                MotionEvent.ACTION_MOVE -> {
                    isClicked = false
                    this.showOptions()
                    this@PolygonView.x += motionEvent.x - tempX
                    this@PolygonView.y += motionEvent.y - tempY
                }
                MotionEvent.ACTION_UP -> {
                    if (isClicked) {
                        if (isOptionsVisible) {
                            isOptionsVisible = false
                            hideOptions()
                        } else {
                            isOptionsVisible = true
                            boxActionListener?.onSelected(this)
                            showOptions()
                        }
                    }
                }
            }
            true
        }
    }

    fun setData(imageSourceId: Int, boxActionListener: BoxActionListener) {
        polygonBoxLayoutBinding.apply {
            imagePolygon.setImageResource(imageSourceId)
        }
        this.boxActionListener = boxActionListener
    }

    fun setTintColor(colorResId: Int) {
        val color=ContextCompat.getColor(
            context,
            colorResId
        )
        polygonBoxLayoutBinding.apply {
            imagePolygon.setColorFilter(color)
            labelTv.setTextColor(color)
        }

    }

    fun hideOptions() {
        polygonBoxLayoutBinding.removeBt.visibility = GONE
        polygonBoxLayoutBinding.sizeBt.visibility = GONE
    }

    fun showOptions() {
        polygonBoxLayoutBinding.removeBt.visibility = VISIBLE
        polygonBoxLayoutBinding.sizeBt.visibility = VISIBLE
    }
}


