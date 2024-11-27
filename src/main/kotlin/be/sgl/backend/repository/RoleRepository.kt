package be.sgl.backend.repository

import be.sgl.backend.entity.Role
import be.sgl.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun getRoleByExternalIdEquals(id: String): Role?
}