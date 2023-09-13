package com.example.chatappusingsockets.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.lifecycleScope
import com.example.chatappusingsockets.R
import com.example.chatappusingsockets.adapters.MessageAdapter
import com.example.chatappusingsockets.base.BaseApplication
import com.example.chatappusingsockets.databinding.FragmentChatScreenBinding
import com.example.chatappusingsockets.extensions.chatImage
import com.example.chatappusingsockets.extensions.createRequestBody
import com.example.chatappusingsockets.extensions.isGone
import com.example.chatappusingsockets.extensions.isVisible
import com.example.chatappusingsockets.models.response.BlockUserResponseModel
import com.example.chatappusingsockets.models.response.CommonChatModel
import com.example.chatappusingsockets.models.response.CommonSocketResponseModel
import com.example.chatappusingsockets.models.response.MessageListenerResponse
import com.example.chatappusingsockets.models.response.ReportResponseModel
import com.example.chatappusingsockets.socket.SocketManager
import com.example.chatappusingsockets.utils.Constants
import com.google.gson.GsonBuilder
import com.project.blackgirluser.util.imagepickerhelper.ImagePickerUtility
import com.project.blackgirluser.util.imagepickerhelper.ImagePickerUtilityFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.microedition.khronos.egl.EGLDisplay


class ChatFragment : ImagePickerUtilityFragment<FragmentChatScreenBinding>(),SocketManager.Observer, MessageAdapter.CallBack {
    private var socketManager: SocketManager =
        BaseApplication.context!!.getSocketMySocketManager()!!
    private lateinit var messageAdapter: MessageAdapter
    private var name = ""
    private var receiverId = ""
    private var status = 0
    private var myPopUpWindow: PopupWindow? = null

    //progress bar
    //    lateinit var progressHUD: ProgressHUD

    //viewmodel initialization
//    private val viewModel by viewModels<AppViewModel>()
    val list by lazy { ArrayList<CommonChatModel>() }


    override fun getViewBinding(): FragmentChatScreenBinding {
        return FragmentChatScreenBinding.inflate(layoutInflater)
    }

    //func- select image from gallery/camera and upload in upload image API -> the response from this API is pushed in tht message param as API was designed to upload image through intermeditary API response only
    //otherwise can upload directly
    override fun selectedImage(imagePath: String?, code: Int) {
        val map: java.util.HashMap<String, RequestBody> = java.util.HashMap()
//        map["type"] = createRequestBody("image")
//        map["folder"] = createRequestBody("user")
//
//        val images: MultipartBody.Part?
//        images = prepareMultiPart("image", File(imagePath))
//        viewModel.uploadImageApi(map,images).observe(requireActivity(),this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //receiverId , name - id and name of person I am chatting with for now set constant
        receiverId = Constants.USER_ID.toString()
//        receiverId = arguments?.getString("otherUserId").toString()
        name = "Receiver"
//        name = arguments?.getString("otherUserName").toString()

//        binding.title.text = name

//        savePreference(AppConstant.ConstantVar.IN_CHAT,"1")
//        progressHUD = ProgressHUD(requireContext())
//        progressHUD.show()
//        if (AppConstant.ConstantVar.notificationType == "1")
//        {
//            progressHUD.dismiss()
//        }
    }

