package be.sgl.backend.config

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.jwt.Jwt

class CustomUserDetails(jwt: Jwt) : UserDetails {

    private val username: String = jwt.getClaim("preferred_username")
    val firstName: String = jwt.getClaim("given_name")
    val lastName: String = jwt.getClaim("family_name")
    val email: String = jwt.getClaim("email")
    val externalId: String = jwt.getClaim("sub")
    private val authorities = jwt.getClaimAsStringList("roles")?.map { SimpleGrantedAuthority(it) } ?: listOf()

    override fun getAuthorities() = authorities

    override fun getPassword() = null

    override fun getUsername(): String = username
}