package com.avis.app.ptalk.core.ble.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.avis.app.ptalk.core.ble.BleClient
import com.avis.app.ptalk.core.ble.BleSession
import com.avis.app.ptalk.core.config.BleUuid
import com.avis.app.ptalk.core.ble.ScannedDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import org.thingai.base.log.ILog
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * PTalk BLE client for device init configuration
 * - Scans for devices exposing the config service (BleUuid.SVC_CONFIG)
 * - Connects and exposes a session for read/write/notifications
 *
 * Requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT runtime permissions (Android 12+).
 */
@SuppressLint("MissingPermission")
class PTalkBleClient(private val app: Context) : BleClient {
    private val TAG: String = "PTalkBleClient"
    private val sessions = ConcurrentHashMap<String, PTalkBleSession>()
    
    // RSSI history for smoothing (keep last 5 readings per device)
    private val rssiHistory = ConcurrentHashMap<String, MutableList<Int>>()
    private val RSSI_HISTORY_SIZE = 5
    
    private fun smoothRssi(address: String, newRssi: Int): Int {
        val history = rssiHistory.getOrPut(address) { mutableListOf() }
        history.add(newRssi)
        // Keep only last N readings
        while (history.size > RSSI_HISTORY_SIZE) {
            history.removeAt(0)
        }
        // Return average
        return history.average().toInt()
    }

    override fun scanForConfigDevices(): Flow<List<ScannedDevice>> {
        val manager = app.getSystemService(BluetoothManager::class.java)
            ?: return callbackFlow { trySend(emptyList()); awaitClose {} }
        val adapter = manager.adapter ?: return callbackFlow { trySend(emptyList()); awaitClose {} }
        val scanner = adapter.bluetoothLeScanner
            ?: return callbackFlow { trySend(emptyList()); awaitClose {} }

        return callbackFlow {
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleUuid.SVC_CONFIG))
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            val devices = ConcurrentHashMap<String, ScannedDevice>()

            val cb = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val d = result.device
                    // Smooth RSSI to reduce fluctuation
                    val smoothedRssi = smoothRssi(d.address, result.rssi)
                    val item = ScannedDevice(
                        address = d.address,
                        name = result.scanRecord?.deviceName ?: d.name ?: d.address,
                        rssi = smoothedRssi,
                        hasConfigService = true
                    )
                    devices[d.address] = item

                    ILog.d(TAG, "scanForConfigDevices", "${item.hasConfigService}")

                    trySend(devices.values.sortedBy { it.name ?: it.address })
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
                }

                override fun onScanFailed(errorCode: Int) {
                    ILog.d(TAG, "onScanFailed", "$errorCode")
                    trySend(emptyList())
                }
            }

            scanner.startScan(listOf(filter), settings, cb)
            awaitClose { scanner.stopScan(cb) }
        }
            .onStart { emit(emptyList()) }
            .flowOn(Dispatchers.IO)
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(address: String): BleSession {
        // Reuse session if exists
        val existing = sessions[address]
        if (existing != null) {
            ILog.d(TAG, "reuse session", address)
            if (existing.isConnected.value) {
                existing.close()
            }
            existing.connect()
            return existing
        }

        ILog.d(TAG, "new session", address)
        val manager = app.getSystemService(BluetoothManager::class.java)
            ?: error("BluetoothManager not available")
        val device = manager.adapter?.getRemoteDevice(address)
            ?: error("Device $address not found")
        val session = PTalkBleSession(app, device)
        sessions[address] = session
        // Initiate connection
        session.connect()
        return session
    }

    override suspend fun disconnect(address: String) {
        ILog.d(TAG, "disconnect", address)
        sessions.remove(address)?.close()
    }
}

/**
 * BLE session for a single device connection.
 * Handles GATT operations with suspend APIs and notification streams.
 */
