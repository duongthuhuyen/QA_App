package jp.techacademy.huyen.duong.qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
            val quid = dataSnapshot?.key
            if(quid != null) {
            val mapp = dataSnapshot.child(quid)
            val map = mapp?.value as Map<*, *>
            if(map != null) {
                val qid = map["quid"] as? String ?: ""
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

                val question = Question(
                    title, body, name, uid, qid,
                    genre, bytes, answerArrayList
                )
                questionArrayList.add(question)
                Log.d("Favorite List", "" + questionArrayList.size)
                adapter.notifyDataSetChanged()
            }}
            }


        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
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
       adapter.setQuestionArrayList(questionArrayList)
        binding.content.listView.adapter = adapter

        if (genreRef != null) {
            genreRef!!.removeEventListener(eventListener)
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            var userId = user.uid
            genreRef = databaseReference.child(FavoritePATH).child(userId)
            genreRef!!.addChildEventListener(eventListener)
        }

        // ----- 追加:ここまで -----
    }
}