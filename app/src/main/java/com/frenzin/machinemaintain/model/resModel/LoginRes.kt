package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class LoginRes (
    @SerializedName("token" ) var token : String? = null
)