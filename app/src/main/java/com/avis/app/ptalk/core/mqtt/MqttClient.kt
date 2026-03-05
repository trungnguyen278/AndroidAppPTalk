package com.avis.app.ptalk.core.mqtt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import org.thingai.base.log.ILog
import java.util.UUID

class PTalkMqttClient {
    private val TAG = "PTalkMqttClient"
    private val serverUri = "tcp://171.226.10.121:443"
    private var client: MqttClient? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableStateFlow<Pair<String, String>?>(null)
    val messages: StateFlow<Pair<String, String>?> = _messages.asStateFlow()

    fun connect() {
        if (_isConnected.value) return

        try {
            val clientId = "AndroidClient_${UUID.randomUUID()}"
            client = MqttClient(serverUri, clientId, MemoryPersistence())

            val options = MqttConnectionOptions().apply {
                userName = "ptalk"
                password = "ptalk123".toByteArray()
                isAutomaticReconnect = true
                isCleanStart = true
                connectionTimeout = 10
                keepAliveInterval = 60
            }

            client?.setCallback(object : MqttCallback {
                override fun disconnected(disconnectResponse: org.eclipse.paho.mqttv5.client.MqttDisconnectResponse?) {
                    ILog.d(TAG, "MQTT Disconnected")
                    _isConnected.value = false
                }

                override fun mqttErrorOccurred(exception: MqttException?) {
                    ILog.e(TAG, "MQTT Error: \${exception?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (topic != null && message != null) {
                        val payload = String(message.payload)
                        ILog.d(TAG, "Message arrived: $topic -> $payload")
                        _messages.value = Pair(topic, payload)
                    }
                }

                override fun deliveryComplete(token: org.eclipse.paho.mqttv5.client.IMqttToken?) {
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    ILog.d(TAG, "MQTT Connected")
                    _isConnected.value = true
                }
                
                override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {}
            })

            client?.connect(options)
        } catch (e: Exception) {
            ILog.e(TAG, "MQTT connection error: \${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (_isConnected.value) {
                client?.disconnect()
                _isConnected.value = false
            }
        } catch (e: Exception) {
            ILog.e(TAG, "Error disconnecting: \${e.message}")
        }
    }

    fun subscribe(topic: String) {
        try {
            if (_isConnected.value) {
                client?.subscribe(topic, 1)
                ILog.d(TAG, "Subscribed to \$topic")
            }
        } catch (e: Exception) {
            ILog.e(TAG, "Error subscribing: \${e.message}")
        }
    }

    fun unsubscribe(topic: String) {
        try {
            if (_isConnected.value) {
                client?.unsubscribe(topic)
            }
        } catch (e: Exception) {
            ILog.e(TAG, "Error unsubscribing: \${e.message}")
        }
    }

    fun publish(topic: String, message: String) {
        try {
            if (_isConnected.value) {
                val mqttMessage = MqttMessage(message.toByteArray()).apply {
                    qos = 1
                    isRetained = false
                }
                client?.publish(topic, mqttMessage)
                ILog.d(TAG, "Published to $topic: $message")
            } else {
                ILog.w(TAG, "Cannot publish to \$topic: not connected")
            }
        } catch (e: Exception) {
            ILog.e(TAG, "Error publishing: \${e.message}")
        }
    }
}