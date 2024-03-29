package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class GetMyTickets(
    @SerializedName("data" ) var data : ArrayList<TicketsDetail> = arrayListOf()
)

data class TicketsDetail (
    @SerializedName("id"              ) var id             : Int?            = null,
    @SerializedName("ticket_number"   ) var ticketNumber   : Int?            = null,
    @SerializedName("customer_id"     ) var customerId     : Int?            = null,
    @SerializedName("machine_id"      ) var machineId      : Int?            = null,
    @SerializedName("description"     ) var description    : String?         = null,
    @SerializedName("status"          ) var status         : String?         = null,
    @SerializedName("created_at"      ) var createdAt      : String?         = null,
    @SerializedName("updated_at"      ) var updatedAt      : String?         = null,
    @SerializedName("user_id"         ) var userId         : Int?            = null,
    @SerializedName("assigned_by"     ) var assignedBy     : Int?            = null,
    @SerializedName("customer_detail" ) var customerDetail : CustomerDetail? = CustomerDetail(),
    @SerializedName("machine_detail"  ) var machineDetail  : TicketMachineDetail?  = TicketMachineDetail(),
    @SerializedName("logs"            ) var logs           : ArrayList<Logs> = arrayListOf()
)

data class CustomerDetail (
    @SerializedName("id"           ) var id          : Int?    = null,
    @SerializedName("name"         ) var name        : String? = null,
    @SerializedName("company_name" ) var companyName : String? = null,
    @SerializedName("phone_number" ) var phoneNumber : String? = null,
    @SerializedName("countrycode"  ) var countrycode : String? = null,
    @SerializedName("address"      ) var address     : String? = null,
    @SerializedName("lat"          ) var lat         : String? = null,
    @SerializedName("long"         ) var long        : String? = null,
    @SerializedName("created_at"   ) var createdAt   : String? = null,
    @SerializedName("updated_at"   ) var updatedAt   : String? = null
)

data class TicketMachineDetail (
    @SerializedName("id"                     ) var id                   : Int?    = null,
    @SerializedName("machine_name"           ) var machineName          : String? = null,
    @SerializedName("machine_model"          ) var machineModel         : String? = null,
    @SerializedName("invert_date"            ) var invertDate           : String? = null,
    @SerializedName("supplier_name"          ) var supplierName         : String? = null,
    @SerializedName("description"            ) var description          : String? = null,
    @SerializedName("machine_serial_number"  ) var machineSerialNumber  : String? = null,
    @SerializedName("status"                 ) var status               : String? = null,
    @SerializedName("created_at"             ) var createdAt            : String? = null,
    @SerializedName("updated_at"             ) var updatedAt            : String? = null,
    @SerializedName("purchase_status"        ) var purchaseStatus       : String? = null,
    @SerializedName("supplier_serial_number" ) var supplierSerialNumber : String? = null,
    @SerializedName("internal_serial_number" ) var internalSerialNumber : String? = null
)

data class Logs (
    @SerializedName("id"         ) var id        : Int?              = null,
    @SerializedName("ticket_id"  ) var ticketId  : Int?              = null,
    @SerializedName("remarks"    ) var remarks   : String?           = null,
    @SerializedName("status"     ) var status    : String?           = null,
    @SerializedName("created_at" ) var createdAt : String?           = null,
    @SerializedName("updated_at" ) var updatedAt : String?           = null,
//    @SerializedName("files"      ) var files     : ArrayList<String> = arrayListOf()
)
