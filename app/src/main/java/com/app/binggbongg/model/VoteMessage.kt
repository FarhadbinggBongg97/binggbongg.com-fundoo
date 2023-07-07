package com.app.binggbongg.model

data class VoteMessage(
    val _id: String,
    val created_at: String,
    val updated_at: String,
    val vote_message: String,
    val vote_number: String
)