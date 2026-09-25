package com.marekfx77.sshmobile.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SecureStorage(context: Context) {
    private val gson = Gson()
    private val prefs: SharedPreferences = try {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "ssh_mobile_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for devices with problematic keystore implementations
        context.getSharedPreferences("ssh_mobile_secure_fallback", Context.MODE_PRIVATE)
    }

    private val KEY_NAMES_LIST = "secure_key_names_list"

    fun savePassword(serverId: String, password: String) {
        prefs.edit().putString("pwd_$serverId", password).apply()
    }

    fun getPassword(serverId: String): String? {
        return prefs.getString("pwd_$serverId", null)
    }

    fun deletePassword(serverId: String) {
        prefs.edit().remove("pwd_$serverId").apply()
    }

    fun savePrivateKey(name: String, pemData: String) {
        prefs.edit().putString("key_$name", pemData).apply()
        val currentKeys = getAllPrivateKeyNames().toMutableSet()
        currentKeys.add(name)
        val json = gson.toJson(currentKeys)
        prefs.edit().putString(KEY_NAMES_LIST, json).apply()
    }

    fun getPrivateKey(name: String): String? {
        return prefs.getString("key_$name", null)
    }

    fun deletePrivateKey(name: String) {
        prefs.edit().remove("key_$name").apply()
        val currentKeys = getAllPrivateKeyNames().toMutableSet()
        currentKeys.remove(name)
        val json = gson.toJson(currentKeys)
        prefs.edit().putString(KEY_NAMES_LIST, json).apply()
    }

    fun getAllPrivateKeyNames(): List<String> {
        val json = prefs.getString(KEY_NAMES_LIST, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<Set<String>>() {}.type
            val set: Set<String> = gson.fromJson(json, type) ?: emptySet()
            set.sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString("app_pin_hash", hash).putBoolean("pin_enabled", true).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = prefs.getString("app_pin_hash", null) ?: return false
        return savedHash == hashPin(pin)
    }

    fun isPinEnabled(): Boolean {
        return prefs.getBoolean("pin_enabled", false) && prefs.getString("app_pin_hash", null) != null
    }

    fun disablePin() {
        prefs.edit().putBoolean("pin_enabled", false).remove("app_pin_hash").apply()
    }

    private fun hashPin(pin: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun deleteAll() {
        prefs.edit().clear().apply()
    }
}
