package com.example.stopping.input

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R
import com.example.stopping.home.HomeActivity
import com.example.stopping.loading.LoadingActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class InputSecondActivity : AppCompatActivity() {

    private lateinit var layoutBtn: LinearLayout
    private lateinit var imgBtnBack: ImageButton
    private lateinit var checkGroups: List<List<CheckBox>>
    private lateinit var userInput: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_second)

        userInput = intent.getStringExtra("user_input") ?: ""

        layoutBtn = findViewById(R.id.layoutBtn)
        imgBtnBack = findViewById(R.id.imgBtnBack)

        checkGroups = listOf(
            listOf(findViewById(R.id.check1), findViewById(R.id.check2), findViewById(R.id.check3)),
            listOf(findViewById(R.id.check4), findViewById(R.id.check5), findViewById(R.id.check6)),
            listOf(findViewById(R.id.check7), findViewById(R.id.check8), findViewById(R.id.check9)),
            listOf(findViewById(R.id.check10), findViewById(R.id.check11), findViewById(R.id.check12)),
            listOf(findViewById(R.id.check13), findViewById(R.id.check14), findViewById(R.id.check15)),
            listOf(findViewById(R.id.check16), findViewById(R.id.check17), findViewById(R.id.check18)),
            listOf(findViewById(R.id.check19), findViewById(R.id.check20), findViewById(R.id.check21)),
            listOf(findViewById(R.id.check22), findViewById(R.id.check23), findViewById(R.id.check24)),
        )

        checkGroups.flatten().forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                updateButtonState()
            }
        }

        layoutBtn.setOnClickListener {
            if (isButtonActive()) {
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("user_input", userInput)
                startActivity(intent)
            }
        }

        imgBtnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        updateButtonState()
    }

    private fun isButtonActive(): Boolean {
        return checkGroups.all { group ->
            group.any { it.isChecked }
        }
    }

    private fun updateButtonState() {
        val isActive = isButtonActive()
        layoutBtn.setBackgroundColor(
            Color.parseColor(if (isActive) "#FFD9D9" else "#D9D9D9")
        )
        val textView = layoutBtn.getChildAt(0) as? TextView
        textView?.setTextColor(
            if (isActive) Color.BLACK else Color.WHITE
        )
    }
}