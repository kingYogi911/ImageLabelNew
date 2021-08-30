package com.example.imagelabel.util

import org.json.JSONObject

interface UploadCallBack {
    fun onSuccess(jsonObject:JSONObject)
    fun onFailure(e: Exception)
}