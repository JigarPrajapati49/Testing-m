package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class MachineDetailByNumber(
    @SerializedName("data" ) var data : MachineDetailsByNumber? = MachineDetailsByNumber()
)

data class MachineDetailsByNumber (

    @SerializedName("id"                     ) var id                   : Int?                    = null,
    @SerializedName("machine_name"           ) var machineName          : String?                 = null,
    @SerializedName("machine_model"          ) var machineModel         : String?                 = null,
    @SerializedName("invert_date"            ) var invertDate           : String?                 = null,
    @SerializedName("supplier_name"          ) var supplierName         : String?                 = null,
    @SerializedName("description"            ) var description          : String?                 = null,
    @SerializedName("machine_serial_number"  ) var machineSerialNumber  : String?                 = null,
    @SerializedName("status"                 ) var status               : String?                 = null,
    @SerializedName("created_at"             ) var createdAt            : String?                 = null,
    @SerializedName("updated_at"             ) var updatedAt            : String?                 = null,
    @SerializedName("purchase_status"        ) var purchaseStatus       : String?                 = null,
    @SerializedName("supplier_serial_number" ) var supplierSerialNumber : String?                 = null,
    @SerializedName("internal_serial_number" ) var internalSerialNumber : String?                 = null,
    @SerializedName("is_booked"              ) var isBooked             : String?                 = null,
    @SerializedName("machineimage"           ) var machineimage         : ArrayList<Machineimage> = arrayListOf()

)

data class Machineimage (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("machine_id" ) var machineId : Int?    = null,
    @SerializedName("image"      ) var image     : String? = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null

)