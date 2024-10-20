package org.jy.jamye.job

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GroupVoteEndJob : Job {
    private val logger = LoggerFactory.getLogger(GroupVoteEndJob::class.java)

    override fun execute(context: JobExecutionContext?) {
        val voteId = context?.jobDetail?.jobDataMap?.getLong("voteId")
        logger.info("투표 ID $voteId 마감 작업을 실행합니다.")

        // 투표 마감 처리 로직
    }
}