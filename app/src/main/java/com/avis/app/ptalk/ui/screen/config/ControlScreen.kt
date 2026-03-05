package com.avis.app.ptalk.ui.screen.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avis.app.ptalk.LocalAppColors
import com.avis.app.ptalk.ui.theme.TechColors
import com.avis.app.ptalk.ui.viewmodel.VMControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    macAddress: String,
    deviceName: String,
    onBack: () -> Unit,
    viewModel: VMControl = hiltViewModel()
) {
    val colors = LocalAppColors.current
    val status by viewModel.deviceStatus.collectAsState()
    val isConnected by viewModel.connectionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDeviceOnline = status != null

    var volume by remember { mutableStateOf(50f) }
    var brightness by remember { mutableStateOf(50f) }

    LaunchedEffect(macAddress) {
        viewModel.initConnection(macAddress)
    }

    LaunchedEffect(status) {
        status?.let {
            volume = (it.volume ?: 50).toFloat()
            brightness = (it.brightness ?: 50).toFloat()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Điều khiển: $deviceName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechColors.PTITRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Trạng thái kết nối",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val connectionText = when {
                        isDeviceOnline -> "Thiết bị trực tuyến"
                        isConnected -> "Đang chờ thiết bị..."
                        else -> "Đang kết nối MQTT..."
                    }
                    
                    val connectionColor = when {
                        isDeviceOnline -> Color(0xFF4CAF50)
                        isConnected -> TechColors.OrangeAccent
                        else -> TechColors.PTITRed
                    }
                    
                    Text(
                        connectionText,
                        color = connectionColor,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    
                    status?.let { st ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Pin thiết bị: ${st.batteryLevel ?: "?"}% | Uptime: ${st.uptimeSec ?: "?"}s",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Volume Control
            Text("Âm lượng", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, null, tint = TechColors.PTITRed)
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    onValueChangeFinished = { viewModel.setVolume(volume.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = TechColors.PTITRed,
                        activeTrackColor = TechColors.PTITRed
                    ),
                    enabled = isConnected && isDeviceOnline
                )
                Text("${volume.toInt()}%", color = colors.textSecondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brightness Control
            Text("Độ sáng màn hình", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BrightnessMedium, null, tint = TechColors.OrangeAccent)
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    onValueChangeFinished = { viewModel.setBrightness(brightness.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = TechColors.OrangeAccent,
                        activeTrackColor = TechColors.OrangeAccent
                    ),
                    enabled = isConnected && isDeviceOnline
                )
                Text("${brightness.toInt()}%", color = colors.textSecondary)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Danger Zone
            Text("Nâng cao", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.rebootDevice() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isConnected && isDeviceOnline && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = DefaultCardColors, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, null)
                Spacer(Modifier.width(8.dp))
                Text("Khởi động lại thiết bị")
            }
        }
    }
}
private val DefaultCardColors = Color(0xFFF44336)
