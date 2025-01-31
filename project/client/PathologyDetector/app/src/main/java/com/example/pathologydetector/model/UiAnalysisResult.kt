package com.example.pathologydetector.model

import androidx.compose.ui.graphics.Color
import com.example.pathologydetector.R

enum class Model(val modelName: String) {
    ONE("Model1"), TWO("Model2"), THREE("Model3"), NONE("Choose a model to process");

    fun getColor(): Color = when (this) {
        ONE -> Color(0xFF673AB7)
        TWO -> Color(0xFFE91E63)
        THREE -> Color(0xFFFF5722)
        NONE -> Color(0xFF8B858D)
    }

    companion object {
        fun fromModel(model: String) = entries.firstOrNull {
            it.modelName.equals(model, ignoreCase = true)
                    || it.name.equals(model, ignoreCase = true)
        } ?: NONE
    }
}

sealed class UiAnalysisResult(val color: Color, val stateMessage: Pair<String, Int>?) {
    abstract val model: Model

    data object NotProcessed : UiAnalysisResult(Color(0xFFC1C1C1), null) {
        override val model = Model.NONE
    }
    data class Processing(override val model: Model) : UiAnalysisResult(Color(0xFFC1C1C1), null)
    data class Healthy(override val model: Model) : UiAnalysisResult(Color(0xFF3B8E06), "Healthy" to R.drawable.icon_medication)
    data class Pathology(override val model: Model) : UiAnalysisResult(Color(0xFFE41010), "Pathology" to R.drawable.icon_medication)
    data class Error(val message: String, override val model: Model) : UiAnalysisResult(Color(0xC1000000), message to R.drawable.icon_warning)
}