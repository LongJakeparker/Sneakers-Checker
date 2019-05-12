package com.sneakers.sneakerschecker.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class SneakerModelJsonSerializer: JsonSerializer<SneakerModel> {

    override fun serialize(sneaker: SneakerModel?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("id", context?.serialize(sneaker?.id))
        jsonObject.add("brand", context?.serialize(sneaker?.brand))
        jsonObject.add("model", context?.serialize(sneaker?.model))
        jsonObject.add("colorway", context?.serialize(sneaker?.colorway))
        jsonObject.add("limitedEdition", context?.serialize(sneaker?.limitedEdition))
        jsonObject.add("releaseDate", context?.serialize(sneaker?.releaseDate))
        jsonObject.add("size", context?.serialize(sneaker?.size))
        jsonObject.add("condition", context?.serialize(sneaker?.condition))
        jsonObject.add("ownerAddress", context?.serialize(sneaker?.ownerAddress))

        return jsonObject
    }
}