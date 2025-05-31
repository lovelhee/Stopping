package com.example.stopping.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stopping.R

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: PlaceInfoAdapter
    private lateinit var placeList: List<PlaceInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        placeList = listOf(
            PlaceInfo("피해자지원 전담 경찰관", "밀양", "경남 밀양시 상남면 밀양대로 1545 밀양경찰서 수사과 수사지원팀", "0553500201"),
            PlaceInfo("피해자 지원실", "창원지방검찰청밀양지청", "(우)50424 경상남도 밀양시 밀양대로 1993-20", "0553504564"),
            PlaceInfo("범죄피해자지원센터", "밀양・창녕 범죄피해자지원센터", "경남 밀양시 밀양대로 1993-20 창원지방검찰청 밀양지청 21호", "0553568272"),
            PlaceInfo("성폭력상담소", "밀양시성가족상담소", "경남 밀양시 중앙로 268 (삼문동, 여성회관)", "0553911366"),
            PlaceInfo("대한법률구조공단", "대한법률구조공단 밀양출장소", "504-19 경남 밀양시 시청서길 15, 화승빌딩 (구, 송림빌딩 4층)", "0553565131")
        )

        adapter = PlaceInfoAdapter(placeList)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerInfo)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val searchEdit = findViewById<EditText>(R.id.etSearch)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this@ChatActivity, com.example.stopping.home.HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()  // 현재 Activity 종료
        }

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                val filtered = placeList.filter { place ->
                    place.title.contains(query, ignoreCase = true)
                }
                adapter.filterList(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 생략 가능
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 생략 가능
            }
        })

    }
}
