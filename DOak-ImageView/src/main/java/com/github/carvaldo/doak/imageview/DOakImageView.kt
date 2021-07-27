package com.github.carvaldo.doak.imageview

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URL

/**
 * TODO: document your custom view class.
 */
class DOakImageView : View {

    private var _url: URL? = null // TODO: use a default from R.string...
    private val client = OkHttpClient.Builder()
        .cache(
            Cache(
                directory = File(context.cacheDir, "http_cache"),
                // $0.05 worth of phone storage in 2020
                maxSize = 50L * 1024L * 1024L // 50 MiB
            )
        )
        .build()

    /**
     * The text to draw
     */
    var uri: String?
        get() = _url?.toURI()?.path
        set(value) {
            _url = URL(value)
            invalidateImage()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.DOakImageView, defStyle, 0
        )
        if (a.hasValue(R.styleable.DOakImageView_uri)) {
            _url = URL(a.getString(R.styleable.DOakImageView_uri))
        }
        a.recycle()
        // Update TextPaint and text measurements from attributes
        invalidateImage()
    }

    private fun invalidateImage() {
        MainScope().launch(Dispatchers.IO) {
            if (_url != null) {
                val request = Request.Builder()
                    .url(_url!!)
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val stream = response.body?.byteStream()
                    val drawable = BitmapDrawable.createFromStream(stream, "DOakImageView")
                    withContext(Dispatchers.Main) {
                        this@DOakImageView.background = drawable
                    }
                } else {
                    //TODO fail
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}