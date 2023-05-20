data class Post(
    val id: Long,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    var attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val description: String,
    val type: AttachmentType,
)
enum class AttachmentType{

}
data class Comment(
    val id: Long,
    val postId: Long,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
)

data class Author(
    val id: Long,
    val name: String,
    val avatar: String,
)
data class PostWithComments(
    val postWithAuthor: PostWithAuthor,
    val comments: List<CommentWithAuthor>
)
//WithAuthor
data class PostWithAuthor(
    val post: Post,
    val author: Author
)
data class CommentWithAuthor(
    val comment: Comment,
    val author: Author
)