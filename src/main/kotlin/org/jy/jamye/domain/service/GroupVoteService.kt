package org.jy.jamye.domain.service

import jakarta.annotation.PostConstruct
import org.jy.jamye.common.client.RedisClient
import org.jy.jamye.domain.model.GroupVote
import org.jy.jamye.infra.GroupVoteRepository
import org.jy.jamye.job.GroupVoteEndJob
import org.quartz.*
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class GroupVoteService(
    private val schedulerFactoryBean: SchedulerFactoryBean,
    private val redisClient: RedisClient,
    private val groupVoteRepository: GroupVoteRepository
) {

    fun scheduleVoteEndJob(voteId: Long, endDateTime: LocalDateTime) {
        val jobDetail = buildJobDetail(voteId)
        val trigger = buildTrigger(jobDetail, endDateTime)

        val scheduler = schedulerFactoryBean.scheduler
        scheduler.scheduleJob(jobDetail, trigger)
    }

    private fun buildJobDetail(voteId: Long): JobDetail {
        return JobBuilder.newJob(GroupVoteEndJob::class.java)
            .withIdentity("voteEndJob-$voteId", "voteEndGroup")
            .usingJobData("voteId", voteId)
            .build()
    }

    private fun buildTrigger(jobDetail: JobDetail, endDateTime: LocalDateTime): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("${jobDetail.key.name}-trigger", "voteEndGroup")
            .startAt(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule())
            .build()
    }

    @PostConstruct
    fun scheduleInit() {
        val totalSchedule = groupVoteRepository.findAll()
        totalSchedule.forEach { scheduleVoteEndJob(it.groupVoteSeq!!,
            if (it.endDateTime.isBefore(LocalDateTime.now())) LocalDateTime.now().plusMinutes(1) else it.endDateTime) }

    }

    fun saveVoteSchedule(groupSeq: Long, endDateTime: LocalDateTime): Long {
        val save = groupVoteRepository.save(GroupVote(groupSeq = groupSeq, endDateTime = endDateTime))
        return save.groupVoteSeq!!
    }

}
