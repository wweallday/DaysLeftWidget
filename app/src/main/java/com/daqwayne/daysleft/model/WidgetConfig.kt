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
}

data class WidgetConfig(
    val eventId: Long? = null,
    val shape: DotShape = DotShape.SQUARE,
    val color: DotColor = DotColor.GREEN,
)
