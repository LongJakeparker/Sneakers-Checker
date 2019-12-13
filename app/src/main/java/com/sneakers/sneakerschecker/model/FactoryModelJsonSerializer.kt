package com.sneakers.sneakerschecker.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class FactoryModelJsonSerializer: JsonSerializer<FactoryContractModel> {

    override fun serialize(factoryModel: FactoryContractModel?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("userIdentity", context?.serialize(factoryModel?.userIdentity))
        jsonObject.add("username", context?.serialize(factoryModel?.username))
        jsonObject.add("eosName", context?.serialize(factoryModel?.eosName))
        jsonObject.add("publicKey", context?.serialize(factoryModel?.publicKey))
        jsonObject.add("role", context?.serialize(factoryModel?.role))
        jsonObject.add("address", context?.serialize(factoryModel?.address))
        jsonObject.add("brand", context?.serialize(factoryModel?.brand))

        return jsonObject
    }
}