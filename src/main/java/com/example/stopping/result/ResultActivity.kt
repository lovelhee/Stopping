package com.example.stopping.result

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R
import com.example.stopping.personal.PersonalInfoActivity
import com.example.stopping.type.TypeActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getString("userId", null)?.toIntOrNull() ?: return
        val token = prefs.getString("jwt", null) ?: return

        val stalkingType = intent.getStringExtra("stalkingType") ?: "분석 결과 없음"
        val checkboxStates = listOf(
            intent.getBooleanExtra("physicalApproachOrContact", false),
            intent.getBooleanExtra("harassmentViaCommunicationOrItems", false),
            intent.getBooleanExtra("invasionOfPrivacyOrIdentity", false),
            intent.getBooleanExtra("persistenceOrRepetition", false)
        )

        val tvType = findViewById<TextView>(R.id.tvType)
        tvType.text = stalkingType

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

        val layoutType = findViewById<FrameLayout>(R.id.layoutType)

        layoutType.setOnClickListener {
            val intent = Intent(this, TypeActivity::class.java)
            intent.putExtra("stalkingType", tvType.text.toString())
            startActivity(intent)
        }

        val supportIds = intent.getIntegerArrayListExtra("support_ids") ?: arrayListOf()
        val layoutPersonal = findViewById<FrameLayout>(R.id.layoutPersonal)

        layoutPersonal.setOnClickListener {
            saveSupportResults(userId, supportIds)

            val intent = Intent(this, PersonalInfoActivity::class.java)
            if (supportIds.isNotEmpty()) {
                intent.putIntegerArrayListExtra("support_ids", supportIds)
            }
            startActivity(intent)
        }

    }

    private fun saveSupportResults(userId: Int, supportIds: List<Int>) {
        val token = getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", null)

        Log.d("SAVE_API", "전송할 토큰: $token")

        val json = JSONObject().apply {
            put("id", userId)
            put("supportIdList", JSONArray(supportIds))
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://d096-164-125-221-234.ngrok-free.app/users/supports")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SAVE_API", "결과 저장 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("SAVE_API", "지원제도 저장완료")
                } else {
                    Log.e("SAVE_API", "저장 실패: ${response.code}")
                }
            }
        })
    }

}