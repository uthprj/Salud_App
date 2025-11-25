package com.example.salud_app.ui.screen.sign

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.salud_app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: SignInViewModel
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            // IMAGE OUTSIDE CARD
            Image(
                painter = painterResource(id = R.drawable.salud_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(250.dp)
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(10.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Chào mừng trở lại",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Đăng nhập bằng email/password", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text(
                            text = "Đăng nhập",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E9EE))
                        Text(text = "  Hoặc  ", color = Color.Gray, fontSize = 12.sp)
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFFE6E9EE))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.signInWithGoogle(
                                context,
                                launcher = null,
                                onSuccess = {
                                    Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                                    navController.navigate("fill-info") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google_icon),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Đăng nhập bằng Google", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun LoginScreenPreview() {
//    LoginScreen(navController = NavController(LocalContext.current), viewModel = ())
//}