package com.example.pathologydetector.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pathologydetector.data.host
import com.example.pathologydetector.model.Model

@Composable
fun HostChangerDialog(
    onDismissRequest: () -> Unit,
) {
    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = true,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        var newHost by remember {
            mutableStateOf(host ?: "192.168.1.")
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = newHost,
            onValueChange = {
                newHost = it
            },
            label = {
                Text("Host")
            },
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        host = newHost.trim()
                        onDismissRequest()
                    }
                ) {
                    Image(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                    )
                }
            }
        )
    }
}