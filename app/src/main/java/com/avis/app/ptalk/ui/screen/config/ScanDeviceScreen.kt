package com.avis.app.ptalk.ui.screen.config

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avis.app.ptalk.LocalAppColors
import com.avis.app.ptalk.R
import com.avis.app.ptalk.core.ble.ScannedDevice
import com.avis.app.ptalk.ui.component.dialog.ErrorDialog
import com.avis.app.ptalk.ui.component.dialog.LoadingDialog
import com.avis.app.ptalk.ui.component.dialog.SuccessDialog
import com.avis.app.ptalk.ui.theme.AppColors
import com.avis.app.ptalk.ui.theme.TechColors
import kotlin.random.Random
import com.avis.app.ptalk.ui.viewmodel.VMConfigDevice
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

/**
 * Modern tech-styled BLE device scanner screen
 * Simplified for config-only functionality
 * Supports system dark/light theme with PTIT branding
 */
@Composable
fun ScanDeviceScreen(
    onDeviceConnected: (String) -> Unit,
    onBack: () -> Unit = {},
    vm: VMConfigDevice = hiltViewModel()
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by vm.ui.collectAsState()
    
    var permissionsGranted by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Kiểm tra trạng thái Bluetooth và Location
    var isBluetoothEnabled by remember { mutableStateOf(false) }
    var isLocationEnabled by remember { mutableStateOf(false) }
    
    // Hàm kiểm tra trạng thái
    fun checkSystemServices(): Pair<Boolean, Boolean> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        
        val btEnabled = bluetoothManager?.adapter?.isEnabled == true
        val locEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                         locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        
        return Pair(btEnabled, locEnabled)
    }
    
    // Cập nhật trạng thái khi vào màn hình và khi resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val (bt, loc) = checkSystemServices()
                isBluetoothEnabled = bt
                isLocationEnabled = loc
                
                // Nếu đang scan mà tắt BT/Location thì dừng scan
                if (uiState.scanning && (!bt || !loc)) {
                    vm.stopScan()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Kiểm tra ngay lập tức
        val (bt, loc) = checkSystemServices()
        isBluetoothEnabled = bt
        isLocationEnabled = loc
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        permissionsGranted = granted
        // Không tự động quét - người dùng sẽ nhấn nút để bắt đầu
    }

    fun requestBlePermissions() {
        val perms = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        permissionLauncher.launch(perms)
    }

    // Chỉ xin quyền khi vào app, không tự động quét
    LaunchedEffect(Unit) { requestBlePermissions() }

    // Loading Dialog
    LoadingDialog(
        show = isConnecting,
        message = "Đang kết nối thiết bị...",
        onDismiss = { isConnecting = false }
    )

    // Config Dialog
    if (showConfigDialog) {
        DeviceConfigDialog(
            deviceId = uiState.deviceId,
            wifiNetworks = uiState.wifiNetworks,
            loadingWifiList = uiState.loadingWifiList,
            onRefreshWifi = { vm.refreshWifiList() },
            onDismiss = {
                // Disconnect device when closing dialog so it can be scanned again
                vm.disconnectDevice()
                showConfigDialog = false
            },
            onSubmit = { ssid, pass, volume, brightness ->
                isConnecting = true
                showConfigDialog = false
                vm.configDevice(
                    ssid, pass, volume, brightness,
                    onSuccess = {
                        isConnecting = false
                        showSuccess = true
                    },
                    onError = { error ->
                        isConnecting = false
                        errorMessage = error
                        showError = true
                    }
                )
            }
        )
    }

    SuccessDialog(
        show = showSuccess,
        title = "Thành công",
        message = "Cấu hình thiết bị hoàn tất!\nThiết bị sẽ khởi động lại và kết nối WiFi.",
        confirmText = "Hoàn thành",
        onDismiss = {
            vm.disconnectDevice()
            showSuccess = false
        }
    )

    ErrorDialog(
        show = showError,
        title = "Lỗi kết nối",
        message = errorMessage.ifEmpty { "Không thể kết nối với thiết bị" },
        onDismiss = {
            vm.disconnectDevice()
            showError = false
        }
    )

    // Main Content
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with gradient
        TechBackground(colors)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.card.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                
                Text(
                    text = "Quét thiết bị",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                
                Spacer(modifier = Modifier.size(40.dp))
            }

            // Permission warning
            AnimatedVisibility(
                visible = !permissionsGranted,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                PermissionWarningCard(colors, onRequestPermission = { requestBlePermissions() })
            }
            
            // Bluetooth disabled warning
            AnimatedVisibility(
                visible = permissionsGranted && !isBluetoothEnabled,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                SystemServiceWarningCard(
                    colors = colors,
                    icon = Icons.Default.Bluetooth,
                    title = "Bluetooth đang tắt",
                    message = "Vui lòng bật Bluetooth trong cài đặt để quét thiết bị"
                )
            }
            
            // Location disabled warning
            AnimatedVisibility(
                visible = permissionsGranted && isBluetoothEnabled && !isLocationEnabled,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                SystemServiceWarningCard(
                    colors = colors,
                    icon = Icons.Default.LocationOn,
                    title = "Vị trí đang tắt",
                    message = "Vui lòng bật Vị trí (GPS) trong cài đặt để quét thiết bị BLE"
                )
            }
            
            // Center section - Radar takes most space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Radar with device positions based on signal strength
                DeviceRadar(
                    devices = uiState.devices,
                    isScanning = uiState.scanning,
                    colors = colors,
                    onDeviceClick = { device ->
                        isConnecting = true
                        vm.stopScan()
                        vm.connectDevice(device.address) {
                            isConnecting = false
                            showConfigDialog = true
                        }
                    }
                )
            }

            // Device count
            Text(
                text = if (uiState.scanning) 
                    "Đang quét... (${uiState.devices.size} thiết bị)" 
                else 
                    "Tìm thấy ${uiState.devices.size} thiết bị",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            // Scan button
            Spacer(modifier = Modifier.height(16.dp))
            TechScanButton(
                isScanning = uiState.scanning,
                enabled = permissionsGranted && isBluetoothEnabled && isLocationEnabled,
                colors = colors,
                onScan = {
                    // Kiểm tra lại trạng thái trước khi scan
                    val (bt, loc) = checkSystemServices()
                    isBluetoothEnabled = bt
                    isLocationEnabled = loc
                    
                    if (!bt || !loc) {
                        // Không cho scan nếu BT hoặc Location tắt
                        return@TechScanButton
                    }
                    
                    if (uiState.scanning) vm.stopScan()
                    else vm.startScan()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = TechColors.ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TechBackground(colors: AppColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (colors.isDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            Color(0xFF0D1520),
                            Color(0xFF0A1628),
                            colors.background
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            Color(0xFFE8EEF5),
                            Color(0xFFF0F5FA),
                            colors.background
                        )
                    )
                }
            )
            .drawBehind {
                // Grid pattern
                val gridSize = 40.dp.toPx()
                val lineColor = if (colors.isDark) {
                    TechColors.PTITRed.copy(alpha = 0.05f)
                } else {
                    TechColors.PTITRed.copy(alpha = 0.08f)
                }
                
                // Vertical lines
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridSize
                }
                
                // Horizontal lines
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridSize
                }
                
                // Glowing orbs - PTIT Red theme
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TechColors.PTITRed.copy(alpha = if (colors.isDark) 0.1f else 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, animatedOffset % size.height),
                        radius = 200f
                    ),
                    center = Offset(size.width * 0.2f, animatedOffset % size.height),
                    radius = 200f
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TechColors.OrangeAccent.copy(alpha = if (colors.isDark) 0.08f else 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.8f, size.height - (animatedOffset % size.height)),
                        radius = 250f
                    ),
                    center = Offset(size.width * 0.8f, size.height - (animatedOffset % size.height)),
                    radius = 250f
                )
            }
    )
}

