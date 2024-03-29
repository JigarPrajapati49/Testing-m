package com.frenzin.machinemaintain.network

import com.frenzin.machinemaintain.model.reqModel.Login
import com.frenzin.machinemaintain.model.resModel.*
import com.frenzin.machinemaintain.utills.ApiEndPoint
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET(ApiEndPoint.GET_LOGIN_USER)
    fun getLoginUserList() : Call<UserList?>?

    @GET(ApiEndPoint.GET_MY_TICKETS)
    fun getMyTickets() : Call<GetMyTickets?>?

    @GET(ApiEndPoint.GET_MACHINE_DETAIL)
    fun getMachineDetail(@Query("page") page: Int): Call<GetMachineDetail?>?

    @GET(ApiEndPoint.GET_BOOK_MACHINE)
    fun getMachineBookDetails(@Query("page") page: Int): Call<GetMachineBookDetails?>?

    @GET(ApiEndPoint.MACHINE_DETAIL_BY_NUMBER)
    fun machineDetailByNumber(@Query("internal_serial_number") internalSerialNumber : String) : Call<MachineDetailByNumber?>?

    @GET(ApiEndPoint.GET_MACHINE_DETAIL_BY_QR)
    fun getMachineDetailByQr(@Query("qr_value") qrValue : String) : Call<MachineDetailByQr?>?

    @POST(ApiEndPoint.LOGIN)
    fun getLogin(@Body login: Login): Call<LoginRes?>?

    @Multipart
    @POST(ApiEndPoint.ADD_MACHINE_DETAIL)
    fun addMachineDetail(
        @Part("machine_serial_number") machineSerialNumber: RequestBody,
        @Part("supplier_name") supplierName: RequestBody,
        @Part("supplier_serial_number") supplierSerialNumber: RequestBody,
        @Part("invert_date") invertDate: RequestBody,
        @Part("description") description: RequestBody,
        @Part("machine_name") machineName: RequestBody,
        @Part("machine_model") machineModel: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>? = null
    ): Call<MachineDetail?>?

    @Multipart
    @POST(ApiEndPoint.BOOK_MACHINE)
    fun bookMachine(
        @Part("customer_name") customerName: RequestBody,
        @Part("mobilenumber") mobileNumber: RequestBody,
        @Part("pi_number") piNumber: RequestBody,
        @Part("dif_number") difNumber: RequestBody,
        @Part("tax_invoice") taxInvoice: RequestBody,
        @Part("Lr_number") lrNumber: RequestBody,
        @Part("address") address: RequestBody,
        @Part("salesperson_id") salesPersonId: RequestBody,
        @Part("machine_id") machineId: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>? = null
    ): Call<LoginRes?>?

    @Multipart
    @POST(ApiEndPoint.CHANGE_OR_ADD_TICKET_LOG)
    fun changeOrAddTicketLog(
        @Part("ticket_id") ticketId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("remarks") remarks: RequestBody,
        @Part files : ArrayList<MultipartBody.Part>? = null
    ) : Call<GetMyTickets?>?

    @Multipart
    @POST(ApiEndPoint.DISPATCH_MACHINE)
    fun dispatchMachine(
        @Part("machine_id") machineId: RequestBody,
        @Part("pi_number") piNumber: RequestBody,
        @Part("dif_number") difNumber: RequestBody,
        @Part("eway_bill_number") ewayBillNumber: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>? = null
    ) : Call<DispatchMachine?>?
}