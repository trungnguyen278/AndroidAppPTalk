package com.avis.app.ptalk.ui.screen.config

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avis.app.ptalk.LocalAppColors
import com.avis.app.ptalk.R
import com.avis.app.ptalk.ui.theme.TechColors
import com.avis.app.ptalk.ui.viewmodel.VMHome
import com.avis.app.ptalk.domain.model.Device
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

/**
 * Home Screen - Shows PTIT logo, user devices, and button to connect new device
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToControl: (String, String) -> Unit,
    onSignOut: () -> Unit = {},
    viewModel: VMHome = hiltViewModel()
) {
    val colors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    var showDeviceManagement by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val deviceSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Profile Bottom Sheet
    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            sheetState = sheetState,
            containerColor = colors.card
        ) {
            ProfileSheetContent(
                username = viewModel.getUsername(),
                email = viewModel.getEmail(),
                phone = viewModel.getPhone(),
                userId = viewModel.getUserId(),
                colors = colors,
                onManageDevices = {
                    scope.launch {
                        sheetState.hide()
                        showProfileSheet = false
                        showDeviceManagement = true
                    }
                },
                onSignOut = {
                    scope.launch {
                        sheetState.hide()
                        showProfileSheet = false
                        viewModel.signOut()
                        onSignOut()
                    }
                }
            )
        }
    }

    // Device Management Bottom Sheet
    if (showDeviceManagement) {
        ModalBottomSheet(
            onDismissRequest = { showDeviceManagement = false },
            sheetState = deviceSheetState,
            containerColor = colors.card
        ) {
            DeviceManagementSheetContent(
                devices = uiState.devices,
                colors = colors,
                onDeleteDevice = { device -> viewModel.deleteDevice(device) }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated background
        HomeBackground(colors.isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with profile button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { showProfileSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.card.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Tài khoản",
                        tint = TechColors.PTITRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // PTIT University Logo
            Image(
                painter = painterResource(id = R.drawable.logo_ptit),
                contentDescription = "PTIT University Logo",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "HỌC VIỆN CÔNG NGHỆ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TechColors.PTITRed,
                textAlign = TextAlign.Center
            )
            Text(
                text = "BƯU CHÍNH VIỄN THÔNG",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TechColors.PTITRed,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Devices Section
            Text(
                text = "Thiết bị của bạn",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.isLoading) {
                CircularProgressIndicator(color = TechColors.PTITRed)
            } else if (!uiState.error.isNullOrEmpty()) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (uiState.devices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.card)
                ) {

                    Text(
                        "Bạn chưa có thiết bị nào. Nhấn Bắt đầu cấu hình để thêm mới.",
                        color = colors.textSecondary,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                uiState.devices.forEach { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onNavigateToControl(device.macAddress, device.name ?: device.macAddress) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(TechColors.PTITRed.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TouchApp, null, tint = TechColors.PTITRed)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name ?: "Thiết bị không tên",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "MAC: ${device.macAddress}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Enter scan button
            Button(
                onClick = onNavigateToScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechColors.PTITRed,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Bắt đầu cấu hình",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GuideStep(
    stepNumber: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    colors: com.avis.app.ptalk.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Step number circle
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = TechColors.PTITRed,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun HomeBackground(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "homeBg")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            TechColors.DarkBackground,
                            Color(0xFF0D1520),
                            TechColors.DarkBackground
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            TechColors.LightBackground,
                            Color(0xFFE8EEF5),
                            TechColors.LightBackground
                        )
                    )
                }
            )
            .drawBehind {
                // Subtle glow effects
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TechColors.PTITRed.copy(alpha = if (isDark) 0.08f else 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, animatedOffset % size.height),
                        radius = 300f
                    ),
                    center = Offset(size.width * 0.3f, animatedOffset % size.height),
                    radius = 300f
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TechColors.OrangeAccent.copy(alpha = if (isDark) 0.06f else 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.7f, size.height - (animatedOffset % size.height)),
                        radius = 350f
                    ),
                    center = Offset(size.width * 0.7f, size.height - (animatedOffset % size.height)),
                    radius = 350f
                )
            }
    )
}

@Composable
private fun ProfileSheetContent(
    username: String?,
    email: String?,
    phone: String?,
    userId: String?,
    colors: com.avis.app.ptalk.ui.theme.AppColors,
    onManageDevices: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(TechColors.PTITRed, TechColors.OrangeAccent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (username?.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = username ?: "Ng\u01b0\u1eddi d\u00f9ng",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

        Spacer(modifier = Modifier.height(16.dp))

        // User info rows
        if (!email.isNullOrBlank()) {
            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = email,
                colors = colors
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!phone.isNullOrBlank()) {
            ProfileInfoRow(
                icon = Icons.Default.Phone,
                label = "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i",
                value = phone,
                colors = colors
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!userId.isNullOrBlank()) {
            ProfileInfoRow(
                icon = Icons.Default.Person,
                label = "User ID",
                value = userId,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.15f))

        Spacer(modifier = Modifier.height(16.dp))

        // Manage devices button
        Button(
            onClick = onManageDevices,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TechColors.PTITRed,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Qu\u1ea3n l\u00fd thi\u1ebft b\u1ecb",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sign out button
        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "\u0110\u0103ng xu\u1ea5t",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun DeviceManagementSheetContent(
    devices: List<Device>,
    colors: com.avis.app.ptalk.ui.theme.AppColors,
    onDeleteDevice: (Device) -> Unit
) {
    var deviceToDelete by remember { mutableStateOf<Device?>(null) }

    // Confirm delete dialog
    deviceToDelete?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            title = { Text("X\u00f3a thi\u1ebft b\u1ecb") },
            text = {
                Text("B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n x\u00f3a thi\u1ebft b\u1ecb \"${device.name ?: device.macAddress}\"?\n\nThi\u1ebft b\u1ecb s\u1ebd \u0111\u01b0\u1ee3c chuy\u1ec3n v\u1ec1 ch\u1ebf \u0111\u1ed9 c\u1ea5u h\u00ecnh BLE.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDevice(device)
                        deviceToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("X\u00f3a")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToDelete = null }) {
                    Text("H\u1ee7y")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Qu\u1ea3n l\u00fd thi\u1ebft b\u1ecb",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (devices.isEmpty()) {
            Text(
                "Ch\u01b0a c\u00f3 thi\u1ebft b\u1ecb n\u00e0o.",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            devices.forEach { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.background
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            null,
                            tint = TechColors.PTITRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                device.name ?: "Thi\u1ebft b\u1ecb",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                device.macAddress,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                        IconButton(
                            onClick = { deviceToDelete = device }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "X\u00f3a",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    colors: com.avis.app.ptalk.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(TechColors.PTITRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TechColors.PTITRed,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary
            )
        }
    }
}
