package com.example.imagelabel.customViews

import android.view.MotionEvent
import android.view.View

interface BoxActionListener {
    fun onRemovePressed(v:View);
    fun onSelected(v:View)
}