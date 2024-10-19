package org.jy.jamye.application

import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserApplicationService(private val userService: UserService, private val groupService: GroupService) {
    @Transactional
    fun deleteUser(username: String, password: String) {
        val userSeq = userService.deleteUser(username, password)
        groupService.autoTransferMasterPrivileges(userSeq)
    }

}
