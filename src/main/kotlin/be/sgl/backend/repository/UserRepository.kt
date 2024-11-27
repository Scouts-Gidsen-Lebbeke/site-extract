package be.sgl.backend.repository

import be.sgl.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun getUserByUsernameEquals(username: String): User?
    @Query("select u from User u join fetch u.userData where u.username = :username")
    fun getUserByUsernameEqualsAndUserData(username: String): User?
}