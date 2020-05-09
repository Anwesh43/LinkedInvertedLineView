package com.anwesh.uiprojects.invertedlineview

/**
 * Created by anweshmishra on 10/05/20.
 */

import android.view.View
import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.view.MotionEvent
import android.app.Activity

val nodes : Int = 5
val lines : Int = 2
val parts : Int = 2
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#212121")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawInvertedLine(i : Int, scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sfi : Float = sf.divideScale(i, parts)
    for (j in 0..(lines - 1)) {
        val sfij : Float = sfi.divideScale(j, lines)
        val x : Float = size * (1 - j) * sfij
        val y : Float = size * j * sfij
        drawLine(size * i, 0f, x, y, paint)
    }
}

fun Canvas.drawInvertedLines(scale : Float, size : Float, paint : Paint) {
    for (i in 0..(parts - 1)) {
        save()
        scale(1f - 2 * i, 1f - 2 * i)
        drawInvertedLine(i, scale, size, paint)
        restore()
    }
}

fun Canvas.drawILNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawInvertedLines(scale, size, paint)
    restore()
}

class InvertedLineView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}