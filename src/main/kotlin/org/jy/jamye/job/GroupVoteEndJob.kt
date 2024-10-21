package org.jy.jamye.job

import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.PostService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GroupVoteEndJob(
    private val groupService: GroupService,
    private val redisClient: RedisClient,
    private val postService: PostService,
    service: PostService
) : Job {
    private val log = LoggerFactory.getLogger(GroupVoteEndJob::class.java)

    override fun execute(context: JobExecutionContext?) {
        val groupSeq = context?.jobDetail?.jobDataMap?.getLong("voteId")
        log.info("투표 ID $groupSeq 마감 작업을 실행합니다.")

        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val voteInfo = deleteVoteMap[groupSeq]
        voteInfo?.let {
            val deleteAgree = voteInfo.agreeUserSeqs
            if(deleteAgree.size > (voteInfo.standardVoteCount/2)) {
                log.info("[1차 투표] $groupSeq: 과반수 삭제 동의 ${deleteAgree.size}/${voteInfo.standardVoteCount}명")
                groupService.deleteGroup(groupSeq!!, voteInfo)
                postService.deletePostInGroup(groupSeq)
            } else if(voteInfo.hasRevoted) {
                log.info("[2차 투표] $groupSeq: ${deleteAgree.size}/${voteInfo.standardVoteCount}명 과반수 달성 불가")
                postService.deleteUserAllPostInGroup(deleteAgree, groupSeq!!)
                groupService.deleteUsers(groupSeq, deleteAgree)

            } else {
                log.info("[1차투표 종료] $groupSeq: ${deleteAgree.size}/${voteInfo.standardVoteCount}명 과반수 달성 불가")
                redisClient.setValueObjectExpireDay("waitingReVote-${groupSeq}", LocalDateTime.now().toString(), 7)
            }
            deleteVoteMap.remove(groupSeq)
            redisClient.setValueObject("deleteVotes", deleteVoteMap)
        }

    }
}