package com.example.chatappusingsockets.models

data class Message(
    val senderName: String,
    val message: String,
    val roomName: String, val index: Any
)