package be.sgl.backend.service.user

import be.sgl.backend.entity.User
import be.sgl.backend.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class InternalUserDataProvider : UserDataProvider {

    @Autowired
    private lateinit var userRepository: UserRepository

    override fun getUser(username: String): User? {
        return userRepository.getUserByUsernameEquals(username)
    }

    override fun getUserWithAllData(username: String): User? {
        return userRepository.getUserByUsernameEqualsAndUserData(username)
    }
}