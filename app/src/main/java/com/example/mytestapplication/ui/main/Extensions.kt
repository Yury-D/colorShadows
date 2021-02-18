package com.example.mytestapplication.ui.main

import android.content.res.Resources
import android.util.TypedValue

val Int.dp: Int
    get() = kotlin.math.ceil(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
    ).toInt()

val Float.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)