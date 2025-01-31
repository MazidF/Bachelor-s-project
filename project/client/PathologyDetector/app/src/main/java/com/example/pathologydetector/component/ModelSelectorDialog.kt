package com.example.pathologydetector.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pathologydetector.model.Model

@Composable
fun ModelSelectorDialog(
    onDismissRequest: () -> Unit,
    onModelPicked: (Model) -> Unit,
) {
    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = true,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier.background(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
            ).padding(vertical = 10.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Choose a model to process",
                modifier = Modifier.padding(bottom = 10.dp),
            )

            for (model in Model.entries.filter { it != Model.NONE }) {
                Text(
                    text = model.modelName,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(
                            color = model.getColor(),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .clickable {
                            onModelPicked(model)
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}