    /************************** onResume **************************/
    override fun onResume() {
        super.onResume()
        BaseApplication.mSocketManager?.onRegister(this)
        if (!socketManager.isConnected() || socketManager.getMySocket() == null) {
            socketManager.init()
        }
        callMsgListSocket()
        sendMessage()
        getBlockStatus()
        onClick()
        binding.rvChatMessages.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.rvChatMessages.scrollToPosition(list.size - 1)
        }

    }

    /************************** getBlockStatus **************************/
    private fun getBlockStatus() {
        val jsonObject = JSONObject()
        jsonObject.put("user_id", Constants.USER_ID)
        jsonObject.put("user2_id", receiverId.toInt())
        socketManager.blockStatus(jsonObject)
    }

    /************************** sendMessage **************************/
    private fun sendMessage() {
        binding.sendMsg.setOnClickListener {
            if (binding.msg.text.isNotEmpty()) {
                val jsonObject1 = JSONObject()
                jsonObject1.put("senderId", Constants.USER_ID)
                jsonObject1.put("receiverId", receiverId.toInt())
                jsonObject1.put("message", binding.msg.text.toString().trim())
                jsonObject1.put("messageType", "1")
                jsonObject1.put("extension", "")
                socketManager.sendMessage(jsonObject1)
                callMsgListSocket()
                binding.msg.text = null

            }
        }
    }

    /************************** callMsgListSocket **************************/
    private fun callMsgListSocket() {
        socketManager.init()
        val jsonObject = JSONObject()
        Log.d("TAG", "callMsgListSocket:  in")
        jsonObject.put("senderId", Constants.USER_ID)
        jsonObject.put("receiverId", receiverId?.toInt())
        socketManager.connectSingleChatSocket()
        socketManager.getChat(jsonObject)

    }

    /************************** onClick **************************/
    private fun onClick() {
        binding.menu.setOnClickListener {
            myPopUpWindow?.showAsDropDown(it, -153, 0)
        }

        binding.attachment.setOnClickListener {
             getImage(requireActivity(),1,false)
        }
//        binding.actionBar.back.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
    }

    @SuppressLint("MissingInflatedId")
    private fun setPopUpWindow(check: Int) {

        try {
            val inflater: LayoutInflater = requireActivity().applicationContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater

            val view = inflater.inflate(R.layout.lay_pop_up_messages, null)
            val block: TextView = view.findViewById(R.id.block)
            val report: TextView = view.findViewById(R.id.report)
            val clearChat: TextView = view.findViewById(R.id.clearChat)
            Log.d("TAG", "setPopUpWindow: ${this.status}")
            if (check == 0) {
                block.text = "block"
                status = 1
            } else {
                block.text = "un_block"
                status = 0
            }
            block.setOnClickListener {

                showBlockOrClearChatDialog(0)
                myPopUpWindow?.dismiss()

            }

            clearChat.setOnClickListener {

                showBlockOrClearChatDialog(1)

                myPopUpWindow?.dismiss()

            }

            report.setOnClickListener {

                reportDialog()
                myPopUpWindow?.dismiss()
            }

            myPopUpWindow = PopupWindow(
                view,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                true
            )
        } catch (e: Exception) {
        }
    }

    private fun reportDialog() {
        val dialog = Dialog(requireContext())
        val views = LayoutInflater.from(requireContext()).inflate(R.layout.report_dialog, null)
        dialog.setContentView(views)
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val yes = views.findViewById<TextView>(R.id.yes)
        val no = views.findViewById<TextView>(R.id.no)
        val feedback = views.findViewById<EditText>(R.id.feedback)
        yes.setOnClickListener {
            val jsonObject = JSONObject()
            jsonObject.put("user_id", Constants.USER_ID)
            jsonObject.put("user2_id", receiverId.toInt())
            jsonObject.put("comments", feedback.text.toString())
            socketManager.reportUser(jsonObject)
            dialog.dismiss()
        }
        no.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBlockOrClearChatDialog(i: Int) {
        val dialog = Dialog(requireContext())
        val views = LayoutInflater.from(requireContext()).inflate(R.layout.block_clear_chat_dialog, null)
        dialog.setContentView(views)
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val message = views.findViewById<TextView>(R.id.message)
        val yes = views.findViewById<TextView>(R.id.yes)
        val no = views.findViewById<TextView>(R.id.no)
        if (i == 0) {
            message.text = "are_you_sure_you_want_to_block"
        } else {
            message.text = "are_you_sure_you_want_to_clear_chat"
        }
        yes.setOnClickListener {
            val jsonObject = JSONObject()
            if (i == 1) {
                jsonObject.put("senderId", Constants.USER_ID)
                jsonObject.put("receiverId", receiverId.toInt())
                socketManager.deleteChat(jsonObject)
                dialog.dismiss()
            } else {
                jsonObject.put("user_id", Constants.USER_ID)
                jsonObject.put("user2_id", receiverId.toInt())
                jsonObject.put("status", status)
                socketManager.blockUser(jsonObject)
                dialog.dismiss()
            }
        }
        no.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /************************** SocketOverrideFunction  **************************/
    override fun onResponseArray(event: String, args: JSONArray) {
        when (event) {
            SocketManager.GET_CHAT_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    // progressHUD.dismiss()
                    val gson = GsonBuilder().create()
                    val getChatList = gson.fromJson(
                        args.toString(),
                        Array<CommonChatModel>::class.java
                    ).toList()
                    if (getChatList.isNotEmpty()) {
                        binding.noDataFound.isGone()
                        list.clear()
                        list.addAll(getChatList)
                        messageAdapter = MessageAdapter(list, this@ChatFragment)
                        binding.rvChatMessages.adapter = messageAdapter
                        binding.rvChatMessages.scrollToPosition(list.size - 1)
                        messageAdapter.notifyDataSetChanged()
                    } else {
                        binding.noDataFound.isVisible()
                        Log.d("", "onResponseArray = : " + getChatList.size)
                    }
                }
            }
        }
    }

    override fun onResponse(event: String, args: JSONObject) {

        when (event) {
            SocketManager.SEND_MESSAGE_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    val mObject = args as JSONObject
                    val gson = GsonBuilder().create()
                    val listChatHistory =
                        gson.fromJson(mObject.toString(), MessageListenerResponse::class.java)
                    val model = CommonChatModel(
                        listChatHistory.chatConstantId,
                        listChatHistory.created,
                        listChatHistory.createdAt,
                        listChatHistory.deletedId,
                        listChatHistory.id,
                        listChatHistory.message,
                        listChatHistory.messageType,
                        listChatHistory.readStatus,
                        listChatHistory.receiverId,
                        listChatHistory.receiverImage,
                        listChatHistory.receiverName,
                        null,
                        listChatHistory.senderId,
                        listChatHistory.senderImage,
                        listChatHistory.senderName,
                        null,
                        listChatHistory.updated,
                        listChatHistory.updatedAt)
                    list.add(model)
                    try {
                        messageAdapter = MessageAdapter(list, this@ChatFragment)
                        binding.rvChatMessages.adapter = messageAdapter
                        binding.rvChatMessages.scrollToPosition(list.size - 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            SocketManager.BLOCK_STATUS_LISTENER -> {

                lifecycleScope.launch(Dispatchers.Main)
                {
                    val gson = GsonBuilder().create()
                    val response =
                        gson.fromJson(args.toString(), BlockUserResponseModel::class.java)
                    var check = 0
                    if (response.blockData.blockedByMe > 0) {
                        status = 0
                        check = 1
                        binding.blockMsgLayout.isVisible()
                        binding.msgLayout.isGone()
                    } else {
                        status = 1
                        check = 0
                        try {
                            binding.blockMsgLayout.isGone()
                            binding.msgLayout.isVisible()
                        } catch (e: Exception) {
                        }
                        /*  binding.blockMsgLayout.isGone()
                          binding.msgLayout.isVisible()*/
                    }
                    Log.d("TAG", "onResponse: status = $status")
                    setPopUpWindow(check)
                }
            }

            SocketManager.CLEAR_CHAT_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main)
                {
                    val gson = GsonBuilder().create()
                    val response =
                        gson.fromJson(args.toString(), CommonSocketResponseModel::class.java)
                    showMessageAlert(requireContext(), response.success_message)
                    requireActivity().onBackPressed()
                }
            }

            SocketManager.BLOCK_USER_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main)
                {
                    val gson = GsonBuilder().create()
                    val response =
                        gson.fromJson(args.toString(), BlockUserResponseModel::class.java)
                    status = response.blockData.blockedByMe
                    if (response.blockData.blockedByMe > 0) {
                        showMessageAlert(requireActivity(), "You blocked this user")
                        binding.blockMsgLayout.isVisible()
                        binding.msgLayout.isGone()
                    } else {
                        showMessageAlert(requireActivity(), "You unblock this user")
                        binding.blockMsgLayout.isGone()
                        binding.msgLayout.isVisible()
                    }
                    setPopUpWindow(status)
                }
            }

            SocketManager.REPORT_USER_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main)
                {
                    val gson = GsonBuilder().create()
                    val response = gson.fromJson(args.toString(), ReportResponseModel::class.java)
                    showMessageAlert(requireActivity(), response.msg)
                }
            }
        }
    }

    private fun showMessageAlert(context: Context, successMessage: String) {
        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
    }

    override fun onError(event: String, vararg args: Array<*>) {

    }


    companion object {
        var userName = ""
    }

