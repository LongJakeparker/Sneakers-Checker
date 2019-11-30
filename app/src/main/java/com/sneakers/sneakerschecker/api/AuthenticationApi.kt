package com.sneakers.sneakerschecker.api

import com.sneakers.sneakerschecker.model.CheckPrivateKeyResultModel
import com.sneakers.sneakerschecker.model.SignIn
import com.sneakers.sneakerschecker.model.SignUp
import retrofit2.Call
import retrofit2.http.*

interface AuthenticationApi {
    @POST("/user/")
    fun signUpApi(@Body param: Map<String, String>): Call<SignUp>

    @FormUrlEncoded
    @POST("/user/signin/")
    fun signInApi(@Header("Authorization") token:String,
                  @Field("grant_type") grant_type: String,
                  @Field("username") userIdentity: String,
                  @Field("password") password: String): Call<SignIn>

    @FormUrlEncoded
    @POST("/oauth/token")
    fun refreshTokenApi(@Header("Authorization") token:String,
                        @Field("grant_type") grant_type: String,
                        @Field("refresh_token") refresh_token: String): Call<SignIn>

    @PATCH("/user/restoration/{networkAddress}")
    fun restoration(@Path("networkAddress") userAddress: String,
                    @Body param: Map<String, String>): Call<CheckPrivateKeyResultModel>
}