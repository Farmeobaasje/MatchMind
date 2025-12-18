package com.Lyno.matchmindai.presentation.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

/**
 * Utility class voor het renderen van statistische charts naar Bitmaps.
 * Omdat Jetpack Glance geen custom drawing ondersteunt, renderen we
 * charts off-screen naar een Bitmap die we dan in de widget kunnen tonen.
 */
class ChartRenderer(private val context: Context) {

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val possessionHomePaint = Paint().apply {
        color = Color.parseColor("#4CAF50") // Groen
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val possessionAwayPaint = Paint().apply {
        color = Color.parseColor("#F44336") // Rood
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val shotsPaint = Paint().apply {
        color = Color.parseColor("#2196F3") // Blauw
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val cornersPaint = Paint().apply {
        color = Color.parseColor("#FF9800") // Oranje
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#1E2230") // SurfaceCard
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#2D3548")
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    /**
     * Creëert een balbezit balk als Bitmap.
     *
     * @param homePercentage Percentage balbezit thuisploeg (0-100)
     * @param awayPercentage Percentage balbezit uitploeg (0-100)
     * @param widthPx Breedte van de bitmap in pixels
     * @param heightPx Hoogte van de bitmap in pixels
     * @return Bitmap met de balbezit visualisatie
     */
    fun createPossessionBar(
        homePercentage: Int,
        awayPercentage: Int,
        widthPx: Int = 300,
        heightPx: Int = 40
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Bereken splitsingspunt
        val total = homePercentage + awayPercentage
        val splitX = if (total > 0) {
            (homePercentage.toFloat() / total) * widthPx
        } else {
            widthPx / 2f
        }

        // Teken achtergrond
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), backgroundPaint)

        // Teken thuis balk
        canvas.drawRect(0f, 0f, splitX, heightPx.toFloat(), possessionHomePaint)

        // Teken uit balk
        canvas.drawRect(splitX, 0f, widthPx.toFloat(), heightPx.toFloat(), possessionAwayPaint)

        // Teken border
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), borderPaint)

        // Teken percentages
        val homeText = "$homePercentage%"
        val awayText = "$awayPercentage%"

        textPaint.textSize = 14f
        textPaint.color = Color.WHITE

        // Thuis percentage
        canvas.drawText(
            homeText,
            splitX / 2f,
            heightPx / 2f + 5,
            textPaint
        )

        // Uit percentage
        canvas.drawText(
            awayText,
            splitX + (widthPx - splitX) / 2f,
            heightPx / 2f + 5,
            textPaint
        )

        return bitmap
    }

