package com.example.smsgateway.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayCredentialsSheet(
    sheetState: SheetState,
    isVisible: Boolean,
    supabaseUrl: String,
    supabaseKey: String,
    deviceId: String,
    deviceSecret: String,
    deviceLabel: String,
    onSave: (url: String, key: String, id: String, secret: String, label: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var url by remember { mutableStateOf(supabaseUrl) }
    var key by remember { mutableStateOf(supabaseKey) }
    var id by remember { mutableStateOf(deviceId) }
    var secret by remember { mutableStateOf(deviceSecret) }
    var label by remember { mutableStateOf(deviceLabel) }
    var showKey by remember { mutableStateOf(false) }
    var showSecret by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Edit Gateway Credentials",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Supabase URL *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            TextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Supabase Anon Key *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            imageVector = if (showKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showKey) "Hide key" else "Show key"
                        )
                    }
                }
            )

            TextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("Device ID *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            TextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text("Device Secret *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showSecret = !showSecret }) {
                        Icon(
                            imageVector = if (showSecret) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showSecret) "Hide secret" else "Show secret"
                        )
                    }
                }
            )

            TextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Device Label (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    onSave(url, key, id, secret, label)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = url.isNotBlank() && key.isNotBlank() && id.isNotBlank() && secret.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoMoIdentitySheet(
    sheetState: SheetState,
    isVisible: Boolean,
    momoNumber: String,
    momoCode: String,
    onSave: (number: String, code: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var number by remember { mutableStateOf(momoNumber) }
    var code by remember { mutableStateOf(momoCode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Edit MoMo Identity",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("MoMo Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                placeholder = { Text("e.g., +256701234567") }
            )

            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("MoMo Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                singleLine = true,
                placeholder = { Text("Optional") }
            )

            Button(
                onClick = {
                    onSave(number, code)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
