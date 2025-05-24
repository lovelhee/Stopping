package com.example.stopping.input

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R
import com.example.stopping.home.HomeActivity

class InputFirstActivity : AppCompatActivity() {

    private lateinit var etContent: EditText
    private lateinit var layoutBtn: LinearLayout
    private var isBtnActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_first)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etContent = findViewById(R.id.etContent)
        layoutBtn = findViewById(R.id.layoutBtn)

        findViewById<ImageButton>(R.id.imgBtnBack).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val content = s.toString().trim()
                isBtnActive = content.isNotEmpty()

                layoutBtn.setBackgroundColor(
                    if (isBtnActive) Color.parseColor("#FFD9D9")
                    else ContextCompat.getColor(this@InputFirstActivity, R.color.gray_d9)
                )

                val textView = layoutBtn.getChildAt(0) as? TextView
                textView?.setTextColor(
                    if (isBtnActive) Color.BLACK
                    else Color.WHITE
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        layoutBtn.setOnClickListener {
            if (isBtnActive) {
                val intent = Intent(this, InputSecondActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}