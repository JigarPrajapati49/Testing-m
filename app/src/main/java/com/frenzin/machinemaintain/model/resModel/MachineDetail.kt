package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class MachineDetail(
    @SerializedName("data" ) var data : MachineDetailList? = MachineDetailList()
)

data class MachineDetailList (
    @SerializedName("id"                    ) var id                  : Int?    = null,
    @SerializedName("machine_name"          ) var machineName         : String? = null,
    @SerializedName("machine_model"         ) var machineModel        : String? = null,
    @SerializedName("invert_date"           ) var invertDate          : String? = null,
    @SerializedName("supplier_name"         ) var supplierName        : String? = null,
    @SerializedName("description"           ) var description         : String? = null,
    @SerializedName("machine_serial_number" ) var machineSerialNumber : String? = null,
    @SerializedName("status"                ) var status              : String? = null,
    @SerializedName("created_at"            ) var createdAt           : String? = null,
    @SerializedName("updated_at"            ) var updatedAt           : String? = null,
    @SerializedName("purchase_status"       ) var purchaseStatus      : String? = null
)