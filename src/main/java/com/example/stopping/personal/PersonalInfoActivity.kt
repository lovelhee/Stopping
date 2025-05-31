package com.example.stopping.personal

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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

    companion object {
        private const val TAG = "PERSONAL_API"
        private const val BASE_URL = "https://d096-164-125-221-234.ngrok-free.app"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        tvLogo.text = Html.fromHtml("STOPP<font color='#E48181'>!</font>ng", Html.FROM_HTML_MODE_LEGACY)

        val supportIds = intent.getIntegerArrayListExtra("support_ids") ?: arrayListOf()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerInfo)
        adapter = PersonalInfoAdapter(supportList, ::showDetailDialog)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@PersonalInfoActivity, com.example.stopping.result.ResultActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        supportIds.forEach { id ->
            fetchSupportInfo(id)
        }
    }

    private fun getJwtToken(): String? {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("jwt", null)
    }

    private fun fetchSupportInfo(id: Int) {

        val token = getJwtToken() ?: run {
            Toast.makeText(this, "토큰이 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = Request.Builder()
            .url("$BASE_URL/supports/$id")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "지원정보 호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "지원정보 응답 오류: ${response.code}")
                    return
                }

                val body = response.body?.string()
                body?.let {
                    val json = JSONObject(it)
                    val info = SupportInfo(
                        id = id,
                        category = json.getString("category"),
                        programName = json.getString("programName"),
                        description = json.getString("description"),
                        contact = json.getString("contact")
                    )

                    Log.d("CHECK_ID", "지원제도 id: $id")

                    runOnUiThread {
                        supportList.add(info)
                        adapter.notifyItemInserted(supportList.size - 1)
                    }
                }
            }
        })
    }

    private fun showDetailDialog(id: Int) {

        val token = getJwtToken() ?: run {
            Toast.makeText(this, "토큰이 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = Request.Builder()
            .url("$BASE_URL/details/$id")
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d("CHECK_DETAIL", "상세정보 조회 id: $id")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DETAIL_API", "상세 정보 호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("DETAIL_API", "상세 정보 응답 오류: ${response.code}")
                    return
                }

                val body = response.body?.string() ?: return
                val json = JSONObject(body)

                runOnUiThread {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_detail_info, null)
                    val dialog = android.app.AlertDialog.Builder(this@PersonalInfoActivity)
                        .setView(dialogView)
                        .create()

                    dialogView.findViewById<TextView>(R.id.tvNameData).text = json.getString("programName")
                    dialogView.findViewById<TextView>(R.id.tvIntData).text = json.getString("description")
                    dialogView.findViewById<TextView>(R.id.tvWhoData).text = json.getString("target")
                    dialogView.findViewById<TextView>(R.id.tvContentData).text = json.getString("content")
                    dialogView.findViewById<TextView>(R.id.tvProcessData).text = json.getString("procedureInfo")
                    dialogView.findViewById<TextView>(R.id.tvAskData).text = json.getString("contact")

                    dialogView.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()
                }
            }
        })
    }
}