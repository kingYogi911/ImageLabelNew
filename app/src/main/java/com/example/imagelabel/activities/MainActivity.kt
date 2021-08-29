package com.example.imagelabel.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.imagelabel.R
import com.example.imagelabel.customViews.BoxActionListener
import com.example.imagelabel.customViews.PolygonView
import com.example.imagelabel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val activityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val polygonList by lazy { ArrayList<PolygonView>() }
    private var x: Float = 0f
    private var y: Float = 0f
    private val boxActionListener = object : BoxActionListener {
        override fun onRemovePressed(v: View) {
            activityMainBinding.container.removeView(v)
        }

        override fun onResizeTouch(v: View, event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.x; y = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val disX = event.x - x;
                    val disY = event.y - y;
                    val (t, l) = listOf(v.left, v.top);

                    val lp = v.layoutParams.apply {
                        this.width += (disX * 0.5).toInt()
                        this.height += (disY * 0.5).toInt()
                    }
                    v.layoutParams = lp
                    v.left = l
                    v.top = t
                }
                MotionEvent.ACTION_UP ->
                    Handler(Looper.getMainLooper())
                        .postDelayed({ (v as PolygonView).hideOptions() }, 3000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        activityMainBinding.addPolygon.setOnClickListener {
            polygonList.forEach { v ->
                v.hideOptions()
                Log.e("MainActivity", "hideOptions() called")
            }
            val pv = PolygonView(this)
            pv.layoutParams = ViewGroup.LayoutParams(200, 200)
            pv.x = (activityMainBinding.container.width / 2).toFloat()
            pv.y = (activityMainBinding.container.height / 2).toFloat()
            pv.isClickable = true
            pv.isFocusable = true
            pv.setData(R.drawable.ic_launcher_background, boxActionListener)
            activityMainBinding.container.addView(pv)
        }

    }
}