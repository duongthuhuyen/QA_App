package jp.techacademy.huyen.duong.qa_app
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.qa_app.databinding.ListAnswerBinding
import jp.techacademy.huyen.duong.qa_app.databinding.ListQuestionDetailBinding
import kotlin.contracts.contract

class QuestionDetailListAdapter(context: Context, private val question: Question) : BaseAdapter(), DatabaseReference.CompletionListener {
    private lateinit var databaseReference: DatabaseReference
    companion object {
        private const val TYPE_QUESTION = 0
        private const val TYPE_ANSWER = 1
    }

    private var layoutInflater: LayoutInflater
    private var listQidFavorite: ArrayList<String> = arrayListOf()

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + question.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return question
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (getItemViewType(position) == TYPE_QUESTION) { // gọi getItemViewType -> phán đoán loại layout nào
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                ListQuestionDetailBinding.inflate(layoutInflater, parent, false)
            } else {
                ListQuestionDetailBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                var uid = user.uid
                //checkFavorite()
                databaseReference = FirebaseDatabase.getInstance().reference
                var genreRef: DatabaseReference = databaseReference.child(FavoritePATH).child(uid)
                var a = FirebaseDatabase.getInstance().getReference("favorites")
                var query: Query = databaseReference.child(FavoritePATH).child(uid).orderByChild("quid").equalTo(question.questionUid)
                genreRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        var isFavorite = 0
                        if (dataSnapshot.exists())
                        {
                            isFavorite = 1
                        }
//                        dataSnapshot.key?.let { listQidFavorite.add(it) }
//                        Log.d("List favorite length: ",""+listQidFavorite.size)
//                        if (question.questionUid in listQidFavorite) {
//                            isFavorite = 1
//                        }

                        binding.favoriteImageView.apply {

                            // 白抜きの星を設定
                            setImageResource(if (isFavorite == 1) R.drawable.ic_star else R.drawable.ic_star_border)

                            // 星をタップした時の処理
                            setOnClickListener {
                                if (isFavorite == 0) {
                                    addFavorite(question)
                                    setImageResource(R.drawable.ic_star)
                                    isFavorite = 1
                                } else {

                                    // adapter.onClickAddFavorite?.invoke(shop)
                                    AlertDialog.Builder(context)
                                        .setTitle(R.string.delete_favorite_dialog_title)
                                        .setMessage(R.string.delete_favorite_dialog_message)
                                        .setPositiveButton(android.R.string.ok) { _, _ ->
                                            deleteFavorite(question)
                                            setImageResource(R.drawable.ic_star_border)
                                            isFavorite = 0
                                        }
                                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                                        .create()
                                        .show()
                                }
                            }
                        }
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

                    override fun onChildRemoved(p0: DataSnapshot) {}
                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                    override fun onCancelled(p0: DatabaseError) {}
                })
            }
            binding.bodyTextView.text = question.body
            binding.nameTextView.text = question.name

            val bytes = question.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                binding.imageView.setImageBitmap(image)
            }

            return view
        } else {
            // ViewBindingを使うための設定
            val binding = if (convertView == null) {
                ListAnswerBinding.inflate(layoutInflater, parent, false)
            } else {
                ListAnswerBinding.bind(convertView)
            }
            val view: View = convertView ?: binding.root

            binding.bodyTextView.text = question.answers[position - 1].body
            binding.nameTextView.text = question.answers[position - 1].name

            return view
        }
    }
    fun addFavorite(question: Question) {
        databaseReference = FirebaseDatabase.getInstance().reference
        val data = HashMap<String, String>()
        // UID
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val genreRef = databaseReference.child(FavoritePATH).child(uid)
        data["quid"] = question.questionUid
        data["title"] = question.title
        data["body"] = question.body
        data["name"] = question.name
        data["uid"] = uid
        if (question.imageBytes != null) {
            val bitmapString =
                Base64.encodeToString(question.imageBytes, Base64.DEFAULT)
            data["image"] = bitmapString
        }
        genreRef.push().setValue(data,this)
    }
    fun deleteFavorite(question: Question) {
        databaseReference = FirebaseDatabase.getInstance().reference
        val data = HashMap<String, String>()
        // UID
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = databaseReference.child(FavoritePATH).child(uid).child(question.questionUid)
        ref.setValue(null)
    }
    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {

        if (databaseError == null) {
        } else {
           Log.d("success","success!")
        }
    }
}
