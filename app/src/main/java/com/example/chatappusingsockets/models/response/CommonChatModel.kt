package com.example.chatappusingsockets.models.response

data class CommonChatModel (
    val chatConstantId: Int?= null,
    val created: Int?= null,
    val createdAt: String?= null,
    val deletedId: Int?= null,
    val id: Int?= null,
    val message: String?= null,
    val messageType: Int?= null,
    val readStatus: Int?= null,
    val receiverId: Int?= null,
    val receiverImage: String?= null,
    val receiverName: String?= null,
    val receiverRole: Int?= null,
    val senderId: Int?= null,
    val senderImage: String?= null,
    val senderName: String?= null,
    val senderRole: Int?= null,
    val updated: Int?= null,
    val updatedAt: String? = null
)