import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun OverlayView(
    boxRect: Rect?,
    fontName: String,
    confidence: Float
) {
    Canvas(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        boxRect?.let { rect ->

            val margin = 20.dp.toPx()
            val expandedRect = Rect(
                rect.left - margin.toInt(),
                rect.top - margin.toInt(),
                rect.right + margin.toInt(),
                rect.bottom + margin.toInt()
            )

            val canvasWidth = size.width
            val canvasHeight = size.height
            val rectWidth = expandedRect.width()
            val rectHeight = expandedRect.height()

            val left = (canvasWidth / 2 - rectWidth / 2)
            val top = (canvasHeight / 2 - rectHeight / 2)

            // draw rectangle
            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(rectWidth.toFloat(), rectHeight.toFloat()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )

            // show text with black background
            val confidenceText = "%.2f%%".format(confidence * 100)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24.dp.toPx()
                isFakeBoldText = true
            }

            val textBounds = Rect()
            textPaint.getTextBounds("$fontName ($confidenceText)", 0, "$fontName ($confidenceText)".length, textBounds)

            val backgroundRectWidth = textBounds.width() + 16.dp.toPx()
            val backgroundRectHeight = textBounds.height() + 8.dp.toPx()
            val backgroundLeft = left - 8.dp.toPx()
            val backgroundTop = top - textBounds.height() - 16.dp.toPx()

            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(backgroundLeft, backgroundTop),
                size = androidx.compose.ui.geometry.Size(backgroundRectWidth, backgroundRectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                "$fontName ($confidenceText)",
                left,
                top - 10.dp.toPx(),
                textPaint
            )
        }
    }
}
