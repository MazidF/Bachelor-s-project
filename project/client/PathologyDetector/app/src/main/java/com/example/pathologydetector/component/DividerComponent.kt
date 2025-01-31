package com.example.pathologydetector.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DividerComponent(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.fillMaxWidth().padding(start = 60.dp, end = 14.dp))
}