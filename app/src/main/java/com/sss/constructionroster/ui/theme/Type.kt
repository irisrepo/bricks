package com.sss.constructionroster.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
)

// Define the labelAmount style as a separate constant
val AmountTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.W600,
    fontSize = 10.sp,
    lineHeight = 10.sp,
    letterSpacing = 0.25.sp,
    color = Color(0xFF2196F3)
)

// Extension property for Typography to access the amount style
val Typography.labelAmount: TextStyle
    get() = AmountTextStyle