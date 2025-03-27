package org.jy.jamye.domain.model

import jakarta.persistence.*

@Table(name = "post_tag_con")
@Entity
class PostTagConnection(
    @Column(name = "post_seq")
    val postSeq: Long,
    @Column(name = "tag_seq")
    val tagSeq: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_seq")
    var postTagSeq: Long? = null,
) {
}