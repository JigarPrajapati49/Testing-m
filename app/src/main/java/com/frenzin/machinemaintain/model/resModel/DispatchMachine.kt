package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class DispatchMachine (
    @SerializedName("data" ) var data : DispatchMachineDetail? = DispatchMachineDetail()
)

data class DispatchMachineDetail (

    @SerializedName("id"          ) var id         : Int?    = null,
    @SerializedName("machine_id"  ) var machineId  : Int?    = null,
    @SerializedName("dispatch_by" ) var dispatchBy : Int?    = null,
    @SerializedName("created_at"  ) var createdAt  : String? = null,
    @SerializedName("updated_at"  ) var updatedAt  : String? = null

)