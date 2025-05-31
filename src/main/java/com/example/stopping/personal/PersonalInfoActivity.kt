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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
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

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getString("userId", null)?.toIntOrNull() ?: return
        val token = prefs.getString("jwt", null) ?: return

        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        tvLogo.text = Html.fromHtml("STOPP<font color='#E48181'>!</font>ng", Html.FROM_HTML_MODE_LEGACY)

        val supportIds = intent.getIntegerArrayListExtra("support_ids")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerInfo)
        adapter = PersonalInfoAdapter(supportList, ::showDetailDialog)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (!supportIds.isNullOrEmpty()) {
            Log.d(TAG, "support_ids 전달됨 → 개별 조회 및 저장")
            saveSupportResults(userId, supportIds, token)
            supportIds.forEach { id -> fetchSupportInfo(id, token) }
        } else {
            Log.d(TAG, "support_ids 없음 → 저장된 리스트 조회")
            loadSavedSupportList(userId, token)
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@PersonalInfoActivity, com.example.stopping.home.HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun getJwtToken(): String? {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("jwt", null)
    }

    private fun fetchSupportInfo(id: Int, token: String) {

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

    private fun saveSupportResults(userId: Int, supportIds: List<Int>, token: String) {
        val json = JSONObject().apply {
            put("id", userId)
            put("supportIdList", JSONArray(supportIds))
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/users/supports")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SAVE_API", "저장 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("SAVE_API", "저장 실패: ${response.code}")
                } else {
                    Log.d("SAVE_API", "지원제도 저장 성공")
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
            .url("$BASE_URL/details/$id") // 🔁 여기만 수정
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d("CHECK_DETAIL", "상세정보 조회 id: $id")
        Log.d("DETAIL_API", "요청할 상세 id: $id, 토큰: $token")


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
                Log.d("DETAIL_API", "상세 응답 바디: $body")

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

    private fun loadSavedSupportList(userId: Int, token: String) {
        val request = Request.Builder()
            .url("$BASE_URL/users/supports/$userId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LOAD_API", "이전 결과 로딩 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("LOAD_API", "로딩 오류: ${response.code}")
                    return
                }

                val jsonArray = JSONArray(response.body?.string())
                supportList.clear()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    supportList.add(
                        SupportInfo(
                            id = item.getInt("id"),
                            category = item.getString("category"),
                            programName = item.getString("programName"),
                            description = item.getString("description"),
                            contact = item.getString("contact")
                        )
                    )
                }
                runOnUiThread { adapter.notifyDataSetChanged() }
            }
        })
    }

}