package com.example.chatappusingsockets.socket

import android.content.ContentValues.TAG
import android.util.Log
import com.example.chatappusingsockets.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager {

    private val errorMessage = "error_message"

    companion object {

        /************************** Emitters **************************/
        private const val CONNECT_USER = "connect_user"
        const val CHAT_LISTING = "chat_listing"
        const val SEND_MESSAGE = "send_message"
        private const val GET_CHAT = "get_message"
        const val CLEAR_CHAT = "delete_chat"
        const val BLOCK_USER = "block_unblock"
        const val BLOCK_STATUS = "block_status"
        const val TRACKING_USER = "tracking_user"
        const val START_TRACKING = "arival_status"
        const val REPORT_USER = "report_user"

        /************************** Listeners **************************/
        private const val CONNECT_LISTENER = "connect_listener"
        const val GET_CHAT_LISTENER = "get_data_message"
        const val CHAT_MESSAGE = "chat_message"
        const val SEND_MESSAGE_LISTENER = "new_message"
        const val CLEAR_CHAT_LISTENER = "cleared_chat"
        const val BLOCK_USER_LISTENER = "block_unblock"
        const val BLOCK_STATUS_LISTENER = "block_status"
        const val TRACKING_USER_LISTENER = "tracking_user"
        const val START_TRACKING_LISTENER = "arival_status"
        const val REPORT_USER_LISTENER = "report_user"
    }

    private var mSocket: Socket? = null
    private var observerList: MutableList<Observer>? = null

    fun getMySocket(): Socket? {
        return mSocket
    }

    // use for get SOCKET_URL
    private fun getSocket(): Socket? {
        run {
            try {
                mSocket = IO.socket(Constants.SOCKET_URL)
            } catch (e: URISyntaxException) {
                Log.d(TAG, "getSocket: "+e.localizedMessage.toString())
                throw RuntimeException(e)
            }
        }
        return mSocket
    }


    fun onRegister(observer: Observer) {
        if (observerList != null && !observerList!!.contains(observer)) {
            observerList!!.clear()
            observerList!!.add(observer)
        } else {
            observerList = ArrayList()
            observerList!!.clear()
            observerList!!.add(observer)
        }
    }

    fun unRegister(observer: Observer) {
        observerList?.let { list ->
            for (i in 0 until list.size - 1) {
                val model = list[i]
                if (model === observer) {
                    observerList?.remove(model)
                }
            }
        }
    }

    fun init() {
        initializeSocket()
    }

    private fun initializeSocket() {
        if (mSocket == null) {
            mSocket = getSocket()
        }
        if (observerList == null || observerList!!.size == 0) {
            observerList = ArrayList()
        }

        disconnect()
        mSocket!!.connect()
        mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket!!.on(errorMessage, onErrorMessage)
        mSocket!!.on(SEND_MESSAGE_LISTENER, sendMessageListener)
        mSocket!!.on(GET_CHAT_LISTENER, getChatListListener)
        mSocket!!.on(BLOCK_USER_LISTENER, blockUserListener)
        mSocket!!.on(BLOCK_STATUS_LISTENER, blockStatusListener)
        mSocket!!.on(CLEAR_CHAT_LISTENER, deleteChatListener)
        mSocket!!.on(START_TRACKING_LISTENER,startTrackingListener)
        mSocket!!.on(REPORT_USER_LISTENER,reportUserListener)
    }



    private fun disconnect() {
        if (mSocket != null) {

            mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
            mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket!!.off(SEND_MESSAGE_LISTENER, sendMessageListener)
            mSocket!!.off(GET_CHAT_LISTENER, getChatListListener)
            mSocket!!.off(BLOCK_USER_LISTENER, blockUserListener)
            mSocket!!.off(BLOCK_STATUS_LISTENER, blockStatusListener)
            mSocket!!.off(CLEAR_CHAT_LISTENER, deleteChatListener)
            mSocket!!.off(START_TRACKING_LISTENER,startTrackingListener)
            mSocket!!.off(REPORT_USER_LISTENER,reportUserListener)
        }
    }

    fun disconnectAll() {
        try {
            if (mSocket != null)
            {
                mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
                mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
                mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
                mSocket!!.off()
                mSocket!!.disconnect()
            }
        } catch (e: Exception)
        {
        }
    }

    private val onConnect = Emitter.Listener {
        if (isConnected()) {
            try {
                val jsonObject = JSONObject()
                    val userid = Constants.USER_ID
                jsonObject.put("userId", userid)
                mSocket!!.off(CONNECT_LISTENER, onConnectListener)
                mSocket!!.on(CONNECT_LISTENER, onConnectListener)
                mSocket!!.emit(CONNECT_USER, jsonObject)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            initializeSocket()
        }
    }

    fun isConnected(): Boolean
    {
        return mSocket != null && mSocket!!.connected()
    }

    private val onConnectListener = Emitter.Listener { args ->
        try {

            Log.d("Socket", "Connected" + args[0].toString())
            Log.d("Socket", "Connected" + args[0].toString())

            // val data = args[1] as JSONObject
            // val data = args[1] as JSONObject
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    private val onDisconnect = Emitter.Listener { args ->
        try {
            Log.d("Socket", "DISCONNECTED :::$args")
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        try {
            Log.d("Socket", "CONNECTION ERROR :::$args")
        } catch (ex: Exception) {
            ex.localizedMessage
        }
    }

    private val onErrorMessage = Emitter.Listener { args ->
        for (observer in observerList!!) {
            try {
                val data = args[0] as JSONObject
                observer.onError(CONNECT_USER, args)
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

    interface Observer
    {
        fun onResponseArray(event: String, args: JSONArray)

        fun onResponse(event: String, args: JSONObject)
        fun onError(event: String, vararg args: Array<*>)
    }



    //*************************GetChatList************************

    fun getChatList(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(CHAT_MESSAGE)
                    mSocket!!.on(CHAT_MESSAGE, getChatMessage)
                    mSocket!!.emit(CHAT_LISTING, jsonObject)
                } else {
                    mSocket!!.off(CHAT_MESSAGE)
                    mSocket!!.on(CHAT_MESSAGE, getChatMessage)
                    mSocket!!.emit(CHAT_LISTING, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

            Log.e("Socket", "Socket getChatList")

        }
    }

    //*************************GetChatMessage************************

    private val getChatMessage = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONArray
            Log.e("TAG", "GetChatListListener: $data")
            for (observer in observerList!!)
            {
                observer.onResponseArray(CHAT_MESSAGE, data)
            }
        }
        catch (ex: Exception)
        {
            ex.localizedMessage
        }

    }

    //func - called to referesh the screen so that the latest sent message becomes visible, doesn't emit the message to socket
    fun connectSingleChatSocket() {

        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(SEND_MESSAGE_LISTENER)
            mSocket!!.on(SEND_MESSAGE_LISTENER, sendMessageListener)
        }
        else
        {
            mSocket!!.connect()
            mSocket!!.off(SEND_MESSAGE_LISTENER)
            mSocket!!.on(SEND_MESSAGE_LISTENER, sendMessageListener)
        }

        Log.e("Socket", "Socket Connected")
    }

    /************************* SEND_MESSAGE_LISTENER ************************/
    private val sendMessageListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject
            for (observer in observerList!!)
            {
                observer.onResponse(SEND_MESSAGE_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }
    /************************* SEND_MESSAGE ************************/
    //func - send message in the form of json passing required params in it
    fun sendMessage(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(SEND_MESSAGE_LISTENER)
                    mSocket!!.on(SEND_MESSAGE_LISTENER, sendMessageListener)
                    mSocket!!.emit(SEND_MESSAGE, jsonObject)
                } else {
                    mSocket!!.off(SEND_MESSAGE_LISTENER)
                    mSocket!!.on(SEND_MESSAGE_LISTENER, sendMessageListener)
                    mSocket!!.emit(SEND_MESSAGE, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }


    /************************* GET_CHAT ************************/

    //func - check connectivity, and get socket chat
    fun getChat(jsonObject: JSONObject?) {
        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(GET_CHAT_LISTENER)
                    mSocket!!.on(GET_CHAT_LISTENER, getChatListListener)
                    mSocket!!.emit(GET_CHAT, jsonObject)
                } else {
                    mSocket!!.off(GET_CHAT_LISTENER)
                    mSocket!!.on(GET_CHAT_LISTENER, getChatListListener)
                    mSocket!!.emit(GET_CHAT, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }

        }
    }

    /************************* GetChatListListener ************************/

    private val getChatListListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONArray

            for (observer in observerList!!)
            {
                observer.onResponseArray(GET_CHAT_LISTENER, data)
            }
        }
        catch (ex: Exception)
        {
            ex.localizedMessage
        }

    }

    /************************* CLEAR_CHAT_LISTENER ************************/

    private val deleteChatListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject

            for (observer in observerList!!)
            {
                observer.onResponse(CLEAR_CHAT_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }

    /************************* CLEAR_CHAT ************************/
    //func - delete the chat in socket with current user
    fun deleteChat(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(CLEAR_CHAT_LISTENER)
                    mSocket!!.on(CLEAR_CHAT_LISTENER, deleteChatListener)
                    mSocket!!.emit(CLEAR_CHAT, jsonObject)
                } else {
                    mSocket!!.off(CLEAR_CHAT_LISTENER)
                    mSocket!!.on(CLEAR_CHAT_LISTENER, deleteChatListener)
                    mSocket!!.emit(CLEAR_CHAT, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

    /************************* BLOCK_USER_LISTENER ************************/
    private val blockUserListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject

            for (observer in observerList!!)
            {
                observer.onResponse(BLOCK_USER_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }

    /************************* BLOCK_USER ************************/
    //func - block current user
    fun blockUser(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(BLOCK_USER_LISTENER)
                    mSocket!!.on(BLOCK_USER_LISTENER, blockUserListener)
                    mSocket!!.emit(BLOCK_USER, jsonObject)
                } else {
                    mSocket!!.off(BLOCK_USER_LISTENER)
                    mSocket!!.on(BLOCK_USER_LISTENER, blockUserListener)
                    mSocket!!.emit(BLOCK_USER, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

    /************************* REPORT_LISTENER ************************/
    private val reportUserListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject

            for (observer in observerList!!)
            {
                observer.onResponse(REPORT_USER_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }

    /************************* BLOCK_USER ************************/
    //func - report the current receiver user
    fun reportUser(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(REPORT_USER_LISTENER)
                    mSocket!!.on(REPORT_USER_LISTENER, reportUserListener)
                    mSocket!!.emit(REPORT_USER, jsonObject)
                } else {
                    mSocket!!.off(REPORT_USER_LISTENER)
                    mSocket!!.on(REPORT_USER_LISTENER, reportUserListener)
                    mSocket!!.emit(REPORT_USER, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

 /************************* BLOCK_STATUS_LISTENER ************************/
    private val blockStatusListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject

            for (observer in observerList!!)
            {
                observer.onResponse(BLOCK_STATUS_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }

    /************************* BLOCK_STATUS ************************/
    //func - update blocked status by passing the sender and receiver id in param
    fun blockStatus(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(BLOCK_STATUS_LISTENER)
                    mSocket!!.on(BLOCK_STATUS_LISTENER, blockStatusListener)
                    mSocket!!.emit(BLOCK_STATUS, jsonObject)
                } else {
                    mSocket!!.off(BLOCK_STATUS_LISTENER)
                    mSocket!!.on(BLOCK_STATUS_LISTENER, blockStatusListener)
                    mSocket!!.emit(BLOCK_STATUS, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }
    fun trackingUser() {

        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(TRACKING_USER_LISTENER)
            mSocket!!.on(TRACKING_USER_LISTENER, trackingUserListener)
        }
        else
        {
            mSocket!!.connect()
            mSocket!!.off(TRACKING_USER_LISTENER)
            mSocket!!.on(TRACKING_USER_LISTENER, trackingUserListener)
        }

        Log.e("Socket", "Socket Connected")
    }


    /************************* TRACKING_USER_LISTENER ************************/

    private val trackingUserListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject

            for (observer in observerList!!)
            {
                observer.onResponse(TRACKING_USER_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }

    /************************* TRACKING_USER ************************/

    fun tracking_user(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.emit(TRACKING_USER, jsonObject)
                } else
                {
                    mSocket!!.emit(TRACKING_USER, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

    /************************* START_TRACKING_LISTENER ************************/
    private val startTrackingListener = Emitter.Listener { args ->
        try
        {
            val data = args[0] as JSONObject
            for (observer in observerList!!)
            {
                observer.onResponse(START_TRACKING_LISTENER, data)
            }
        } catch (ex: Exception)
        {
            ex.localizedMessage
        }
    }
    /************************* START_TRACKING ************************/
    fun startTracking(jsonObject: JSONObject?) {

        if (jsonObject != null) {
            try {
                if (!mSocket!!.connected()) {
                    mSocket!!.connect()
                    mSocket!!.off(START_TRACKING_LISTENER)
                    mSocket!!.on(START_TRACKING_LISTENER, startTrackingListener)
                    mSocket!!.emit(START_TRACKING, jsonObject)
                } else {
                    mSocket!!.off(START_TRACKING_LISTENER)
                    mSocket!!.on(START_TRACKING_LISTENER, startTrackingListener)
                    mSocket!!.emit(START_TRACKING, jsonObject)
                }
            } catch (ex: Exception) {
                ex.localizedMessage
            }
        }
    }

}