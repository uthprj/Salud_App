package com.example.salud_app.ui.screen.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.salud_app.components.AppScaffold
import com.example.salud_app.components.ScreenLevel
import com.example.salud_app.model.FoodItem
import com.example.salud_app.model.Meal
import com.example.salud_app.ui.theme.Salud_AppTheme

@Composable
fun DiaryScreen() {
    val navController = rememberNavController()

    // Dữ liệu mẫu (bạn có thể thay bằng database sau này)
    val sampleMeals = listOf(
        Meal(
            name = "Bữa sáng",
            calories = 350,
            proteins = 15.0,
            carbohydrates = 40.0,
            fats = 10.0,
            foodItems = listOf(
                FoodItem("Bánh mì trứng", 200),
                FoodItem("Sữa tươi", 150)
            )
        ),
        Meal(
            name = "Bữa trưa",
            calories = 650,
            proteins = 30.0,
            carbohydrates = 80.0,
            fats = 18.0,
            foodItems = listOf(
                FoodItem("Cơm", 300),
                FoodItem("Thịt gà", 250),
                FoodItem("Rau luộc", 100)
            )
        ),
        Meal(
            name = "Bữa tối",
            calories = 450,
            proteins = 22.0,
            carbohydrates = 55.0,
            fats = 12.0,
            foodItems = listOf(
                FoodItem("Bún bò", 400),
                FoodItem("Trà đá", 50)
            )
        )
    )

    Salud_AppTheme {
        AppScaffold(
            navController = navController,
            title = "Nhật ký",
            screenLevel = ScreenLevel.MAIN
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(12.dp)
            ) {
                MealCardList(meals = sampleMeals)
            }
        }
    }
}

@Composable
fun MealCardList(meals: List<Meal>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        meals.forEach { meal ->
            ExpandableMealCard(meal = meal)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ExpandableMealCard(meal: Meal) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EBFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "+${meal.calories} Calo",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp
                )
            }

            // Nội dung mở rộng
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    meal.foodItems.forEach { food ->
                        Text(
                            text = "• ${food.name} (${food.calories} Calo)",
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Protein: ${meal.proteins}g | Carbs: ${meal.carbohydrates}g | Fat: ${meal.fats}g",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DiaryScreenPreview() {
    DiaryScreen()
}