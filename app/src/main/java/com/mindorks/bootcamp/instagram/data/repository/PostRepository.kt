package com.mindorks.bootcamp.instagram.data.repository

import com.mindorks.bootcamp.instagram.data.local.db.DatabaseService
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.remote.NetworkService
import com.mindorks.bootcamp.instagram.data.remote.request.PostCreationRequest
import com.mindorks.bootcamp.instagram.data.remote.request.PostLikeModifyRequest
import io.reactivex.Single
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val databaseService: DatabaseService,
    private val networkService: NetworkService
) {

    fun fetchHomePostList(
        firstPostId: String?,
        lastPostId: String?,
        user: User
    ): Single<List<Post>> {
        return networkService.doHomePostListCall(
            firstPostId,
            lastPostId,
            user.id,
            user.accessToken
        ).map { it.data }
    }

    fun makeLikePost(post: Post, user: User): Single<Post> {
        return networkService.doPostLikeCall(
            PostLikeModifyRequest(post.id),
            user.id,
            user.accessToken
        ).map {
            post.likedBy?.apply {
                this.find { postUser -> postUser.id == user.id } ?: this.add(
                    Post.User(
                        user.id,
                        user.name,
                        user.profilePicUrl
                    )
                )
            }
            return@map post
        }
    }

    fun makeUnLikePost(post: Post, user: User): Single<Post> {
        return networkService.doPostUnLikeCall(
            PostLikeModifyRequest(post.id),
            user.id,
            user.accessToken
        ).map {
            post.likedBy?.apply {
                this.find { postUser -> postUser.id == user.id }?.let { this.remove(it) }
            }
            return@map post
        }
    }

    fun createPost(imageUrl: String, imageWidth: Int, imageHeight: Int, user: User): Single<Post> =
        networkService.doPostCreationCall(
            PostCreationRequest(imageUrl, imageWidth, imageHeight), user.id, user.accessToken
        ).map {
            Post(
                it.data.id,
                it.data.imgUrl,
                it.data.imgWidth,
                it.data.imgHeight,
                Post.User(
                    user.id,
                    user.name,
                    user.profilePicUrl
                ),
                mutableListOf(),
                it.data.createdAt
            )
        }
}