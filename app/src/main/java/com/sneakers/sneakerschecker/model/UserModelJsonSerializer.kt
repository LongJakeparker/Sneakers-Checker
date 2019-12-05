package com.sneakers.sneakerschecker.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class UserModelJsonSerializer: JsonSerializer<UserUpdateModel> {

    override fun serialize(userUpdateModel: UserUpdateModel?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("userIdentity", context?.serialize(userUpdateModel?.userIdentity))
        jsonObject.add("username", context?.serialize(userUpdateModel?.username))
        jsonObject.add("eosName", context?.serialize(userUpdateModel?.eosName))
        jsonObject.add("publicKey", context?.serialize(userUpdateModel?.publicKey))
        jsonObject.add("role", context?.serialize(userUpdateModel?.role))
        jsonObject.add("address", context?.serialize(userUpdateModel?.address))

        return jsonObject
    }
}