package be.sgl.backend.service.user

import be.sgl.backend.entity.User

interface UserDataProvider {
    fun getUser(username: String) : User?
    fun getUserWithAllData(username: String): User?
}