package ir.sharif.androidproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.sharif.androidproject.models.Advertisement
import ir.sharif.androidproject.models.AdvertisementType
import ir.sharif.androidproject.webservice.webservices.posts.PostResponse
import kotlinx.android.synthetic.main.activity_posts.*

class PostsActivity : AppCompatActivity(), Advertiser.AdvertiseListener<List<PostResponse>> {
    private lateinit var postAdapter: PostAdapter
    private var isInGridMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        title = "prj2"
        postList.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(arrayListOf())
        postList.adapter = postAdapter
        MessageController.fetchPosts()
    }

    override fun onResume() {
        super.onResume()
        Advertiser.subscribe(this, AdvertisementType.POSTS_LOADED)
    }

    override fun onPause() {
        super.onPause()
        Advertiser.unSubscribe(this, AdvertisementType.POSTS_LOADED)
    }

    override fun receiveData(advertisement: Advertisement<List<PostResponse>>) {
        runOnUiThread {
            postAdapter.replaceData(advertisement.data)
        }
    }

    inner class PostAdapter(private var postList: ArrayList<PostResponse>) : RecyclerView.Adapter<PostViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PostViewHolder(LayoutInflater.from(this@PostsActivity).inflate(R.layout.post_item, null))

        override fun getItemCount() = postList.size

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            holder.itemView.findViewById<TextView>(R.id.titleTextView).text = postList[position].title
            holder.itemView.findViewById<TextView>(R.id.bodyTextView).text = postList[position].body
        }

        fun replaceData(posts: List<PostResponse>) {
            postList.clear()
            postList.addAll(posts)
            notifyDataSetChanged()
        }
    }

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
