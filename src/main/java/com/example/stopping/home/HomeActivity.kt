package com.example.stopping.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R
import com.example.stopping.chat.ChatActivity
import com.example.stopping.input.InputFirstActivity
import com.example.stopping.personal.PersonalInfoActivity
import com.example.stopping.type.TypeActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        tvLogo.text = Html.fromHtml(
            "STOPP<font color='#E48181'>!</font>ng",
            Html.FROM_HTML_MODE_LEGACY
        )

        val layoutChat = findViewById<FrameLayout>(R.id.layoutChat)
        layoutChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        val tvNews = findViewById<TextView>(R.id.tvNews)
        val dailyNews = "'접근 금지'에도 전 여친에게 116회 연락한 20대의 최후"
        val underlined = SpannableString(dailyNews)
        underlined.setSpan(UnderlineSpan(), 0, dailyNews.length, 0)
        tvNews.text = underlined
        tvNews.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dailian.co.kr/news/view/1502291/?sc=Naver"))
            startActivity(intent)
        }

        findViewById<View>(R.id.layoutMainBtn).setOnClickListener {
            val intent = Intent(this, InputFirstActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.layoutTest).setOnClickListener {
            val intent = Intent(this, InputFirstActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.layoutInfo).setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.layoutSol).setOnClickListener {
            val intent = Intent(this, TypeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}