    /**
     * Creëert een staafdiagram voor schoten op doel.
     *
     * @param homeShots Aantal schoten thuisploeg
     * @param awayShots Aantal schoten uitploeg
     * @param widthPx Breedte van de bitmap in pixels
     * @param heightPx Hoogte van de bitmap in pixels
     * @return Bitmap met het staafdiagram
     */
    fun createShotsChart(
        homeShots: Int,
        awayShots: Int,
        widthPx: Int = 200,
        heightPx: Int = 80
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Bereken maximale waarde voor schaal
        val maxShots = maxOf(homeShots, awayShots, 1)
        val barWidth = (widthPx * 0.35f).toInt()
        val spacing = (widthPx * 0.1f).toInt()

        // Teken achtergrond
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), backgroundPaint)

        // Bereken bar hoogtes
        val homeBarHeight = (homeShots.toFloat() / maxShots) * (heightPx * 0.7f)
        val awayBarHeight = (awayShots.toFloat() / maxShots) * (heightPx * 0.7f)

        // Teken thuis bar
        val homeX = spacing.toFloat()
        val homeY = heightPx - homeBarHeight - 10
        canvas.drawRect(
            homeX,
            homeY,
            homeX + barWidth,
            heightPx - 10f,
            shotsPaint
        )

        // Teken uit bar
        val awayX = (spacing * 2 + barWidth).toFloat()
        val awayY = heightPx - awayBarHeight - 10
        canvas.drawRect(
            awayX,
            awayY,
            awayX + barWidth,
            heightPx - 10f,
            cornersPaint
        )

        // Teken labels
        textPaint.textSize = 12f
        textPaint.color = Color.WHITE

        // Thuis label
        canvas.drawText(
            "Thuis",
            homeX + barWidth / 2f,
            heightPx - 2f,
            textPaint
        )

        canvas.drawText(
            homeShots.toString(),
            homeX + barWidth / 2f,
            homeY - 5,
            textPaint
        )

        // Uit label
        canvas.drawText(
            "Uit",
            awayX + barWidth / 2f,
            heightPx - 2f,
            textPaint
        )

        canvas.drawText(
            awayShots.toString(),
            awayX + barWidth / 2f,
            awayY - 5,
            textPaint
        )

        // Teken border
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), borderPaint)

        return bitmap
    }

    /**
     * Creëert een momentum grafiek voor de wedstrijd.
     *
     * @param momentumData Lijst van momentum waarden (0-100) over tijd
     * @param widthPx Breedte van de bitmap in pixels
     * @param heightPx Hoogte van de bitmap in pixels
     * @return Bitmap met de momentum grafiek
     */
    fun createMomentumChart(
        momentumData: List<Int>,
        widthPx: Int = 250,
        heightPx: Int = 60
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (momentumData.isEmpty()) {
            // Teken placeholder
            canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), backgroundPaint)
            textPaint.textSize = 14f
            textPaint.color = Color.GRAY
            canvas.drawText(
                "Geen momentum data",
                widthPx / 2f,
                heightPx / 2f,
                textPaint
            )
            return bitmap
        }

        // Teken achtergrond
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), backgroundPaint)

        // Bereken punten voor de lijn
        val pointSpacing = widthPx.toFloat() / (momentumData.size - 1)
        val points = mutableListOf<Pair<Float, Float>>()

        for ((index, value) in momentumData.withIndex()) {
            val x = index * pointSpacing
            val y = heightPx - (value.toFloat() / 100f * (heightPx * 0.8f)) - 10
            points.add(Pair(x, y))
        }

        // Teken lijn
        val linePaint = Paint().apply {
            color = Color.parseColor("#00FF94") // PrimaryNeon
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
        }

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            canvas.drawLine(start.first, start.second, end.first, end.second, linePaint)
        }

        // Teken punten
        val pointPaint = Paint().apply {
            color = Color.parseColor("#6C5DD3") // SecondaryPurple
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (point in points) {
            canvas.drawCircle(point.first, point.second, 4f, pointPaint)
        }

        // Teken border
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), borderPaint)

        return bitmap
    }

    /**
     * Creëert een eenvoudige statistiek kaart met icon en waarde.
     *
     * @param iconText Icon tekst (emoji)
     * @param value Waarde om te tonen
     * @param label Label voor de waarde
     * @param widthPx Breedte van de bitmap in pixels
     * @param heightPx Hoogte van de bitmap in pixels
     * @return Bitmap met de statistiek kaart
     */
    fun createStatCard(
        iconText: String,
        value: String,
        label: String,
        widthPx: Int = 80,
        heightPx: Int = 60
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Teken achtergrond met afgeronde hoeken
        val rect = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
        canvas.drawRoundRect(rect, 12f, 12f, backgroundPaint)

        // Teken icon
        textPaint.textSize = 20f
        textPaint.color = Color.parseColor("#00FF94") // PrimaryNeon
        canvas.drawText(
            iconText,
            widthPx / 2f,
            heightPx * 0.35f,
            textPaint
        )

        // Teken waarde
        textPaint.textSize = 16f
        textPaint.color = Color.WHITE
        canvas.drawText(
            value,
            widthPx / 2f,
            heightPx * 0.65f,
            textPaint
        )

        // Teken label
        textPaint.textSize = 10f
        textPaint.color = Color.GRAY
        canvas.drawText(
            label,
            widthPx / 2f,
            heightPx * 0.85f,
            textPaint
        )

        // Teken border
        canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

        return bitmap
    }
}
