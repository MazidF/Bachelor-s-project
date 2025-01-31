package com.example.pathologydetector.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.pathologydetector.R

private val nunitoFont = FontFamily(
    Font(R.font.nunito_extra_light, weight = FontWeight.ExtraLight, style = FontStyle.Normal),
    Font(R.font.nunito_light, weight = FontWeight.Light, style = FontStyle.Normal),
    Font(R.font.nunito_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(R.font.nunito_medium, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(R.font.nunito_semi_bold, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(R.font.nunito_bold, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(R.font.nunito_extra_bold, weight = FontWeight.ExtraBold, style = FontStyle.Normal),
    Font(R.font.nunito_black, weight = FontWeight.Black, style = FontStyle.Normal),

    Font(R.font.nunito_extra_light_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(R.font.nunito_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.nunito_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.nunito_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.nunito_semi_bold_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.nunito_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.nunito_extra_bold_italic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(R.font.nunito_black_italic, weight = FontWeight.Black, style = FontStyle.Italic),
)

// Set of Material typography styles to start with
val Typography = Typography().run {
    copy(
        displayLarge.copy(fontFamily = nunitoFont),
        displayMedium.copy(fontFamily = nunitoFont),
        displaySmall.copy(fontFamily = nunitoFont),
        headlineLarge.copy(fontFamily = nunitoFont),
        headlineMedium.copy(fontFamily = nunitoFont),
        headlineSmall.copy(fontFamily = nunitoFont),
        titleLarge.copy(fontFamily = nunitoFont),
        titleMedium.copy(fontFamily = nunitoFont),
        titleSmall.copy(fontFamily = nunitoFont),
        bodyLarge.copy(fontFamily = nunitoFont),
        bodyMedium.copy(fontFamily = nunitoFont),
        bodySmall.copy(fontFamily = nunitoFont),
        labelLarge.copy(fontFamily = nunitoFont),
        labelMedium.copy(fontFamily = nunitoFont),
        labelSmall.copy(fontFamily = nunitoFont),
    )
}
