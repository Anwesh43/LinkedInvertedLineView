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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class ILNode(var i : Int, val state : State = State()) {

        private var next : ILNode? = null
        private var prev : ILNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = ILNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawILNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : ILNode {
            var curr : ILNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class InvertedLine(var i : Int) {

        private var curr : ILNode = ILNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir, {
                    dir *= -1
                })
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : InvertedLineView) {

        private val animator : Animator = Animator(view)
        private val il : InvertedLine = InvertedLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            il.draw(canvas, paint)
            animator.animate {
                il.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            il.startUpdating {
                animator.start()
            }
        }

    }
}