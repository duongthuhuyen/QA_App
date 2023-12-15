package jp.techacademy.huyen.duong.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle // 追加
import androidx.core.view.GravityCompat // 追加
import com.google.android.material.navigation.NavigationView // 追加
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.qa_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    private var genre = 0  // 追加


    // ----- 追加:ここから -----
    private lateinit var databaseReference: DatabaseReference
    private lateinit var questionArrayList: ArrayList<Question>
    private lateinit var adapter: QuestionsListAdapter

    private var genreRef: DatabaseReference? = null
    //異なるActivity間で通信
    var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val res = intent.getStringExtra(KEY_RESULT).toString()
                if (res == "OK") {
                    genre = 1
                    val navigationView = findViewById<NavigationView>(R.id.nav_view)

                    onNavigationItemSelected(navigationView.menu.getItem(0))
                }
            }
        }
    }

    private val eventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>
            //val favoriteStatus = map["favoriteStatus"] as? String ?: "0"
            //Log.d("FavoriteMain",""+favoriteStatus)
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
                title, body, name, uid, dataSnapshot.key ?: "",
                genre, bytes, answerArrayList
            )
            questionArrayList.add(question)
            adapter.notifyDataSetChanged()
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
    private val eventListenerFavorite = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val quid = dataSnapshot?.key
            if(quid != null) {
                val mapp = dataSnapshot.child(quid)

//            val quidkey = mapp?.key
//            val quisnapshot = quidkey?.let { mapp.child(it) }
                val maps = dataSnapshot?.value as? Map<*,*>
                if(maps != null) {
                    var a = maps.values
                    var map = a.first() as? HashMap<*,*>
                    //map = maps
                    val qid = map!!["quid"] as? String ?: ""
                    val title = map!!["title"] as? String ?: ""
                    val body = map!!["body"] as? String ?: ""
                    val name = map!!["name"] as? String ?: ""
                    val uid = map!!["uid"] as? String ?: ""
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.content.toolbar)

        val user = FirebaseAuth.getInstance().currentUser
        binding.navView.menu.getItem(4).setVisible(false)
        if (user != null) {
            binding.navView.menu.getItem(4).setVisible(true)
        }
        binding.content.fab.setOnClickListener {
            // ジャンルを選択していない場合はメッセージを表示するだけ
            Log.d("Genre",""+genre)
            if (genre == 0) {
                Snackbar.make(it, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            // ログインしていなければログイン画面に遷移させる
            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", genre)
                startActivity(intent)
            }
        }

        // ----- 追加:ここから
        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.content.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        // ----- 追加:ここまで
        // ----- List Question -----
        // Firebase
        databaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        adapter = QuestionsListAdapter(this)
        questionArrayList = ArrayList()
        adapter.notifyDataSetChanged()
        // ----- 追加:ここまで -----
        // ----- 追加:ここから onclick Item to view detail question-----
        binding.content.inner.listView.setOnItemClickListener { _, _, position, _ ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", questionArrayList[position])
            startActivity(intent)
        }
        // ----- 追加:ここまで -----
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingActivity::class.java)
            resultLauncher.launch(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        // 1:趣味を既定の選択とする
        if(genre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
        val user = FirebaseAuth.getInstance().currentUser
        binding.navView.menu.getItem(4).setVisible(false)
        if (user != null) {
            binding.navView.menu.getItem(4).setVisible(true)
        }
    }

    // ----- 追加:ここから
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_hobby -> {
                binding.content.toolbar.title = getString(R.string.menu_hobby_label)
                genre = 1
            }
            R.id.nav_life -> {
                binding.content.toolbar.title = getString(R.string.menu_life_label)
                genre = 2
            }
            R.id.nav_health -> {
                binding.content.toolbar.title = getString(R.string.menu_health_label)
                genre = 3
            }
            R.id.nav_computer -> {
                binding.content.toolbar.title = getString(R.string.menu_computer_label)
                genre = 4
            }
            R.id.nav_favorite -> {
                binding.content.toolbar.title = getString(R.string.favorite_title)
                genre = 5
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        // ----- 追加:ここから -----
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        questionArrayList.clear()
        adapter.setQuestionArrayList(questionArrayList)
        binding.content.inner.listView.adapter = adapter

        // 選択したジャンルにリスナーを登録する
        if (genreRef != null) {
            genreRef!!.removeEventListener(eventListener)
        }
        if (genre < 5) {
            genreRef = databaseReference.child(ContentsPATH).child(genre.toString())
            genreRef!!.addChildEventListener(eventListener)
        } else if(genre == 5) {
            var user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                genreRef = databaseReference.child(FavoritePATH).child(user.uid)
                genreRef!!.addChildEventListener(eventListenerFavorite)
            }
        }
        // ----- 追加:ここまで -----
        return true
    }
    // ----- 追加:ここまで
}
