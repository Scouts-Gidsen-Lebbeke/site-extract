package be.sgl.backend.repository

import be.sgl.backend.entity.Organization
import be.sgl.backend.entity.enum.OrganizationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrganizationRepository : JpaRepository<Organization, Int> {
    fun getByType(type: OrganizationType): Organization?
}