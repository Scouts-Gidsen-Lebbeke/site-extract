package be.sgl.backend.config.security

import be.sgl.backend.entity.enum.RoleLevel
import be.sgl.backend.service.user.UserDataProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class LevelSecurityService {

    @Autowired
    private lateinit var userDataProvider: UserDataProvider

    fun isAdmin() = currentUserHasRoleWithLevel(RoleLevel.ADMIN)

    fun isStaff() = currentUserHasRoleWithLevel(RoleLevel.STAFF)

    fun isMember() = currentUserHasRoleWithLevel(RoleLevel.MEMBER)

    private fun currentUserHasRoleWithLevel(level: RoleLevel): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = userDataProvider.getUser(authentication?.name ?: return false) ?: return false
        return user.roles.any { it.role.level == level }
    }
}
