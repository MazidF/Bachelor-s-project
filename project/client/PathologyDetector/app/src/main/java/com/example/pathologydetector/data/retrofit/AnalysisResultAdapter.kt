package com.example.pathologydetector.data.retrofit

import com.example.pathologydetector.model.Model
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object AnalysisResultAdapter : JsonDeserializer<AnalysisResult> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AnalysisResult {
        val jsonObject = json?.asJsonObject ?: return AnalysisResult.NotProcessed
        val model = Model.fromModel(jsonObject["model"].asString)

        return if (jsonObject.has("status")) {
            if (jsonObject["status"].asInt == 1) {
                AnalysisResult.Healthy(model)
            } else {
                AnalysisResult.Pathology(model)
            }
        } else if (jsonObject.has("error")) {
            AnalysisResult.Error(jsonObject["error"].asString, model)
        } else { // Local
            when (jsonObject["tag"]?.asString) {
                "Healthy" -> AnalysisResult.Healthy(model)
                "Pathology" -> AnalysisResult.Pathology(model)
                "Error" -> AnalysisResult.Error(jsonObject["message"].asString, model)
                else -> AnalysisResult.NotProcessed
            }
        }
    }
}