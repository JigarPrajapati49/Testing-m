package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class GetMachineDetail(
    @SerializedName("data"          ) var data        : Data?   = Data(),
    @SerializedName("next_page_url" ) var nextPageUrl : String? = null,
    @SerializedName("prev_page_url" ) var prevPageUrl : String? = null
)

data class Data (

    @SerializedName("current_page"   ) var currentPage  : Int?             = null,
    @SerializedName("data"           ) var data         : ArrayList<GetMachineDetailList>  = arrayListOf(),
    @SerializedName("first_page_url" ) var firstPageUrl : String?          = null,
    @SerializedName("from"           ) var from         : Int?             = null,
    @SerializedName("last_page"      ) var lastPage     : Int?             = null,
    @SerializedName("last_page_url"  ) var lastPageUrl  : String?          = null,
    @SerializedName("next_page_url"  ) var nextPageUrl  : String?          = null,
    @SerializedName("path"           ) var path         : String?          = null,
    @SerializedName("per_page"       ) var perPage      : Int?             = null,
    @SerializedName("prev_page_url"  ) var prevPageUrl  : String?          = null,
    @SerializedName("to"             ) var to           : Int?             = null,
    @SerializedName("total"          ) var total        : Int?             = null

)

data class GetMachineDetailList(
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
