package com.example.chatappusingsockets.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappusingsockets.databinding.MessageListBinding
import com.example.chatappusingsockets.extensions.chatImage
import com.example.chatappusingsockets.extensions.isGone
import com.example.chatappusingsockets.extensions.isVisible
import com.example.chatappusingsockets.extensions.loadImage
import com.example.chatappusingsockets.models.response.CommonChatModel
import com.example.chatappusingsockets.utils.Constants
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MessageAdapter (var list:List<CommonChatModel>, private val onClick: CallBack): RecyclerView.Adapter<MessageAdapter.ViewHolder>(){

    inner class ViewHolder( val binding : MessageListBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MessageListBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("TAG", "onBindViewHolder: "+list[position].senderId.toString())
        if (list[position].senderId.toString() == Constants.USER_ID.toString())
        {
            if (list[position].messageType==1)
            {
                holder.binding.myMsgLayout.isVisible()
                holder.binding.otherUserMsgLayout.isGone()
                holder.binding.myMsg.text = list[position].message
                holder.binding.myMsgTime.text = list[position].created?.let { convertTime(it.toLong()) }
                holder.binding.myImageImageLayout.isGone()
                holder.binding.otherUserImageLayout.isGone()
            }
            else
            {
                holder.binding.myMsgLayout.isGone()
                holder.binding.otherUserMsgLayout.isGone()
                holder.binding.myImageTime.text = list[position].created?.let { convertTime(it.toLong()) }
                holder.binding.myImageImageLayout.isVisible()
                holder.binding.otherUserImageLayout.isGone()
                list[position].message?.let { holder.binding.myImage.chatImage(it) }
                holder.binding.myImage.setOnClickListener {
                    onClick.clickItem(list[position])
                }
                Log.d("TAG", "onBindViewHolder: "+list[position].message)
            }
        }
        else
        {
            if (list[position].messageType==1)
            {
                holder.binding.myMsgLayout.isGone()
                holder.binding.otherUserMsgLayout.isVisible()
                holder.binding.otherUserMsg.text = list[position].message
                holder.binding.otherUserMsgMsgTime.text = list[position].created?.let { convertTime(it.toLong()) }
                holder.binding.ivProfile.loadImage(Constants.ConstantVar.OtherUserImage)
                holder.binding.myImageImageLayout.isGone()
                holder.binding.otherUserImageLayout.isGone()
            }
            else
            {
                holder.binding.myMsgLayout.isGone()
                holder.binding.otherUserMsgLayout.isGone()
                holder.binding.otherUserMsg.text = list[position].message
                holder.binding.otherUserMsgImageTime.text = list[position].created?.let { convertTime(it.toLong()) }
                holder.binding.imgMsgProfile.loadImage(Constants.ConstantVar.OtherUserImage)
                holder.binding.myImageImageLayout.isGone()
                holder.binding.otherUserImageLayout.isVisible()
                list[position].message?.let { holder.binding.otherUserImage.chatImage(it) }
                Log.d("TAG", "onBindViewHolder: "+list[position].message)

                holder.binding.otherUserImage.setOnClickListener {
                    onClick.clickItem(list[position])
                }
            }

        }

    }

    override fun getItemCount(): Int
    {
        return if (list.isNotEmpty()) list.size else 0
    }

    // to set Date in custom format
    fun convertTime(timestamp: Long): String? {
        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp * 1000
        val outputFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy | hh:mm aa")
        //outputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return outputFormat.format(cal.getTime())
    }

    interface CallBack
    {
        fun clickItem(data : CommonChatModel)

    }
}