package jp.techacademy.huyen.duong.qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.qa_app.databinding.ActivityFavoriteQuestionBinding
import jp.techacademy.huyen.duong.qa_app.databinding.ActivityMainBinding

class FavoriteQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteQuestionBinding
    private var genre = 0  // 追加

    // ----- 追加:ここから -----
    private lateinit var databaseReference: DatabaseReference
    private lateinit var questionArrayList: ArrayList<Question>
    private lateinit var adapter: QuestionsListAdapter

    private var genreRef: DatabaseReference? = null

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>
            val qid = map["title"] as? String ?: ""
            val favoriteStatus = map["favoriteStatus"] as? String ?: "0"
            Log.d("Test",qid+": "+favoriteStatus)
            if (favoriteStatus.toInt() == 1) {
                val title = map["title"] as? String ?: ""
                val body = map["body"] as? String ?: ""
                val name = map["name"] as? String ?: ""
                val uid = map["uid"] as? String ?: ""
                val imageString = map["image"] as? String ?: ""
                val bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val answerArrayList = ArrayList<Answer>()
                val answerMap = map["answers"] as Map<*, *>?
                if (answerMap != null) {
                    for (key in answerMap.keys) {
                        val map1 = answerMap[key] as Map<*, *>
                        val map1Body = map1["body"] as? String ?: ""
                        val map1Name = map1["name"] as? String ?: ""
                        val map1Uid = map1["uid"] as? String ?: ""
                        val map1AnswerUid = key as? String ?: ""
                        val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                        answerArrayList.add(answer)
                    }
                }

                val question = Question(
                    favoriteStatus.toInt(),
                    title, body, name, uid, dataSnapshot.key ?: "",
                    genre, bytes, answerArrayList
                )
                questionArrayList.add(question)
                Log.d("Favorite List",""+questionArrayList.size)
                adapter.notifyDataSetChanged()
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            // 変更があったQuestionを探す
            for (question in questionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<*, *>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val map1 = answerMap[key] as Map<*, *>
                            val map1Body = map1["body"] as? String ?: ""
                            val map1Name = map1["name"] as? String ?: ""
                            val map1Uid = map1["uid"] as? String ?: ""
                            val map1AnswerUid = key as? String ?: ""
                            val answer = Answer(map1Body, map1Name, map1Uid, map1AnswerUid)
                            question.answers.add(answer)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {}
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        override fun onCancelled(p0: DatabaseError) {}
    }
    // ----- 追加:ここまで -----
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().reference

//        // ListViewの準備
        adapter = QuestionsListAdapter(this)
        questionArrayList = ArrayList()
//        //adapter.notifyDataSetChanged()
       // questionArrayList.clear()
       adapter.setQuestionArrayList(questionArrayList)
        binding.content.listView.adapter = adapter

        if (genreRef != null) {
            genreRef!!.removeEventListener(eventListener)
        }
        for (i in 1..4) {
            genre = i
            genreRef = databaseReference.child(ContentsPATH).child(i.toString())
            genreRef!!.addChildEventListener(eventListener)
        }
        Log.d("List favorite test ",""+questionArrayList.size)
        // ----- 追加:ここまで -----
    }

    override fun onResume() {
        super.onResume()
        // ----- 追加:ここから -----
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
//        questionArrayList.clear()
//        adapter.setQuestionArrayList(questionArrayList)
//        binding.content.listView.adapter = adapter
//
//        // 選択したジャンルにリスナーを登録する
//        if (genreRef != null) {
//            genreRef!!.removeEventListener(eventListener)
//        }
//        for (i in 1..4) {
//            genre = i
//            genreRef = databaseReference.child(ContentsPATH).child(i.toString())
//            genreRef!!.addChildEventListener(eventListener)
//        }
        // ----- 追加:ここまで -----
    }
}