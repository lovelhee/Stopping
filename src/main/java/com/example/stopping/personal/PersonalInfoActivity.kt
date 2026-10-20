package com.example.stopping.personal

import com.example.stopping.BuildConfig
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.ImageButton
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
    private fun fetchSupportInfo(id: Int) {
        val request = Request.Builder()
            .url("https://9fcf-58-72-42-92.ngrok-free.app/supports/$id")
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
        val request = Request.Builder()
            .url("https://9fcf-58-72-42-92.ngrok-free.app/details/$id")
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()

        Log.d("CHECK_DETAIL", "상세정보 조회 id: $id")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DETAIL_API", "상세 정보 호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("DETAIL_API", "오류 코드: ${response.code}")
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


    companion object {
        private val TOKEN = BuildConfig.API_TOKEN
    }
}