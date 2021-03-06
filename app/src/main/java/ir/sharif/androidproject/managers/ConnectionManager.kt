package ir.sharif.androidproject.managers

import ir.sharif.androidproject.Advertiser
import ir.sharif.androidproject.MessageController
import ir.sharif.androidproject.MyApplication
import ir.sharif.androidproject.database.comments.CommentBean
import ir.sharif.androidproject.database.posts.PostBean
import ir.sharif.androidproject.models.Advertisement
import ir.sharif.androidproject.models.AdvertisementType
import ir.sharif.androidproject.models.Item
import ir.sharif.androidproject.webservice.WebserviceHelper
import ir.sharif.androidproject.webservice.base.WebserviceException
import java.net.SocketTimeoutException

object ConnectionManager {
    private val cloud = DispatchQueue("Connection")

    fun loadPosts() {
        cloud.postRunnable {
            try {
                val posts = WebserviceHelper.getPosts()
                MyApplication.database.postDao().nukeTable()
                MyApplication.database.postDao().insertAll(*posts.map { postResponse ->
                    with(postResponse) {
                        PostBean(id!!, userId!!, title, body)
                    }
                }.toTypedArray())
                Advertiser.advertise(Advertisement(AdvertisementType.POSTS_LOADED, posts))
            } catch (e: Exception) {
                StorageManager.loadPosts()
            }
        }
    }

    fun loadComments(postId: Int) {
        cloud.postRunnable {
            try {
                val comments = WebserviceHelper.getComments(postId)
                MyApplication.database.commentDao().deletePostComments(postId)
                MyApplication.database.commentDao().insertAll(*comments.map { commentResponse ->
                    with(commentResponse) {
                        CommentBean(postId, id!!, name, email, body)
                    }
                }.toTypedArray())
                Advertiser.advertise(Advertisement(AdvertisementType.COMMENTS_LOADED, comments))
            } catch (e: Exception) {
                StorageManager.loadComments(postId)
            }
        }
    }

    fun load(number: Int) =
        cloud.postRunnable({
            MessageController.onFetchComplete(((number + 1..number + 10).map {
                Item(
                    it.toString(),
                    "From Server",
                    ""
                )
            }), true)
        }, 3000)
}