@Composable
private fun TechHeader(colors: AppColors) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 24.dp)
    ) {
        // PTIT School Logo only
        Image(
            painter = painterResource(id = R.drawable.logo_ptit),
            contentDescription = "PTIT Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PTalk Config",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = colors.textPrimary
        )

        Text(
            text = "Cấu hình thiết bị qua Bluetooth",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        
        Text(
            text = "Học viện Công nghệ Bưu chính Viễn thông",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ScanningRadar(colors: AppColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Radar rings - PTIT Red theme
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale * (1f + index * 0.2f))
                    .alpha(alpha * (1f - index * 0.2f))
                    .border(
                        width = 2.dp,
                        color = colors.primary,
                        shape = CircleShape
                    )
            )
        }

        // Center icon
        Icon(
            imageVector = Icons.Default.Radar,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun TechDeviceCard(
    device: ScannedDevice,
    colors: AppColors,
    onClick: () -> Unit
) {
    val signalStrength = when {
        device.rssi >= -50 -> "Rất mạnh"
        device.rssi >= -60 -> "Mạnh"
        device.rssi >= -70 -> "Trung bình"
        else -> "Yếu"
    }
    
    val signalColor = when {
        device.rssi >= -50 -> TechColors.SuccessGreen
        device.rssi >= -60 -> colors.primary
        device.rssi >= -70 -> TechColors.WarningOrange
        else -> TechColors.ErrorRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.primary.copy(alpha = 0.3f),
                    colors.accent.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bluetooth icon with glow
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                signalColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothConnected,
                    contentDescription = null,
                    tint = signalColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "PTalk Device",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }

            // Signal indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${device.rssi} dBm",
                    style = MaterialTheme.typography.labelMedium,
                    color = signalColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = signalStrength,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun TechScanButton(
    isScanning: Boolean,
    enabled: Boolean,
    colors: AppColors,
    onScan: () -> Unit
) {
    Button(
        onClick = onScan,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) TechColors.ErrorRed else colors.primary,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isScanning) "Dừng quét" else "Quét thiết bị",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun PermissionWarningCard(colors: AppColors, onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechColors.WarningOrange.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, TechColors.WarningOrange.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onRequestPermission() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = TechColors.WarningOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cần cấp quyền Bluetooth",
                    style = MaterialTheme.typography.titleSmall,
                    color = TechColors.WarningOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nhấn để cấp quyền và bắt đầu quét",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SystemServiceWarningCard(
    colors: AppColors,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechColors.ErrorRed.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, TechColors.ErrorRed.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TechColors.ErrorRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TechColors.ErrorRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyDeviceHint(colors: AppColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy thiết bị",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary
        )
        Text(
            text = "Nhấn \"Quét thiết bị\" để bắt đầu tìm kiếm",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Radar display showing devices positioned by signal strength
 * Devices closer to center = stronger signal (closer device)
 * Responsive: scales based on screen size
 */
@Composable
private fun DeviceRadar(
    devices: List<ScannedDevice>,
    isScanning: Boolean,
    colors: AppColors,
    onDeviceClick: (ScannedDevice) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    // Scanning sweep animation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    
    // Pulse animation for rings
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Use BoxWithConstraints for responsive sizing
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Calculate radar size based on available space
        // Use smaller of width or reasonable max height, with padding
        val availableWidth = maxWidth
        val radarSize = (availableWidth * 0.85f).coerceIn(200.dp, 500.dp)
        val radarSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { radarSize.toPx() }
        
        Box(
            modifier = Modifier
                .size(radarSize)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radar background with rings
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val center = Offset(size.width / 2, size.height / 2)
                        val maxRadius = size.width / 2
                        
                        // Radar green color (like in movies)
                        val radarGreen = Color(0xFF00FF00)
                        val radarDarkGreen = Color(0xFF003300)
                        
                        // Background circle - dark green
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    radarDarkGreen,
                                    Color(0xFF001800)
                                )
                            ),
                            radius = maxRadius,
                            center = center
                        )
                        
                        // Distance rings (green like movie radar)
                        val ringCount = 4
                        for (i in 1..ringCount) {
                            val ringRadius = maxRadius * i / ringCount
                            drawCircle(
                                color = radarGreen.copy(alpha = 0.3f),
                                radius = ringRadius,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )
                        }
                        
                        // Cross lines - green
                        drawLine(
                            color = radarGreen.copy(alpha = 0.2f),
                            start = Offset(center.x, 0f),
                            end = Offset(center.x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = radarGreen.copy(alpha = 0.2f),
                            start = Offset(0f, center.y),
                            end = Offset(size.width, center.y),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        // Scanning sweep - bright green
                        if (isScanning) {
                            val sweepRadians = Math.toRadians(sweepAngle.toDouble())
                            val sweepEnd = Offset(
                                (center.x + maxRadius * kotlin.math.cos(sweepRadians)).toFloat(),
                                (center.y + maxRadius * kotlin.math.sin(sweepRadians)).toFloat()
                            )
                            
                            // Sweep trail effect
                            for (trail in 0..30) {
                                val trailAngle = Math.toRadians((sweepAngle - trail * 2).toDouble())
                                val trailEnd = Offset(
                                    (center.x + maxRadius * kotlin.math.cos(trailAngle)).toFloat(),
                                    (center.y + maxRadius * kotlin.math.sin(trailAngle)).toFloat()
                                )
                                drawLine(
                                    color = radarGreen.copy(alpha = (0.4f - trail * 0.012f).coerceAtLeast(0f)),
                                    start = center,
                                    end = trailEnd,
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            
                            // Main sweep line
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        radarGreen,
                                        radarGreen.copy(alpha = 0f)
                                    ),
                                    start = center,
                                    end = sweepEnd
                                ),
                                start = center,
                                end = sweepEnd,
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Radar green for UI elements
                val radarGreen = Color(0xFF00FF00)
                
                // Calculate marker offset multiplier based on radar size
                val markerOffsetMultiplier = (radarSize.value * 0.4f).coerceIn(80f, 200f)
                
                // Pulse animation when scanning - green
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseScale)
                            .alpha(pulseAlpha)
                            .border(2.dp, radarGreen, CircleShape)
                    )
                }
                
                // Center icon - green (size scales with radar)
                val centerIconSize = (radarSize.value * 0.12f).coerceIn(32f, 60f).dp
                Box(
                    modifier = Modifier
                        .size(centerIconSize)
                        .background(radarGreen.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(centerIconSize * 0.6f)
                    )
                }
                
                // Device markers positioned by signal strength
                devices.forEachIndexed { index, device ->
                    val position = calculateDevicePosition(device.rssi, device.address)
                    
                    DeviceMarker(
                        device = device,
                        position = position,
                        offsetMultiplier = markerOffsetMultiplier,
                        radarSize = radarSize.value,
                        onClick = { onDeviceClick(device) }
                    )
                }
            }
        }
        

    }
}

