package org.jy.jamye.job

import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.common.listener.NotifyInfo
import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.PostService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.math.ceil

@Component
class GroupVoteEndJob(
    private val groupService: GroupService,
    private val redisClient: RedisClient,
    private val postService: PostService,
    private val publisher: ApplicationEventPublisher
) : Job {
    private val log = LoggerFactory.getLogger(GroupVoteEndJob::class.java)

    override fun execute(context: JobExecutionContext?) {
        val groupSeq = context?.jobDetail?.jobDataMap?.getLong("voteId")
        if (groupSeq == null) {
            log.info("삭제 투표 실패")
        }
        val group = groupService.getGroupSimpleInfo(groupSeq!!)
        log.info("투표 ID $groupSeq 마감 작업을 실행합니다.")

        val deleteVoteMap = redisClient.getDeleteVoteMap()
        val voteInfo = deleteVoteMap[groupSeq]
        lateinit var message: String
        voteInfo?.let {
            val deleteAgree = voteInfo.agreeUserSeqs
            if(deleteAgree.size > (ceil(voteInfo.standardVoteCount.toDouble()/2))) {
                message = "[1차 투표] ${group.name}의 삭제 투표가 완료되었습니다. 과반수 삭제 동의(${deleteAgree.size}/${voteInfo.standardVoteCount}명)로 인해 그룹이 자동 삭제되었습니다."
                log.info("[1차 투표] $groupSeq: 과반수 삭제 동의 ${deleteAgree.size}/${voteInfo.standardVoteCount}명")
                groupService.deleteGroup(groupSeq)
            } else if(voteInfo.hasRevoted) {
                message = "[2차 투표] ${group.name}의 삭제투표가 완료되었습니다. 과반수가 삭제 비동의(${deleteAgree.size}/${voteInfo.standardVoteCount}명)로 인해 그룹이 삭제되지 않았습니다. 삭제투표에 동의한 인원은 자동 그룹 탈퇴되었습니다."
                log.info("[2차 투표] $groupSeq: ${deleteAgree.size}/${voteInfo.standardVoteCount}명 과반수 달성 불가")
                groupService.deleteUsers(groupSeq, deleteAgree)
                postService.deleteUserAllPostInGroup(deleteAgree, groupSeq)
            } else {
                message = "[1차투표 종료] ${group.name}의 삭제투표가 완료되었습니다. 과반수가 삭제 비동의(${deleteAgree.size}/${voteInfo.standardVoteCount}명)로 인해 그룹이 삭제되지 않았습니다."
                log.info("[1차투표 종료] $groupSeq: ${deleteAgree.size}/${voteInfo.standardVoteCount}명 과반수 달성 불가")
                redisClient.setValueObjectExpireDay("waitingReVote-${groupSeq}", LocalDateTime.now().toString(), 7)
            }
            deleteVoteMap.remove(groupSeq)
            redisClient.setValueObject("deleteVotes", deleteVoteMap)
            val userSeqsInGroup = groupService.getUserSeqsInGroup(groupSeq)
            val event = NotifyInfo(groupSeq = groupSeq, userSeqs = userSeqsInGroup, message = message)
            publisher.publishEvent(event)
        }

    }
}