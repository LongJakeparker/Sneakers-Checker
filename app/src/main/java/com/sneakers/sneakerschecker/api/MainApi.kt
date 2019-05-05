package com.sneakers.sneakerschecker.api

import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.model.ValidateModel
import retrofit2.Call
import retrofit2.http.*

interface MainApi {
    @GET("sneaker/{sneakerId}")
    fun validateSneaker(@Path("sneakerId") sneakerId: String): Call<ValidateModel>

    @GET("user/ownership/{address}")
    fun getCollection(@Header("Authorization") token:String,
                      @Path("address") userAddress: String): Call<ArrayList<SneakerModel>>
}