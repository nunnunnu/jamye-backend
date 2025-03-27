package org.jy.jamye.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "pi_comment")
class Tag(
    @Column(name = "tag_name")
    var tagName: String,
    @Column(name = "gi_seq")
    var groupSeq: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_seq")
    var tagSeq: Long? = null

)