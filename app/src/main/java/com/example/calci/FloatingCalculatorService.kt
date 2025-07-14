package com.example.voicecalculator

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import android.view.WindowManager.LayoutParams
import android.view.View.OnTouchListener

class FloatingCalculatorService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var layoutParams: LayoutParams
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.layout_floating_calc, null)

        val typeFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            LayoutParams.TYPE_PHONE

        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            typeFlag,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 100
        layoutParams.y = 300

        setupFloatingCalculator()
        windowManager.addView(floatingView, layoutParams)

        // Dragging the floating view
        val rootView = floatingView.findViewById<LinearLayout>(R.id.floatingRoot)
        rootView.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                else -> false
            }
        })
    }

    private fun setupFloatingCalculator() {
        val input = floatingView.findViewById<EditText>(R.id.etExpression)
        val result = floatingView.findViewById<TextView>(R.id.tvMiniResult)
        val btnEqual = floatingView.findViewById<Button>(R.id.btnMiniEqual)
        val btnClose = floatingView.findViewById<ImageButton>(R.id.btnMiniClose)

        btnEqual.setOnClickListener {
            try {
                val expr = input.text.toString()
                    .replace("π", Math.PI.toString())
                    .replace("e", Math.E.toString())
                val value = net.objecthunter.exp4j.ExpressionBuilder(expr).build().evaluate()
                result.text = "= $value"
            } catch (e: Exception) {
                result.text = "Error"
            }
        }

        btnClose.setOnClickListener {
            stopSelf()
        }
    }

    override fun onDestroy() {
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        super.onDestroy()
    }
}
