package com.avis.app.ptalk.ui.screen.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.avis.app.ptalk.LocalAppColors
import com.avis.app.ptalk.domain.control.WifiNetwork
import com.avis.app.ptalk.ui.theme.AppColors
import com.avis.app.ptalk.ui.theme.TechColors

/**
 * Modern tech-styled device configuration dialog
 * Supports system dark/light theme with PTIT branding
 */
@Composable
fun DeviceConfigDialog(
    deviceId: String = "",
    wifiNetworks: List<WifiNetwork> = emptyList(),
    loadingWifiList: Boolean = false,
    onRefreshWifi: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onSubmit: (ssid: String, password: String, volume: Float, brightness: Float) -> Unit
) {
    val colors = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var wifiSsid by remember { mutableStateOf("") }
    var wifiPass by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(0.6f) }       // Default 60%
    var brightness by remember { mutableStateOf(1.0f) }   // Default 100%
    var showWifiDropdown by remember { mutableStateOf(false) }
    
    // Auto-refresh if wifi list is empty when dialog opens
    androidx.compose.runtime.LaunchedEffect(wifiNetworks) {
        if (wifiNetworks.isEmpty() && !loadingWifiList) {
            onRefreshWifi()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.5f),
                        colors.accent.copy(alpha = 0.5f)
                    )
                )
            ),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                DialogHeader(
                    deviceId = deviceId,
                    colors = colors,
                    onClose = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onDismiss()
                    }
                )

                // WiFi Section
                TechSectionCard(
                    icon = Icons.Default.Wifi,
                    title = "Cấu hình WiFi",
                    iconColor = colors.primary,
                    colors = colors
                ) {
                    // WiFi SSID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tên WiFi",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary
                        )
                        if (loadingWifiList) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.primary
                            )
                        } else {
                            IconButton(
                                onClick = onRefreshWifi,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TechTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            placeholder = if (wifiNetworks.isEmpty()) "Nhập tên WiFi" else "Chọn hoặc nhập WiFi",
                            leadingIcon = Icons.Default.Wifi,
                            colors = colors,
                            trailingIcon = if (wifiNetworks.isNotEmpty()) {
                                {
                                    IconButton(onClick = { showWifiDropdown = !showWifiDropdown }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Show WiFi list",
                                            tint = colors.primary
                                        )
                                    }
                                }
                            } else null
                        )

                        DropdownMenu(
                            expanded = showWifiDropdown,
                            onDismissRequest = { showWifiDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 200.dp)
                                .background(colors.card)
                        ) {
                            wifiNetworks.forEach { network ->
                                DropdownMenuItem(
                                    text = {
                                        WifiNetworkItem(network, colors)
                                    },
                                    onClick = {
                                        wifiSsid = network.ssid
                                        showWifiDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    Text(
                        text = "Mật khẩu WiFi",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TechTextField(
                        value = wifiPass,
                        onValueChange = { wifiPass = it },
                        placeholder = "Nhập mật khẩu",
                        isPassword = !isPasswordVisible,
                        colors = colors,
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff 
                                                  else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = colors.textMuted
                                )
                            }
                        }
                    )
                }

                // Device Settings Section
                TechSectionCard(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt thiết bị",
                    iconColor = colors.accent,
                    colors = colors
                ) {
                    // Volume
                    TechSlider(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Âm lượng",
                        value = volume,
                        onValueChange = { volume = it },
                        color = colors.primary,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Brightness
                    TechSlider(
                        icon = Icons.Default.BrightnessMedium,
                        label = "Độ sáng",
                        value = brightness,
                        onValueChange = { brightness = it },
                        color = colors.accent,
                        colors = colors
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Hủy",
                            color = TechColors.ErrorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onSubmit(wifiSsid, wifiPass, volume, brightness)
                        },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lưu cấu hình",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    deviceId: String,
    colors: AppColors,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cấu hình thiết bị",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colors.textPrimary
            )
            if (deviceId.isNotEmpty()) {
                Text(
                    text = "ID: $deviceId",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary
                )
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = colors.card,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TechSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    colors: AppColors,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.card
        ),
        border = BorderStroke(
            width = 1.dp,
            color = iconColor.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = colors.textPrimary
                )
            }
            content()
        }
    }
}

@Composable
private fun TechTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    colors: AppColors,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = colors.textMuted
            )
        },
        singleLine = true,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = trailingIcon,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.cardHighlight,
            cursorColor = colors.primary,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
        )
    )
}

@Composable
private fun TechSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    colors: AppColors
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        label = "slider"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            Text(
                text = "${(animatedValue * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun WifiNetworkItem(network: WifiNetwork, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = network.ssid,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SignalWifi4Bar,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when {
                    network.rssi >= -50 -> TechColors.SuccessGreen
                    network.rssi >= -70 -> TechColors.WarningOrange
                    else -> TechColors.ErrorRed
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${network.rssi}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}
