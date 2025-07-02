package com.netbillvpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.netbillvpn.ui.theme.NetbillVpnTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val VPN_REQUEST_CODE = 1001

    private var isConnected by mutableStateOf(false)
    private var currentPayload by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetbillVpnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    ConfigManagerScreen()
                }
            }
        }
    }

    private fun prepareVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, VpnServiceManager::class.java)
        intent.putExtra("payload", currentPayload)
        startService(intent)
        isConnected = true
        Toast.makeText(this, "VPN Connected", Toast.LENGTH_SHORT).show()
    }

    private fun stopVpnService() {
        val intent = Intent(this, VpnServiceManager::class.java)
        stopService(intent)
        isConnected = false
        Toast.makeText(this, "VPN Disconnected", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(this, "VPN Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun ConfigManagerScreen() {
        val context = LocalContext.current
        val configManager = remember { ConfigManager(context) }

        var payload by remember { mutableStateOf(TextFieldValue("")) }
        var creatorNote by remember { mutableStateOf(TextFieldValue("")) }
        var expiryDate by remember { mutableStateOf("") }
        var fileName by remember { mutableStateOf(TextFieldValue("")) }
        var savedConfigs by remember { mutableStateOf(configManager.listConfigs()) }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "NETBILL VPN Config Manager", style = MaterialTheme.typography.h6)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("Config File Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = payload,
                onValueChange = { payload = it },
                label = { Text("Payload") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = creatorNote,
                onValueChange = { creatorNote = it },
                label = { Text("Creator Note") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        if (fileName.text.isBlank()) {
                            Toast.makeText(context, "Please enter a file name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val expiryTimestamp = try {
                            if (expiryDate.isNotBlank()) {
                                dateFormat.parse(expiryDate)?.time
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                        val config = VpnConfig(
                            payload = payload.text,
                            expiryDate = expiryTimestamp,
                            creatorNote = creatorNote.text
                        )
                        val success = configManager.saveConfig(config, fileName.text)
                        if (success) {
                            Toast.makeText(context, "Config saved", Toast.LENGTH_SHORT).show()
                            savedConfigs = configManager.listConfigs()
                        } else {
                            Toast.makeText(context, "Failed to save config", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Config")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (fileName.text.isBlank()) {
                            Toast.makeText(context, "Please enter a file name to load", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val config = configManager.loadConfig(fileName.text)
                        if (config != null) {
                            payload = TextFieldValue(config.payload)
                            creatorNote = TextFieldValue(config.creatorNote ?: "")
                            expiryDate = config.expiryDate?.let { dateFormat.format(Date(it)) } ?: ""
                            Toast.makeText(context, "Config loaded", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Config not found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = "Load")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Config")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = {
                        if (!isConnected) {
                            currentPayload = payload.text
                            (context as MainActivity).prepareVpn()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Connect")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (isConnected) {
                            (context as MainActivity).stopVpnService()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Disconnect")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Saved Configs:", style = MaterialTheme.typography.subtitle1)
            for (configName in savedConfigs) {
                Text(text = configName)
            }
        }
    }
}
