import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val gson = Gson()
private val BASE_URL = "http://127.0.0.1:9999"

suspend fun OkHttpClient.apiCall(url: String): Response {
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let(::newCall)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
    }
}

private suspend fun makeRequest(url: String): Response{
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let {
                client.newCall(it).enqueue(object : Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response)
                    }
                })
            }
    }
}
private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()

private suspend fun <T> parseResponse(url: String, typeToken: TypeToken<T>): T {
    return withContext(context = Dispatchers.IO) {
        gson.fromJson(requireNotNull(makeRequest(url).body!!.string()), typeToken.type)
    }
}

suspend fun getPosts(): List<Post> = parseResponse("$BASE_URL/api/posts", object: TypeToken<List<Post>>(){})

suspend fun getComments(postId: Long): List<Comment> =
    parseResponse("$BASE_URL/api/posts/$postId/comments", object : TypeToken<List<Comment>>() {})
suspend fun getAuthor(id: Long): Author =
    parseResponse("$BASE_URL/api/authors/$id", object : TypeToken<Author>() {})
//"$BASE_URL/api/slow/authors/$id"
suspend fun setAuthor(authors : List<Author>, comment: Comment ): Author
{
    val author: Author =
        if (authors.find { comment.authorId == it.id } != null) {
            authors.find { comment.authorId == it.id }!!
        } else {
            getAuthor(comment.authorId)
        }
    return author
}

fun main() {
    var authors : List<Author> = emptyList()
    var postsWithAuthor: List<PostWithAuthor> = emptyList()
    var result : List<PostWithComments> = emptyList()
    with(CoroutineScope(EmptyCoroutineContext)) {//создание пространства
        launch {
            try {
                getPosts()
                    .map { post ->
                        async {
                            val author: Author =
                                if (authors.find { post.authorId == it.id } != null) {
                                    authors.find { post.authorId == it.id }!!
                                } else {
                                    getAuthor(post.authorId)
                                }
                            val postsWithAuthor = postsWithAuthor + PostWithAuthor(post, author)
                        }
                    }.awaitAll()

                postsWithAuthor.map { postWithAuthor ->
                    async {
                        result=result+PostWithComments(postWithAuthor, comments = getComments(postWithAuthor.post.id)
                            .map{
                            CommentWithAuthor(it, setAuthor(authors,it))
                        })
                    }
                }.awaitAll()
                println(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
    Thread.sleep(1000L)
}