package org.jy.jamye.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "tag")
class Tag(
    @Column(name = "tag_name")
    var tagName: String,
    @Column(name = "gi_seq")
    var groupSeq: Long,
    @Column(name="tot_cnt")
    var postUseTotalCount: Long = 0,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_seq")
    var tagSeq: Long? = null

)