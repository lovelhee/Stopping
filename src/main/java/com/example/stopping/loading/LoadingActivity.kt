package com.example.stopping.loading

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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class LoadingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LOADING_API"
        private const val BASE_URL = "https://30b1-164-125-221-234.ngrok-free.app"
        private const val TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkcmVhbWttczIwMTRAZ21haWwuY29tIiwiaWF0IjoxNzQ4MTA2NDcxLCJleHAiOjE3NDgxNzg0NzF9.TFQwW0r2WpHO_wV3m8IXWhxeTmqTLquclkTVkfWT0PI"
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
        callAiApi(userInput)
    }

    private fun callAiApi(prompt: String) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = "$BASE_URL/chat?prompt=$encodedPrompt"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "API 호출 실패", e)
                // TODO: 실패 시 사용자 알림 처리 추가 가능
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "응답 오류 코드: ${it.code}")
                        return
                    }

                    val body = it.body?.string()
                    if (body != null) {
                        try {
                            val json = JSONObject(body)
                            val intent = Intent(this@LoadingActivity, ResultActivity::class.java).apply {
                                putExtra("stalkingType", json.getString("stalkingType"))
                                putExtra("physicalApproachOrContact", json.getBoolean("physicalApproachOrContact"))
                                putExtra("harassmentViaCommunicationOrItems", json.getBoolean("harassmentViaCommunicationOrItems"))
                                putExtra("invasionOfPrivacyOrIdentity", json.getBoolean("invasionOfPrivacyOrIdentity"))
                                putExtra("persistenceOrRepetition", json.getBoolean("persistenceOrRepetition"))
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