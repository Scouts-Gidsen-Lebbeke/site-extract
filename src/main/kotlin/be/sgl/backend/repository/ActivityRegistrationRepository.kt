package be.sgl.backend.repository

import be.sgl.backend.entity.ActivityRegistration
import be.sgl.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ActivityRegistrationRepository : JpaRepository<ActivityRegistration, Int> {
    fun getByStartBetweenOrderByStart(begin: LocalDateTime, end: LocalDateTime): List<ActivityRegistration>
    fun getByUserAndStartBetweenOrderByStart(user: User, begin: LocalDateTime, end: LocalDateTime): List<ActivityRegistration>
}