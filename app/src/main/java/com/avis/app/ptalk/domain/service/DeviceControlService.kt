package com.avis.app.ptalk.domain.service

import com.avis.app.ptalk.core.mqtt.PTalkMqttClient
import com.avis.app.ptalk.core.websocket.ControlResponse
import com.avis.app.ptalk.core.websocket.DeviceStatusResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.thingai.base.log.ILog

/**
 * Service for managing real-time device control via MQTT.
 */
class DeviceControlService(
    private val mqttClient: PTalkMqttClient
) {
    private val TAG = "DeviceControlService"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val _deviceStatus = MutableStateFlow<DeviceStatusResponse?>(null)
    val deviceStatus: StateFlow<DeviceStatusResponse?> = _deviceStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    val connectionState = mqttClient.isConnected

    private var currentDeviceId: String? = null

    init {
        scope.launch {
            mqttClient.messages.collect { pair ->
                pair?.let { (topic, payload) ->
                    currentDeviceId?.let { deviceId ->
                        val statusTopic = "devices/${deviceId}/status"
                        if (topic == statusTopic) {
                            try {
                                val status = gson.fromJson(payload, DeviceStatusResponse::class.java)
                                _deviceStatus.value = status
                            } catch (e: Exception) {
                                ILog.e(TAG, "Failed to parse status payload: \$payload")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Connect to MQTT and subscribe to the device's status topic
     */
    fun connect(deviceId: String) {
        ILog.d(TAG, "connect() called with deviceId: ${deviceId}")
        currentDeviceId = deviceId
        mqttClient.connect()

        // Wait for actual connection before subscribing & requesting status
        scope.launch {
            mqttClient.isConnected.collect { connected ->
                if (connected) {
                    ILog.d(TAG, "MQTT connected, now subscribing to devices/${deviceId}/status")
                    mqttClient.subscribe("devices/${deviceId}/status")
                    refreshStatus()
                    return@collect
                }
            }
        }

        ILog.d(TAG, "MQTT connect() initiated")
    }

    /**
     * Disconnect from MQTT
     */
    fun disconnect() {
        currentDeviceId?.let { deviceId ->
            mqttClient.unsubscribe("devices/${deviceId}/status")
        }
        mqttClient.disconnect()
        currentDeviceId = null
        _deviceStatus.value = null
    }

    private fun publishCommand(action: String, value: Any? = null): Boolean {
        val deviceId = currentDeviceId ?: return false
        val commandTopic = "devices/${deviceId}/cmd"
        
        val commandMap = mutableMapOf<String, Any>("cmd" to action)
        if (value != null) {
            when (action) {
                "set_volume" -> commandMap["volume"] = value
                "set_brightness" -> commandMap["brightness"] = value
                "set_device_name" -> commandMap["device_name"] = value
                else -> commandMap["value"] = value
            }
        }
        
        return try {
            val json = gson.toJson(commandMap)
            mqttClient.publish(commandTopic, json)
            true
        } catch (e: Exception) {
            _lastError.value = "Command failed: \${e.message}"
            false
        }
    }

    /**
     * Request device status update
     */
    fun refreshStatus() {
        publishCommand("request_status")
    }

    /**
     * Set device volume (0-100)
     */
    fun setVolume(volume: Int): Boolean {
        _isLoading.value = true
        _lastError.value = null
        val success = publishCommand("set_volume", volume)
        if (success) {
            _deviceStatus.value = _deviceStatus.value?.copy(volume = volume)
        }
        _isLoading.value = false
        return success
    }

    /**
     * Set device brightness (0-100)
     */
    fun setBrightness(brightness: Int): Boolean {
        _isLoading.value = true
        _lastError.value = null
        val success = publishCommand("set_brightness", brightness)
        if (success) {
            _deviceStatus.value = _deviceStatus.value?.copy(brightness = brightness)
        }
        _isLoading.value = false
        return success
    }

    /**
     * Set device name via MQTT command
     */
    fun setDeviceName(name: String): Boolean {
        _isLoading.value = true
        _lastError.value = null
        val success = publishCommand("set_device_name", name)
        if (success) {
            _deviceStatus.value = _deviceStatus.value?.copy(deviceName = name)
        }
        _isLoading.value = false
        return success
    }

    /**
     * Reboot device
     */
    fun rebootDevice(): ControlResponse {
        val success = publishCommand("reboot")
        return if (success) {
            ControlResponse(status = "success", message = "Reboot command sent")
        } else {
            ControlResponse(status = "error", message = "Failed to send command")
        }
    }

    /**
     * Request BLE config mode
     */
    fun requestBleConfig(): ControlResponse {
        val success = publishCommand("request_ble_config")
        return if (success) {
            ControlResponse(status = "success", message = "BLE config command sent")
        } else {
            ControlResponse(status = "error", message = "Failed to send command")
        }
    }

    /**
     * Request OTA update
     */
    fun requestOta(version: String? = null): ControlResponse {
        val success = publishCommand("ota_update", version)
        return if (success) {
            ControlResponse(status = "success", message = "OTA command sent")
        } else {
            ControlResponse(status = "error", message = "Failed to send command")
        }
    }

    fun getBatteryLevel(): Int? {
        return _deviceStatus.value?.batteryLevel
    }

    fun clearError() {
        _lastError.value = null
    }
}