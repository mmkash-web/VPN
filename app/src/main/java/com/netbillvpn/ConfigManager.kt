package com.netbillvpn

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import org.json.JSONObject

data class VpnConfig(
    val payload: String,
    val expiryDate: Long?, // Unix timestamp in milliseconds, nullable
    val creatorNote: String?
)

class ConfigManager(private val context: Context) {

    private val configDirName = "netbillvpn_configs"

    init {
        val dir = File(context.filesDir, configDirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    fun saveConfig(config: VpnConfig, fileName: String): Boolean {
        return try {
            val json = JSONObject()
            json.put("payload", config.payload)
            json.put("expiryDate", config.expiryDate)
            json.put("creatorNote", config.creatorNote)

            val file = File(context.filesDir, "$configDirName/$fileName.json")
            FileOutputStream(file).use { it.write(json.toString().toByteArray()) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadConfig(fileName: String): VpnConfig? {
        return try {
            val file = File(context.filesDir, "$configDirName/$fileName.json")
            if (!file.exists()) return null

            val jsonStr = FileInputStream(file).bufferedReader().use { it.readText() }
            val json = JSONObject(jsonStr)

            val payload = json.getString("payload")
            val expiryDate = if (json.has("expiryDate") && !json.isNull("expiryDate")) json.getLong("expiryDate") else null
            val creatorNote = if (json.has("creatorNote") && !json.isNull("creatorNote")) json.getString("creatorNote") else null

            VpnConfig(payload, expiryDate, creatorNote)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isConfigExpired(config: VpnConfig): Boolean {
        val now = System.currentTimeMillis()
        return config.expiryDate?.let { it < now } ?: false
    }

    fun listConfigs(): List<String> {
        val dir = File(context.filesDir, configDirName)
        return dir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }
}
