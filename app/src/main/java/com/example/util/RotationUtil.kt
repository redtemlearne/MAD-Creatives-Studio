package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun toggleScreenOrientation(activity: Activity?) {
    if (activity == null) return
    val currentOrientation = activity.resources.configuration.orientation
    if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun setScreenOrientationSensor(activity: Activity?) {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
}
