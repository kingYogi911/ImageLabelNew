package com.example.imagelabel.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.appcompat.widget.LinearLayoutCompat
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
        polygonBoxLayoutBinding.imagePolygon.setOnClickListener {
            this.showOptions()
        }
        polygonBoxLayoutBinding.sizeBt.setOnTouchListener { _, motionEvent ->
            boxActionListener?.onResizeTouch(
                this@PolygonView,
                motionEvent
            ); true
        }
        Log.e("PolygonView", "Setting Focus Change Listener");
        this.setOnFocusChangeListener { _, b ->
            Log.e("PolygonView", "b=$b");
            polygonBoxLayoutBinding.apply {
                removeBt.visibility = if (b) VISIBLE else GONE
                sizeBt.visibility = if (b) VISIBLE else GONE
            }
        }
        var (x, y) = listOf(0f, 0f)
        polygonBoxLayoutBinding.imagePolygon.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = motionEvent.x
                    y = motionEvent.y
                    showOptions()
                }
                MotionEvent.ACTION_MOVE -> {
                    this@PolygonView.x += motionEvent.x - x
                    this@PolygonView.y += motionEvent.y - y
                }
                MotionEvent.ACTION_UP->{
                    Handler(Looper.getMainLooper())
                        .postDelayed({ hideOptions() }, 5000)
                }
            }
            true
        }
    }

    fun setData(imageSourceId: Int, boxActionListener: BoxActionListener) {
        polygonBoxLayoutBinding.imagePolygon.setImageResource(imageSourceId);
        this.boxActionListener = boxActionListener
    }

    fun hideOptions() {
        polygonBoxLayoutBinding.removeBt.visibility = GONE
        polygonBoxLayoutBinding.sizeBt.visibility = GONE
        //invalidate();
    }

    private fun showOptions() {
        polygonBoxLayoutBinding.removeBt.visibility = VISIBLE
        polygonBoxLayoutBinding.sizeBt.visibility = VISIBLE
    }
}


