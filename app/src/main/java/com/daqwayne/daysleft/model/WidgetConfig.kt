package com.daqwayne.daysleft.model

enum class DotShape { SQUARE, CIRCLE, X }

enum class DotColor(
    val hex: Long,
) {
    GREEN(0xFF4CAF50),
    BLUE(0xFF2196F3),
    ORANGE(0xFFFF9800),
    RED(0xFFF44336),
    YELLOW(0xFFFFEB3B),
    MATERIAL_YOU(-1L),
}

data class WidgetConfig(
    val eventId: Long? = null,
    val shape: DotShape = DotShape.SQUARE,
    val color: DotColor = DotColor.GREEN,
    val leftPadding: Float = 12f,
    val rightPadding: Float = 12f,
    val topPadding: Float = 12f,
    val bottomPadding: Float = 64f, // Reserves space for the bottom text row
    val dotSpacing: Float = 4f      // Updated from 2f based on our previous chat
)