//    override fun selectedImage(imagePath: String?, code: Int)
//    {
//
//        val map: java.util.HashMap<String, RequestBody> = java.util.HashMap()
//        map["type"] = createRequestBody("image")
//        map["folder"] = createRequestBody("user")
//
//        val images: MultipartBody.Part?
//        images = prepareMultiPart("image", File(imagePath))
//        viewModel.uploadImageApi(map,images).observe(requireActivity(),this)
//    }

//    override fun onChanged(t: Resource<FileUploadResponse>?) {
//
//        when (t?.status) {
//            Status.SUCCESS -> {
//                Log.d("TAG", "onChanged: == "+"in")
//
//                val data =t.data!!.body
//                val imageUrl = data[0].image
//                Log.d("TAG", "onChanged: $imageUrl")
//                val jsonObject1 = JSONObject()
//                jsonObject1.put("senderId", getPreference(AppConstant.ConstantVar.USER_ID, "").toInt())
//                jsonObject1.put("receiverId", receiverId.toInt())
//                jsonObject1.put("message",ApiConstants.IMAGE_BASE_URL+ imageUrl)
//                jsonObject1.put("messageType", "2")
//                jsonObject1.put("extension", "")
//                socketManager.sendMessage(jsonObject1)
//                callMsgListSocket()
//            }
//            Status.ERROR -> {
//
//                showErrorAlert(requireActivity(), t.message!!)
//            }
//            Status.LOADING -> {
//
//            }
//            else -> {
//                showErrorAlert(requireActivity(), t?.message!!)
//            }
//        }
//    }

    override fun clickItem(data: CommonChatModel) {
        val dialog = Dialog(requireView().context)
        val views =
            LayoutInflater.from(requireView().context).inflate(R.layout.open_image, null)
        dialog.setContentView(views)
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setGravity(Gravity.CENTER)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val image = views.findViewById<ImageView>(R.id.image)
        val close = views.findViewById<ImageView>(R.id.close)
        image.chatImage(data.message.toString())
        dialog.show()
        close.setOnClickListener {
            dialog.dismiss()
        }
    }

}