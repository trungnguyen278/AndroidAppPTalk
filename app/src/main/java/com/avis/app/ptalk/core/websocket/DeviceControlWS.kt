package com.avis.app.ptalk.core.websocket

import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.thingai.base.log.ILog
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * WebSocket client for real-time device control.
 * Protocol matches server.py control_request/control_response flow.
 */
class DeviceControlWebSocket {
    private val TAG = "DeviceControlWebSocket"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep connection alive
        .build()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Pending control requests: req_id -> callback
    private val pendingRequests = ConcurrentHashMap<String, (ControlResponse) -> Unit>()

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /**
     * Connect to the server WebSocket
     */
    fun connect(serverUrl: String) {
        ILog.d(TAG, "Attempting to connect to: $serverUrl")

        if (_connectionState.value == ConnectionState.CONNECTED) {
            ILog.d(TAG, "Already connected")
            return
        }

        _connectionState.value = ConnectionState.CONNECTING
        ILog.d(TAG, "State changed to CONNECTING")

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                ILog.d(TAG, "WebSocket connected")
                _connectionState.value = ConnectionState.CONNECTED

                // Send handshake as Android app
                val handshake = JsonObject().apply {
                    addProperty("client_type", "android_app")
                }
                webSocket.send(gson.toJson(handshake))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                ILog.d(TAG, "Received: $text")
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                ILog.e(TAG, "WebSocket failure", t.message)
                _connectionState.value = ConnectionState.DISCONNECTED
                // Notify all pending requests of failure
                pendingRequests.forEach { (_, callback) ->
                    callback(
                        ControlResponse(
                            status = "error",
                            errorCode = "CONNECTION_FAILED",
                            message = t.message ?: "Connection failed"
                        )
                    )
                }
                pendingRequests.clear()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                ILog.d(TAG, "WebSocket closed: $code $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        })
    }

    /**
     * Disconnect from server
     */
    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        pendingRequests.clear()
    }

    /**
     * Send a control command to a device and wait for response
     */
    suspend fun sendControlCommand(
        deviceId: String,
        command: String,
        params: Map<String, Any> = emptyMap(),
        timeoutMs: Long = 10000
    ): ControlResponse {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            return ControlResponse(
                status = "error",
                errorCode = "NOT_CONNECTED",
                message = "WebSocket not connected"
            )
        }

        val reqId = UUID.randomUUID().toString()

        // Build payload (what device receives)
        val payload = JsonObject().apply {
            addProperty("cmd", command)
            params.forEach { (key, value) ->
                when (value) {
                    is String -> addProperty(key, value)
                    is Number -> addProperty(key, value)
                    is Boolean -> addProperty(key, value)
                }
            }
        }

        // Build control request (what server routes)
        val request = JsonObject().apply {
            addProperty("cmd", "control_request")
            addProperty("req_id", reqId)
            addProperty("device_id", deviceId)
            add("payload", payload)
        }

        return withTimeout(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                pendingRequests[reqId] = { response ->
                    if (continuation.isActive) {
                        continuation.resume(response)
                    }
                }

                continuation.invokeOnCancellation {
                    pendingRequests.remove(reqId)
                }

                val sent = webSocket?.send(gson.toJson(request)) ?: false
                if (!sent) {
                    pendingRequests.remove(reqId)
                    if (continuation.isActive) {
                        continuation.resume(
                            ControlResponse(
                                status = "error",
                                errorCode = "SEND_FAILED",
                                message = "Failed to send WebSocket message"
                            )
                        )
                    }
                }

                ILog.d(TAG, "Sent control request: $command to $deviceId")
            }
        }
    }

    private fun handleMessage(text: String) {
        ILog.d(TAG, "handleMessage: $text")
        try {
            val json = gson.fromJson(text, JsonObject::class.java)
            val cmd = json.get("cmd")?.asString
            ILog.d(TAG, "Parsed cmd: $cmd")

            if (cmd == "control_response") {
                val reqId = json.get("req_id")?.asString ?: return
                val status = json.get("status")?.asString ?: "error"
                val errorCode = json.get("error_code")?.asString
                val message = json.get("message")?.asString

                ILog.d(
                    TAG,
                    "control_response: reqId=$reqId, status=$status, errorCode=$errorCode, message=$message"
                )

                // Try to get device_response first, fallback to parsing root object
                val deviceResponse = json.get("device_response")?.asJsonObject
                ILog.d(TAG, "device_response exists: ${deviceResponse != null}")

                val parsedDeviceResponse = if (deviceResponse != null) {
                    ILog.d(TAG, "Parsing device_response: $deviceResponse")
                    parseDeviceResponse(deviceResponse)
                } else if (status == "ok") {
                    // Server returns data at root level (not wrapped in device_response)
                    ILog.d(TAG, "Parsing root object as device response")
                    parseDeviceResponse(json)
                } else null

                ILog.d(TAG, "parsedDeviceResponse: $parsedDeviceResponse")

                val response = ControlResponse(
                    reqId = reqId,
                    status = status,
                    errorCode = errorCode,
                    message = message,
                    deviceResponse = parsedDeviceResponse
                )

                val callback = pendingRequests.remove(reqId)
                ILog.d(TAG, "Callback for reqId=$reqId exists: ${callback != null}")
                callback?.invoke(response)
            }
        } catch (e: Exception) {
            ILog.e(TAG, "Failed to parse message", e.message)
        }
    }

    private fun parseDeviceResponse(json: JsonObject): DeviceStatusResponse? {
        return try {
            DeviceStatusResponse(
                deviceId = json.get("device_id")?.asString,
                status = json.get("status")?.asString,
                // Server uses battery_percent, not battery_level
                batteryLevel = json.get("battery_percent")?.asInt
                    ?: json.get("battery_level")?.asInt,
                volume = json.get("volume")?.asInt,
                brightness = json.get("brightness")?.asInt,
                deviceName = json.get("device_name")?.asString,
                firmwareVersion = json.get("firmware_version")?.asString,
                wifiSsid = json.get("wifi_ssid")?.asString,
                wifiRssi = json.get("wifi_rssi")?.asInt,
                connectivityState = json.get("connectivity_state")?.asString,
                uptimeSec = json.get("uptime_sec")?.asInt
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Response from control command
 */
data class ControlResponse(
    val reqId: String? = null,
    val status: String,  // "ok" or "error"
    val errorCode: String? = null,
    val message: String? = null,
    val deviceResponse: DeviceStatusResponse? = null
) {
    val isSuccess: Boolean get() = status == "ok"
}

/**
 * Device status/response data
 */
data class DeviceStatusResponse(
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName(value = "batteryLevel", alternate = ["battery_level", "battery_percent"]) val batteryLevel: Int? = null,
    @SerializedName("volume") val volume: Int? = null,
    @SerializedName("brightness") val brightness: Int? = null,
    @SerializedName("device_name") val deviceName: String? = null,
    @SerializedName("firmware_version") val firmwareVersion: String? = null,
    @SerializedName("wifi_ssid") val wifiSsid: String? = null,
    @SerializedName("wifi_rssi") val wifiRssi: Int? = null,
    @SerializedName("connectivity_state") val connectivityState: String? = null,
    @SerializedName("uptime_sec") val uptimeSec: Int? = null
)
