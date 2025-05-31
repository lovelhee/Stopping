package com.example.stopping.loading

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R
import com.example.stopping.result.ResultActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class LoadingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LOADING_API"
        private const val BASE_URL = "https://d096-164-125-221-234.ngrok-free.app"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userInput = intent.getStringExtra("user_input") ?: ""
        val supportIds = intent.getIntegerArrayListExtra("support_ids") ?: arrayListOf()

        val token = getJwtToken()
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        callAiApi(token, userInput, supportIds)
    }

    private fun getJwtToken(): String? {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("jwt", null)
    }

    private fun callAiApi(token: String, prompt: String, supportIds: ArrayList<Int>) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = "$BASE_URL/chat?prompt=$encodedPrompt"

        val request1 = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "AI API 호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "AI 응답 오류 코드: ${it.code}")
                        return
                    }

                    val body1 = it.body?.string()
                    if (body1 != null) {
                        val json1 = JSONObject(body1)
                        callPrecedentApi(token, prompt, json1, supportIds)
                    }
                }
            }
        })
    }

    private fun callPrecedentApi(token: String, prompt: String, json1: JSONObject, supportIds: ArrayList<Int>) {
        val url = "$BASE_URL/search/simple"
        val jsonBody = JSONObject().apply {
            put("question", prompt)
        }

        Log.d(TAG, "보내는 판례 질문: ${jsonBody}")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)

        val request2 = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request2).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "판례 API 호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = it.body?.string()
                        Log.e(TAG, "판례 응답 오류: ${it.code} / 내용: $errorBody")
                        return
                    }

                    val body2 = it.body?.string()
                    if (body2 != null) {
                        try {
                            val precedentArray = JSONArray(body2)
                            val precedent = precedentArray.getJSONObject(0)

                            val intent = Intent(this@LoadingActivity, ResultActivity::class.java).apply {
                                putExtra("stalkingType", json1.getString("stalkingType"))
                                putExtra("physicalApproachOrContact", json1.getBoolean("physicalApproachOrContact"))
                                putExtra("harassmentViaCommunicationOrItems", json1.getBoolean("harassmentViaCommunicationOrItems"))
                                putExtra("invasionOfPrivacyOrIdentity", json1.getBoolean("invasionOfPrivacyOrIdentity"))
                                putExtra("persistenceOrRepetition", json1.getBoolean("persistenceOrRepetition"))

                                putExtra("precedentTitle", precedent.getString("title"))
                                putExtra("precedentContent", precedent.getString("content"))
                                putExtra("precedentSimilarity", precedent.getString("similarity"))

                                putIntegerArrayListExtra("support_ids", supportIds)
                            }

                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            Log.e(TAG, "JSON 파싱 오류", e)
                        }
                    }
                }
            }
        })
    }
}