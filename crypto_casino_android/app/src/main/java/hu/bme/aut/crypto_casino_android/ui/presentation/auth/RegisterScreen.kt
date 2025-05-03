package hu.bme.aut.crypto_casino_android.ui.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.components.CasinoButton
import hu.bme.aut.crypto_casino_android.ui.components.CasinoTextField

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        when (registerState) {
            is ApiResult.Success -> {
                isLoading = false
                onRegisterSuccess()
            }
            is ApiResult.Error -> {
                isLoading = false
            }
            ApiResult.Loading -> {
                isLoading = true
            }
            null -> {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetStates()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create an Account",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            CasinoTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CasinoTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CasinoTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CasinoTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                errorText = if (password != confirmPassword && confirmPassword.isNotEmpty()) "Passwords do not match" else null,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (registerState is ApiResult.Error) {
                Text(
                    text = (registerState as ApiResult.Error).exception.message ?: "Registration failed",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            CasinoButton(
                text = if (isLoading) "Registering..." else "Register",
                onClick = {
                    if (password == confirmPassword) {
                        viewModel.register(username, email, password)
                    }
                },
                enabled = !isLoading &&
                        username.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        password == confirmPassword,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Already have an account? Login")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
