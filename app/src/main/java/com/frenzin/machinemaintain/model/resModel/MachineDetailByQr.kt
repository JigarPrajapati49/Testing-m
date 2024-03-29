package com.frenzin.machinemaintain.model.resModel

import com.google.gson.annotations.SerializedName

data class MachineDetailByQr(

    @SerializedName("is_booked"         ) var isBooked          : Boolean?           = null,
    @SerializedName("bookmachinedetail" ) var bookmachinedetail : BookMachineDetail? = BookMachineDetail(),
    @SerializedName("bookimage"         ) var bookimage         : Bookimage?            = Bookimage(),
    @SerializedName("machine_detail"    ) var machineDetail     : MachineDetailsByQr?     = MachineDetailsByQr(),
    @SerializedName("machine_image"     ) var machineImage      : ArrayList<String>  = arrayListOf()
)

data class BookMachineDetail (

    @SerializedName("id"               ) var id             : Int?    = null,
    @SerializedName("customer_name"    ) var customerName   : String? = null,
    @SerializedName("mobilenumber"     ) var mobilenumber   : String? = null,
    @SerializedName("tax_invoice"      ) var taxInvoice     : String? = null,
    @SerializedName("Lr_number"        ) var LrNumber       : String? = null,
    @SerializedName("address"          ) var address        : String? = null,
    @SerializedName("salesperson_id"   ) var salespersonId  : Int?    = null,
    @SerializedName("machine_id"       ) var machineId      : Int?    = null,
    @SerializedName("created_by"       ) var createdBy      : Int?    = null,
    @SerializedName("created_at"       ) var createdAt      : String? = null,
    @SerializedName("updated_at"       ) var updatedAt      : String? = null,
    @SerializedName("pi_number"        ) var piNumber       : String? = null,
    @SerializedName("dif_number"       ) var difNumber      : String? = null,
    @SerializedName("eway_bill_number" ) var ewayBillNumber : String? = null

)

data class Bookimage (

    @SerializedName("id"         ) var id        : Int?    = null,
    @SerializedName("book_id"    ) var bookId    : Int?    = null,
    @SerializedName("image"      ) var image     : String? = null,
    @SerializedName("created_at" ) var createdAt : String? = null,
    @SerializedName("updated_at" ) var updatedAt : String? = null
)

data class MachineDetailsByQr (

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