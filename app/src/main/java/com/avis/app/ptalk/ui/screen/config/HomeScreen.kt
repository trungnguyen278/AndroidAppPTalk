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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avis.app.ptalk.LocalAppColors
import com.avis.app.ptalk.R
import com.avis.app.ptalk.ui.theme.TechColors

/**
 * Home Screen - Shows PTIT logo and button to enter scan screen
 */
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit
) {
    val colors = LocalAppColors.current
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated background
        HomeBackground(colors.isDark)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // PTIT University Logo
            Image(
                painter = painterResource(id = R.drawable.logo_ptit),
                contentDescription = "PTIT University Logo",
                modifier = Modifier.size(140.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // University name
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
            
            // User Guide Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.card
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TechColors.PTITRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Hướng dẫn sử dụng",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Step 1
                    GuideStep(
                        stepNumber = 1,
                        icon = Icons.Default.Search,
                        title = "Quét thiết bị",
                        description = "Nhấn nút \"Bắt đầu cấu hình\" bên dưới để quét các thiết bị PTalk xung quanh",
                        colors = colors
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Step 2
                    GuideStep(
                        stepNumber = 2,
                        icon = Icons.Default.RadioButtonChecked,
                        title = "Xem trên radar",
                        description = "Thiết bị sẽ xuất hiện trên màn hình radar. Gần tâm = tín hiệu mạnh",
                        colors = colors
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Step 3
                    GuideStep(
                        stepNumber = 3,
                        icon = Icons.Default.TouchApp,
                        title = "Chọn thiết bị",
                        description = "Nhấn vào biểu tượng Bluetooth màu xanh trên radar để chọn thiết bị cần cấu hình",
                        colors = colors
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Step 4
                    GuideStep(
                        stepNumber = 4,
                        icon = Icons.Default.Settings,
                        title = "Cấu hình WiFi",
                        description = "Nhập thông tin WiFi (SSID và mật khẩu) để thiết bị kết nối mạng",
                        colors = colors
                    )
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
