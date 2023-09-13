package com.example.chatappusingsockets.models.response

data class MessageListenerResponse (   val chatConstantId: Int,
val created: Int,
val createdAt: String,
val deletedId: Int,
val id: Int,
val message: String,
val messageType: Int,
val readStatus: Int,
val receiverId: Int,
val receiverImage: String,
val receiverName: String,
val senderId: Int,
val senderImage: String,
val senderName: String,
val updated: Int,
val updatedAt: String)