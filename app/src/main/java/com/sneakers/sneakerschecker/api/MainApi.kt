package com.sneakers.sneakerschecker.api

import com.sneakers.sneakerschecker.model.SignIn
import com.sneakers.sneakerschecker.model.SignUp
import com.sneakers.sneakerschecker.model.ValidateModel
import retrofit2.Call
import retrofit2.http.*

interface MainApi {
    @GET("sneaker/{sneakerId}")
    fun validateSneaker(@Path("sneakerId") sneakerId: String): Call<ValidateModel>
}