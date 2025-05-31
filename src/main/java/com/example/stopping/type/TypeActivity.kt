package com.example.stopping.type

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R

class TypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_type)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val type = intent.getStringExtra("stalkingType") ?: ""

        val fragment = when (type) {
            "오프라인 직접 스토킹" -> OfflineFragment()
            "물건 전달형 스토킹" -> GiftFragment()
            "통신/사이버 스토킹" -> CyberFragment()
            "개인정보 악용형 스토킹" -> InfoFragment()
            "디지털 성적 스토킹" -> DigitalFragment()
            else -> OfflineFragment() // 기본 fallback
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        tvLogo.text = Html.fromHtml("STOPP<font color='#E48181'>!</font>ng", Html.FROM_HTML_MODE_LEGACY)
    }
}