package com.example.pathologydetector.data.retrofit

import androidx.annotation.Keep
import com.example.pathologydetector.model.Model

@Keep
sealed class AnalysisResult(val tag: String, open val model: Model) {
    data object NotProcessed : AnalysisResult("NotProcessed", Model.NONE)
    class Healthy(model: Model) : AnalysisResult("Healthy", model)
    class Pathology(model: Model) : AnalysisResult("Pathology", model)
    class Error(val message: String, model: Model) : AnalysisResult("Error", model)
}
