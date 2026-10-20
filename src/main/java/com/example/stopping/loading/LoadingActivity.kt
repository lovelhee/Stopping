package com.example.stopping.loading

import com.example.stopping.BuildConfig
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        private const val BASE_URL = "https://9fcf-58-72-42-92.ngrok-free.app"
        private val TOKEN = BuildConfig.API_TOKEN
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

        callAiApi(userInput, supportIds)
    }

    private fun callAiApi(prompt: String, supportIds: ArrayList<Int>) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = "$BASE_URL/chat?prompt=$encodedPrompt"

        val request1 = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()

        val client = OkHttpClient()

        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "API 호출 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "응답 오류 코드: ${it.code}")
                        return
                    }

                    val body1 = it.body?.string()
                    if (body1 != null) {
                        val json1 = JSONObject(body1)
                        // 1차 분석 데이터 파싱 완료 → 2차 판례 API 호출
                        callPrecedentApi(prompt, json1, supportIds)
                    }
                }
            }
        })
    }
    private fun callPrecedentApi(prompt: String, json1: JSONObject, supportIds: ArrayList<Int>) {
        val url = "$BASE_URL/search/simple"
        val jsonBody = JSONObject()
        jsonBody.put("question", prompt)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)

        val request2 = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()

        val client = OkHttpClient()

        client.newCall(request2).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "판례 API 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "판례 응답 오류: ${it.code}")
                        return
                    }

                    val body2 = it.body?.string()
                    if (body2 != null) {
                        try {
                            val precedentArray = JSONArray(body2)
                            val precedent = precedentArray.getJSONObject(0)

                            // 두 API 결과를 합쳐서 결과 화면으로 전달
                            val intent = Intent(this@LoadingActivity, ResultActivity::class.java).apply {
                                // 분석 결과
                                putExtra("stalkingType", json1.getString("stalkingType"))
                                putExtra("physicalApproachOrContact", json1.getBoolean("physicalApproachOrContact"))
                                putExtra("harassmentViaCommunicationOrItems", json1.getBoolean("harassmentViaCommunicationOrItems"))
                                putExtra("invasionOfPrivacyOrIdentity", json1.getBoolean("invasionOfPrivacyOrIdentity"))
                                putExtra("persistenceOrRepetition", json1.getBoolean("persistenceOrRepetition"))

                                // 판례 결과
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