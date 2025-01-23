package org.jy.jamye.application

import org.jy.jamye.domain.service.GroupService
import org.jy.jamye.domain.service.UserService
import org.jy.jamye.infra.UserRepository
import org.jy.jamye.security.TokenDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserApplicationService(
    private val userService: UserService,
    private val groupService: GroupService,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun deleteUser(username: String, password: String) {
        val userSeq = userService.deleteUser(username, password)
        groupService.autoTransferMasterPrivileges(userSeq, setOf())
    }

    fun duplicateIdCheck(id: String): Boolean {
        return !userRepository.existsByUserId(id)
    }

    fun duplicateEmailCheck(email: String): Boolean {
        return !userRepository.existsByEmail(email)
    }
}
