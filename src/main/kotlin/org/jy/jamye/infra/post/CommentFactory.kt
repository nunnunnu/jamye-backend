package org.jy.jamye.infra.post

import org.jy.jamye.domain.post.model.Comment
import org.springframework.stereotype.Service

@Service
class CommentFactory {
    fun createComment(userSeq: Long, groupSeq: Long, postSeq: Long, comment: String): Comment {
        return Comment(userSeq = userSeq, groupSeq = groupSeq, postSeq = postSeq, comment = comment)
    }

}
