package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class UserList (
    @SerializedName("data" ) var data : ArrayList<UserListData> = arrayListOf()
)

data class UserListData(
    @SerializedName("id"                ) var id              : Int?    = null,
    @SerializedName("name"              ) var name            : String? = null,
    @SerializedName("email"             ) var email           : String? = null,
    @SerializedName("email_verified_at" ) var emailVerifiedAt : String? = null,
    @SerializedName("created_at"        ) var createdAt       : String? = null,
    @SerializedName("updated_at"        ) var updatedAt       : String? = null
)