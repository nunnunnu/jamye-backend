package org.jy.jamye.domain.group.service

import jakarta.annotation.PostConstruct
import org.jy.jamye.common.client.RedisClient
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
    private val redisClient: RedisClient
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
        val deleteVoteMap = redisClient.getDeleteVoteMap()
        deleteVoteMap.entries.forEach { (key, value) ->
            val endDateTime = value.endDateAsLocalDateTime()
            scheduleVoteEndJob(key,
            if (endDateTime.isBefore(LocalDateTime.now())) LocalDateTime.now().plusMinutes(1) else endDateTime) }

    }
}
