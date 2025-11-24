package com.example.salud_app.ui.screen.sign

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.salud_app.R
import com.example.salud_app.model.User

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: SignInViewModel
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(130.dp))

        Image(
            painter = painterResource(id = R.drawable.salud_logo),
            contentDescription = null,
            modifier = Modifier.height(150.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Đăng nhập",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email / username
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(text = "Mail hoặc username") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            singleLine = true,
            shape = RoundedCornerShape(25.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(text = "Mật khẩu") },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            singleLine = true,
            shape = RoundedCornerShape(25.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Quên mật khẩu?",
            color = Color(0xFF3B82F6),
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Button đăng nhập
        Button(
            onClick = {
                Toast.makeText(context, "Đăng nhập bằng email/password", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .width(200.dp)
                .height(45.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "ĐĂNG NHẬP",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "hoặc đăng nhập với",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Google Sign In
        Image(
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    // Bỏ launcher, chỉ truyền null
                    viewModel.signInWithGoogle(
                        context,
                        launcher = null,
                        onSuccess = { user ->
                            Toast.makeText(context, "Đăng nhập thành công: ${user.username}", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
        )

        Spacer(modifier = Modifier.height(25.dp))

        Row {
            Text("Chưa có tài khoản?")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Đăng ký ngay!",
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Đi đến màn hình đăng ký", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