private class PTalkBleSession(
    private val app: Context,
    private val device: BluetoothDevice
) : BleSession, BluetoothGattCallback() {
    private val TAG = "PTalkBleSession"

    private var gatt: BluetoothGatt? = null
    private val _connected = MutableStateFlow(false)
    override val isConnected = _connected

    override val address: String get() = device.address

    private val opMutex = Any()
    private var pendingRead: CompletableDeferred<ByteArray>? = null
    private var pendingWrite: CompletableDeferred<Unit>? = null
    private var servicesReady = CompletableDeferred<Unit>()

    private val notifyChannel = Channel<ByteArray>(Channel.UNLIMITED)

    @SuppressLint("MissingPermission")
    suspend fun connect() {
        ILog.d(TAG, "connect")
        gatt = device.connectGatt(app, false, this, BluetoothDevice.TRANSPORT_LE)
        // Wait until services discovered before allowing ops
        // Optional timeout to avoid deadlocks
        withTimeout(5_000) {
            ILog.d(TAG, "wait timeout")
            servicesReady.await()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun close() {
        ILog.d(TAG, "close")
        try {
            gatt?.disconnect()
        } catch (e: Exception) {
            ILog.e(TAG, e.message)
        } finally {
            gatt?.close()
            gatt = null
            _connected.value = false
            pendingRead?.cancel()
            pendingWrite?.cancel()
            notifyChannel.close()
        }
    }

    // Reads a characteristic as ByteArray
    @SuppressLint("MissingPermission")
    override suspend fun read(uuid: UUID): ByteArray {
        val ch = findCharacteristic(uuid) ?: error("Characteristic $uuid not found")
        val g = gatt ?: error("Not connected")
        val result = CompletableDeferred<ByteArray>()
        synchronized(opMutex) {
            pendingRead = result
            if (!g.readCharacteristic(ch)) {
                pendingRead = null
                error("readCharacteristic returned false for $uuid")
            }
        }
        return withTimeout(10_000) { result.await() }
    }

    // Writes a characteristic with or without response
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override suspend fun write(uuid: UUID, value: ByteArray, withResponse: Boolean): ByteArray {
        val ch = findCharacteristic(uuid) ?: error("Characteristic $uuid not found")
        val g = gatt ?: error("Not connected")
        ch.writeType = if (withResponse)
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        else
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        val result = CompletableDeferred<Unit>()
        synchronized(opMutex) {
            pendingWrite = result
            if (g.writeCharacteristic(ch, value, ch.writeType) != BluetoothStatusCodes.SUCCESS) {
                pendingWrite = null
                error("writeCharacteristic returned false for $uuid")
            }
        }
        withTimeout(10_000) { result.await() }
        return value
    }

    // Notification flow for a characteristic
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun notifications(uuid: UUID): Flow<ByteArray> {
        val ch = findCharacteristic(uuid) ?: error("Characteristic $uuid not found")
        val g = gatt ?: error("Not connected")
        // Enable notifications
        g.setCharacteristicNotification(ch, true)
        // Write CCCD to enable
        val cccd = ch.getDescriptor(CCCD_UUID)
        if (cccd != null) {
            g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            g.setCharacteristicNotification(ch, false)
        }
        return notifyChannel.receiveAsFlow()
            .flowOn(Dispatchers.IO)
    }

    // BluetoothGattCallback implementations

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        ILog.d(TAG, "onConnectionStateChange", "$newState")
        _connected.value = (newState == BluetoothProfile.STATE_CONNECTED)
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices()
        } else {
            servicesReady.completeExceptionally(IllegalStateException("Disconnected"))
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            servicesReady.complete(Unit)
        } else {
            servicesReady.completeExceptionally(IllegalStateException("Service discovery failed: $status"))
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        pendingRead?.complete(value)
        pendingRead = null
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        pendingWrite?.complete(Unit)
        pendingWrite = null
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        notifyChannel.trySend(value)
    }

    private fun getConfigService(): BluetoothGattService? {
        return gatt?.getService(BleUuid.SVC_CONFIG)
    }

    private fun findCharacteristic(uuid: UUID): BluetoothGattCharacteristic? {
        return getConfigService()?.getCharacteristic(uuid)
    }

    companion object {
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}