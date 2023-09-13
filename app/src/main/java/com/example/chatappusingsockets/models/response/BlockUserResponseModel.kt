package com.example.chatappusingsockets.models.response

data class BlockUserResponseModel (
    val blockData: BlockData
) {
    data class BlockData(
        val blockedByMe: Int,
        val blockedByOther: Int
    )
}