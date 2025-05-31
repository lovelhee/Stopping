package com.example.stopping.login

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stopping.R
import com.example.stopping.home.HomeActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import androidx.appcompat.app.AlertDialog
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvLogo = findViewById<TextView>(R.id.tvLogo)
        tvLogo.text = Html.fromHtml("STOPP<font color='#E48181'>!</font>ng", Html.FROM_HTML_MODE_LEGACY)

        val loginBtn = findViewById<ImageButton>(R.id.imgBtnLogin)
        loginBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnMember = findViewById<Button>(R.id.btnMember)
        btnMember.setOnClickListener {
            showSignupDialog()
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etId = findViewById<EditText>(R.id.etId)
        val etPwd = findViewById<EditText>(R.id.etPwd)

        btnLogin.setOnClickListener {
            val email = etId.text.toString()
            val password = etPwd.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
            val request = Request.Builder()
                .url("https://d096-164-125-221-234.ngrok-free.app/login")
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val token = json.getString("token") // ← 여기서 파싱!
                        val userId = json.getString("id") // ← id 받아오기

                        Log.d("LOGIN_RESPONSE", "원본 응답: $body")

                        Log.d("LOGIN_RESPONSE", "받아온 사용자 ID: $userId")
                        Log.d("LOGIN_RESPONSE", "받아온 토큰: $token")

                        // SharedPreferences 저장
                        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                        prefs.edit().putString("jwt", token).apply()

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "로그인 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        }
    }

    private fun showSignupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_signup, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnSignup = dialogView.findViewById<Button>(R.id.btnSignup)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSignup.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
            val request = Request.Builder()
                .url("https://d096-164-125-221-234.ngrok-free.app/sign")
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this@LoginActivity, "회원가입 실패: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
        dialog.show()
    }
}