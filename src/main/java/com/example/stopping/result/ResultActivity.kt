package com.example.stopping.result

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val stalkingType = intent.getStringExtra("stalkingType") ?: "분석 결과 없음"
        val checkboxStates = listOf(
            intent.getBooleanExtra("physicalApproachOrContact", false),
            intent.getBooleanExtra("harassmentViaCommunicationOrItems", false),
            intent.getBooleanExtra("invasionOfPrivacyOrIdentity", false),
            intent.getBooleanExtra("persistenceOrRepetition", false)
        )

        findViewById<TextView>(R.id.tvType).text = stalkingType
        findViewById<CheckBox>(R.id.checkbox1).isChecked = checkboxStates[0]
        findViewById<CheckBox>(R.id.checkbox2).isChecked = checkboxStates[1]
        findViewById<CheckBox>(R.id.checkbox3).isChecked = checkboxStates[2]
        findViewById<CheckBox>(R.id.checkbox4).isChecked = checkboxStates[3]

        val precedentTitle = intent.getStringExtra("precedentTitle") ?: "-"
        val precedentContent = intent.getStringExtra("precedentContent") ?: "-"
        val precedentSimilarity = intent.getStringExtra("precedentSimilarity") ?: "-"

        val tvName = findViewById<TextView>(R.id.tvName)
        val underlinedTitle = SpannableString(precedentTitle).apply {
            setSpan(UnderlineSpan(), 0, precedentTitle.length, 0)
        }
        tvName.text = underlinedTitle

        findViewById<TextView>(R.id.tvContent).text = precedentContent
        findViewById<TextView>(R.id.tvPer).text = precedentSimilarity


    }
}