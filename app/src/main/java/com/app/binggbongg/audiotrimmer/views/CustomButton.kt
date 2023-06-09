/*
 * *
 *  * Created by Youssef Assad on 6/2/18 11:22 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 6/2/18 11:04 AM
 *
 */

package com.ringtone.maker.Views

import android.content.Context
import android.graphics.Typeface

import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.app.binggbongg.R


/**
 * Created by Joseph27 on 11/28/17.
 */

class CustomButton : AppCompatButton {



    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MyTextView)
            val fontName = a.getString(R.styleable.MyTextView_fontName)
            if (fontName != null) {
                val myTypeface = Typeface.createFromAsset(context.assets, "fonts/" + fontName)
                typeface = myTypeface
            }
            a.recycle()
        }
    }

}