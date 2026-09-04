package com.delta.vuelvo_commerce.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The Vuelvo line-icon set, recreated from vuelvo-icons.jsx / vuelvo-biz-icons.jsx.
 * Each icon is the same 24×24 SVG path data the prototype used, so the silhouettes
 * are identical. Stroked icons tint to [Color.Black] and are recoloured at the call
 * site via `Icon(tint = …)`; small solid accents are baked as fills.
 */
private fun vuIcon(
    name: String,
    strokeWidth: Float = 2f,
    strokes: List<String> = emptyList(),
    fills: List<String> = emptyList(),
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    strokes.forEach { d ->
        addPath(
            pathData = addPathNodes(d),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    fills.forEach { d ->
        addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black))
    }
}.build()

object VuelvoIcons {

    // ── Category icons ──────────────────────────────────────────
    val Coffee = vuIcon(
        "Coffee", 1.9f, strokes = listOf(
            "M5 9h11v5a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4V9Z",
            "M16 10h2.2a2.3 2.3 0 0 1 0 4.6H16",
            "M8 3.2c-.5.7-.5 1.4 0 2.1M11.5 3.2c-.5.7-.5 1.4 0 2.1",
        )
    )
    val Bread = vuIcon(
        "Bread", 1.9f, strokes = listOf(
            "M4.5 11.5c-1.4-2 .1-4.8 2.6-4.8 1 0 1.7.4 2.2 1 .5-1 1.6-1.7 2.9-1.7 1.9 0 3.1 1.4 3.1 3 1.6.2 2.7 1.4 2.7 2.9 0 .9-.4 1.6-1 2.1L9 19.5a2 2 0 0 1-2.6-.2l-2.5-2.6a2 2 0 0 1 0-2.8l.6-.6Z",
            "M9.5 10.5 7 13M13 11l-2.5 2.5",
        )
    )
    val Scissors = vuIcon(
        "Scissors", 1.9f, strokes = listOf(
            "M3.4 6 a2.6 2.6 0 1 0 5.2 0 a2.6 2.6 0 1 0 -5.2 0",
            "M3.4 18 a2.6 2.6 0 1 0 5.2 0 a2.6 2.6 0 1 0 -5.2 0",
            "M8.3 7.6 20 17M8.3 16.4 20 7M11 12l2.2 1.8",
        )
    )
    val Fork = vuIcon(
        "Fork", 1.9f, strokes = listOf(
            "M7 3v6a2 2 0 0 0 2 2v0M9 3v8M7 3v4M9 21V11",
            "M16 3c-1.6 0-2.5 1.6-2.5 4s.9 3.5 2.5 3.5S18.5 9.4 18.5 7 17.6 3 16 3ZM16 10.5V21",
        )
    )
    val IceCream = vuIcon(
        "IceCream", 1.9f, strokes = listOf(
            "M8 9a4 4 0 0 1 8 0",
            "M7.6 9h8.8L12 21 7.6 9Z",
            "M9 13h6",
        )
    )
    val Store = vuIcon(
        "Store", 1.9f, strokes = listOf(
            "M4 9.5 5.4 5h13.2L20 9.5M4 9.5h16M4 9.5a2 2 0 0 0 4 0 2 2 0 0 0 4 0 2 2 0 0 0 4 0 2 2 0 0 0 4 0",
            "M5 11v8.5h14V11M9.5 19.5v-4.5h5v4.5",
        )
    )

    // ── UI icons ────────────────────────────────────────────────
    val Tag = vuIcon(
        "Tag", 2f,
        strokes = listOf(
            "M4 11.5V5.5a1.5 1.5 0 0 1 1.5-1.5h6L20 12.5a1.6 1.6 0 0 1 0 2.3l-5.2 5.2a1.6 1.6 0 0 1-2.3 0L4 11.5Z",
            "M7 8.4 a1.4 1.4 0 1 0 2.8 0 a1.4 1.4 0 1 0 -2.8 0",
        )
    )
    val Crown = vuIcon(
        "Crown", 2f, strokes = listOf(
            "M3.5 8.5 7 12l5-6.5 5 6.5 3.5-3.5-1.6 10H5.1L3.5 8.5Z",
            "M5.5 20h13",
        )
    )
    val Lock = vuIcon(
        "Lock", 2f,
        strokes = listOf(
            "M7.1 10.5 h9.8 a2.6 2.6 0 0 1 2.6 2.6 v4.8 a2.6 2.6 0 0 1 -2.6 2.6 h-9.8 a2.6 2.6 0 0 1 -2.6 -2.6 v-4.8 a2.6 2.6 0 0 1 2.6 -2.6 Z",
            "M8 10.5V8a4 4 0 0 1 8 0v2.5",
        ),
        fills = listOf(
            "M10.7 15 a1.3 1.3 0 1 0 2.6 0 a1.3 1.3 0 1 0 -2.6 0",
        )
    )
    val Camera = vuIcon(
        "Camera", 2f,
        strokes = listOf(
            "M4 9.5a2 2 0 0 1 2-2h1.4l1.1-1.8a1 1 0 0 1 .85-.47h5.3a1 1 0 0 1 .85.47L16.6 7.5H18a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-7Z",
            "M8.7 12.8 a3.3 3.3 0 1 0 6.6 0 a3.3 3.3 0 1 0 -6.6 0",
        )
    )
    val Minus = vuIcon("Minus", 2.4f, strokes = listOf("M5 12h14"))
    val Plus = vuIcon("Plus", 2.4f, strokes = listOf("M12 5v14M5 12h14"))
    val Check = vuIcon("Check", 2.6f, strokes = listOf("M4.5 12.5 10 17.5 19.5 6.5"))
    val ChevronDown = vuIcon("ChevronDown", 2.4f, strokes = listOf("M6 9.5 12 15.5 18 9.5"))
    val Nfc = vuIcon(
        "Nfc", 2.2f,
        strokes = listOf(
            "M6 8.5C7.6 7 9.7 6 12 6s4.4 1 6 2.5",
            "M8.3 11c1-.9 2.3-1.5 3.7-1.5s2.7.6 3.7 1.5",
            "M10.6 13.5c.4-.3.9-.5 1.4-.5s1 .2 1.4.5",
        ),
        fills = listOf(
            "M11.4 16.5 a0.6 0.6 0 1 0 1.2 0 a0.6 0.6 0 1 0 -1.2 0",
        )
    )
}
