package com.frenzin.machinemaintain.model.reqModel

data class Login(
    val email : String,
    val password : String,
    val androidVersion : String,
    val deviceModel : String,
    val appVersion : String,
    val fcmToken : String
)
