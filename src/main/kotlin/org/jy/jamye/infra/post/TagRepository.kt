package org.jy.jamye.infra.post

import org.jy.jamye.domain.post.model.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Meta
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface TagRepository: JpaRepository<Tag, Long> {
    @Meta(comment = "보유한 잼얘의 태그 slicing 조회")
    @Query("""
        SELECT t
        FROM Tag t
        WHERE 
            t.groupSeq = :groupSeq
            AND EXISTS (
                SELECT 1 
                FROM PostTagConnection pc
                INNER JOIN UserGroupPost up ON up.userSequence = :userSeq AND up.postSequence = pc.postSeq 
                where pc.tagSeq = t.tagSeq
            )
    """)
    fun findSliceByGroupSeqAndUserSeq(groupSeq: Long, userSeq: Long,page: Pageable): Slice<Tag>
    fun findByGroupSeqAndTagNameContains(groupSeq: Long, keyword: String, page: Pageable) : Slice<Tag>
    @Meta(comment = "미사용 태그 삭제")
    @Transactional
    @Modifying
    @Query("""
            DELETE FROM Tag WHERE tagSeq IN (
              SELECT temp.tagSeq FROM (
                SELECT t2.tagSeq as tagSeq
                FROM Tag t2
                LEFT JOIN PostTagConnection p ON t2.tagSeq = p.tagSeq
                WHERE p.postTagSeq IS NULL
              ) AS temp
            )
        """)
    fun deleteByNoUseTag()
    @Meta(comment = "태그 총 사용량 업데이트")
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            update
            tag t
        join (
            select
                pt.tag_seq,
                count(pt.pt_seq) as totCnt
            from
                post_tag_con pt
            group by
                pt.tag_seq
        ) as tot on
        tot.tag_seq = t.tag_seq 
        set t.tot_cnt = tot.totCnt
        where tot.tag_seq = t.tag_seq 
    """)
    fun postTotalCount()
    @Meta(comment = "이미 등록된 tag 찾기")
    fun findByTagNameInAndGroupSeq(toSet: Set<String>, groupSeq: Long): List<Tag>
}