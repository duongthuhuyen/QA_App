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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.techacademy.huyen.duong.qa_app.databinding.ListAnswerBinding
import jp.techacademy.huyen.duong.qa_app.databinding.ListQuestionDetailBinding
import kotlin.contracts.contract

class QuestionDetailListAdapter(context: Context, private val question: Question) : BaseAdapter() {
    private lateinit var databaseReference: DatabaseReference
    companion object {
        private const val TYPE_QUESTION = 0
        private const val TYPE_ANSWER = 1
    }

    private var layoutInflater: LayoutInflater

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
                var q = question
                //checkFavorite()
                databaseReference = FirebaseDatabase.getInstance().reference
                var genreRef: DatabaseReference = databaseReference.child(ContentsPATH).child(question.genre.toString())
                genreRef!!.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        //TODO("Not yet implemented")
                        val map = snapshot.value as Map<*, *>
                        val quid = snapshot.key ?: ""
                        if (quid == question.questionUid) {
                            val favoriteStatus = map["favoriteStatus"] as? String ?: "0"
                            binding.favoriteImageView.apply {
                                // お気に入り状態を取得
                                var isFavorite = favoriteStatus.toInt()
                                // 白抜きの星を設定
                                setImageResource(if (isFavorite == 1) R.drawable.ic_star else R.drawable.ic_star_border)

                                // 星をタップした時の処理
                                setOnClickListener {
                                    if (isFavorite == 0) {
                                        addFavorite(q)
                                        setImageResource(R.drawable.ic_star)
                                        isFavorite = 1
                                        q.favoriteStatus = 1
                                    } else {

                                        // adapter.onClickAddFavorite?.invoke(shop)
                                        AlertDialog.Builder(context)
                                            .setTitle(R.string.delete_favorite_dialog_title)
                                            .setMessage(R.string.delete_favorite_dialog_message)
                                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                                deleteFavorite(q)
                                                setImageResource(R.drawable.ic_star_border)
                                                isFavorite = 0
                                                q.favoriteStatus = 0
                                            }
                                            .setNegativeButton(android.R.string.cancel) { _, _ -> }
                                            .create()
                                            .show()
                                    }
                                }
                            }
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                       // TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        //TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        //TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }

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
        val contentref = databaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid)
       contentref.child("favoriteStatus").setValue("1")
    }
    fun deleteFavorite(question: Question) {
        databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child(ContentsPATH).child(question.genre.toString())
            .child(question.questionUid).child("favoriteStatus").setValue("0")
    }
}
const val KEY_RESULT = "key_result"