package com.avis.app.ptalk.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avis.app.ptalk.LocalAppColors
import com.avis.app.ptalk.R
import com.avis.app.ptalk.ui.theme.TechColors
import com.avis.app.ptalk.ui.viewmodel.auth.VMSignup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VMSignup = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current
    var authUsername by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "TẠO TÀI KHOẢN",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TechColors.PTITRed
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = authUsername,
                onValueChange = { authUsername = it; viewModel.clearError() },
                label = { Text("Tên đăng nhập *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = TechColors.PTITRed,
                    focusedLabelColor = TechColors.PTITRed
                )
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; viewModel.clearError() },
                label = { Text("Tên người dùng *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = TechColors.PTITRed,
                    focusedLabelColor = TechColors.PTITRed
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = TechColors.PTITRed,
                    focusedLabelColor = TechColors.PTITRed
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; viewModel.clearError() },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = TechColors.PTITRed,
                    focusedLabelColor = TechColors.PTITRed
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = { Text("Mật khẩu *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = TechColors.PTITRed,
                    focusedLabelColor = TechColors.PTITRed
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passConfirm,
                onValueChange = { passConfirm = it; viewModel.clearError() },
                label = { Text("Xác nhận mật khẩu *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = TechColors.PTITRed,
                    focusedLabelColor = TechColors.PTITRed
                )
            )

            if (!uiState.error.isNullOrEmpty()) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {

                            if (
                                authUsername.isBlank() ||
                                username.isBlank() ||
                                password.isBlank() ||
                                passConfirm.isBlank()
                            ) {
                                return@Button
                            }

                            if (password != passConfirm) {
                                return@Button
                            }

                            viewModel.signup(
                                            authUsername = authUsername,
                                            email = email,
                                            pass = password,
                                            passConfirm = passConfirm,
                                            username = username,
                                            phone = phone
                                        )
                        },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechColors.PTITRed,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Đăng ký",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản? ", color = colors.textSecondary)
                Text(
                    text = "Đăng nhập",
                    color = TechColors.PTITRed,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
        }
    }
}
