package com.example.salud_app.ui.screen.sign

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.salud_app.R
import androidx.compose.ui.unit.*
import androidx.navigation.NavController


@Composable
fun LoginScreen(
    navController: NavController
) {

    Column(

        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize()
            .padding(horizontal = 32.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(130.dp))

        Image(
            painter = painterResource(id = R.drawable.salud_logo),
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))


        Text(
            text = "Đăng nhập",
            fontSize = 40.sp,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
            )

        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = {
                Image(
                    painter = painterResource(id = R.drawable.person_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                )
                Text(
                    text = "Mail hoặc username",
                    modifier = Modifier.padding(start = 30.dp)
                )
                          },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            singleLine = true,
            shape = RoundedCornerShape(25.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = {
                Image(
                    painter = painterResource(id = R.drawable.password_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                )
                Text(
                    text = "Mật khẩu",
                    modifier = Modifier.padding(start = 30.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.visibility_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.End)
                )
            },
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
            modifier = Modifier
                .align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .width(200.dp)
                .height(45.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3B82F6)
            ),
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

        Image(
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        Row {
            Text("Chưa có tài khoản?")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Đăng ký ngay!",
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavController(LocalContext.current) )
}

