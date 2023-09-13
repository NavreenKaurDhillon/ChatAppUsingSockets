package com.example.chatappusingsockets.extensions

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.chatappusingsockets.R
import com.example.chatappusingsockets.utils.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun View.isVisible(){
    this.visibility = View.VISIBLE
}fun View.isGone(){
    this.visibility = View.GONE
}
fun ImageView.chatImage(path: Any) {

    Glide.with(this)
        .load(path)
        .placeholder(R.drawable.placeholder_image)
        .error(R.drawable.placeholder_image)
        .into(this)

}
fun ImageView.loadImage(path: Any) {

    Glide.with(this)
        .load(Constants.IMAGE_BASE_URL+path)
        .placeholder(R.drawable.placeholder_image)
        .error(R.drawable.placeholder_image)
        .into(this)

}
fun createRequestBody(param: String): RequestBody {
    return param.toRequestBody("text/plain".toMediaTypeOrNull())
}