/**
 * Calculate device position on radar based on RSSI and MAC address
 * Stronger signal = closer to center (smooth continuous mapping)
 * MAC address determines the angle (stable position per device)
 */
private fun calculateDevicePosition(rssi: Int, macAddress: String): Offset {
    // RSSI typically ranges from -30 (very close) to -100 (far)
    // Map RSSI to distance using smooth linear interpolation
    // -30 dBm → 0.12 (very close to center)
    // -100 dBm → 0.88 (near edge)
    val minRssi = -100f
    val maxRssi = -30f
    val minDistance = 0.12f
    val maxDistance = 0.88f
    
    // Clamp RSSI to valid range and calculate normalized distance
    val clampedRssi = rssi.coerceIn(minRssi.toInt(), maxRssi.toInt()).toFloat()
    val rssiRatio = (clampedRssi - minRssi) / (maxRssi - minRssi)  // 0 (far) to 1 (close)
    val normalizedDistance = maxDistance - (rssiRatio * (maxDistance - minDistance))  // Invert: close = small distance
    
    // Use MAC address hash as seed - this gives stable angle per device
    val random = Random(macAddress.hashCode())
    val angle = Math.toRadians(random.nextDouble() * 360)
    
    val x = (normalizedDistance * kotlin.math.cos(angle)).toFloat()
    val y = (normalizedDistance * kotlin.math.sin(angle)).toFloat()
    
    return Offset(x, y)
}

@Composable
private fun DeviceMarker(
    device: ScannedDevice,
    position: Offset,
    offsetMultiplier: Float,
    radarSize: Float,
    onClick: () -> Unit
) {
    // All devices use radar green color
    val radarGreen = Color(0xFF00FF00)
    
    // Subtle glow animation (not bouncing)
    val infiniteTransition = rememberInfiniteTransition(label = "marker")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Scale marker size based on radar size
    val markerSize = (radarSize * 0.1f).coerceIn(28f, 50f).dp
    val fontSize = (radarSize * 0.045f).coerceIn(12f, 20f).sp
    
    Box(
        modifier = Modifier
            .offset(
                x = (position.x * offsetMultiplier).dp,
                y = (position.y * offsetMultiplier).dp
            )
            .size(markerSize)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        radarGreen.copy(alpha = glowAlpha),
                        radarGreen.copy(alpha = 0.3f)
                    )
                ),
                shape = CircleShape
            )
            .border(2.dp, radarGreen, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Letter "P" instead of Bluetooth icon
        Text(
            text = "P",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        )
    }
}


