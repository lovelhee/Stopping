package com.example.stopping.personal

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stopping.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PersonalInfoActivity : AppCompatActivity() {

    private val supportList = mutableListOf<SupportInfo>()
    private lateinit var adapter: PersonalInfoAdapter
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        tvLogo.text = Html.fromHtml("STOPP<font color='#E48181'>!</font>ng", Html.FROM_HTML_MODE_LEGACY)

        val supportIds = intent.getIntegerArrayListExtra("support_ids") ?: arrayListOf()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerInfo)
        adapter = PersonalInfoAdapter(supportList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        supportIds.forEach { id ->
            fetchSupportInfo(id)
        }
    }
    private fun fetchSupportInfo(id: Int) {
        val request = Request.Builder()
            .url("https://30b1-164-125-221-234.ngrok-free.app/supports/$id")
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SUPPORT_API", "호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("SUPPORT_API", "오류 코드: ${response.code}")
                    return
                }

                val body = response.body?.string()
                body?.let {
                    val json = JSONObject(it)
                    val info = SupportInfo(
                        category = json.getString("category"),
                        programName = json.getString("programName"),
                        description = json.getString("description"),
                        contact = json.getString("contact")
                    )

                    runOnUiThread {
                        supportList.add(info)
                        adapter.notifyItemInserted(supportList.size - 1)
                    }
                }
            }
        })
    }

    companion object {
        private const val TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkcmVhbWttczIwMTRAZ21haWwuY29tIiwiaWF0IjoxNzQ4MTEyNjQ4LCJleHAiOjE3NDgxODQ2NDh9.8lDN8SJJSbscpSAagMVZgZLMPGkuKXHD7blSTwAKLRE" // 너의 토큰 그대로 써줘!
    }
}