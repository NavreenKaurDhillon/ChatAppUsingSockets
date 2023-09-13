package com.example.chatappusingsockets.base

import android.app.Application
import com.example.chatappusingsockets.socket.SocketManager

class BaseApplication : Application() {
    companion object{
        var context : BaseApplication?=null
        var mSocketManager: SocketManager? = null

    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
    fun getSocketMySocketManager(): SocketManager? {
        mSocketManager = if (mSocketManager == null) {
            SocketManager()
        } else {
            return mSocketManager
        }
        return mSocketManager
    }

}