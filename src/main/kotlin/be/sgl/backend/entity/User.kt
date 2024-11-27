package be.sgl.backend.entity

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(
    indexes = [
        Index(name = "idx_username", columnList = "username", unique = true),
    ]
)
class User : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
    var username: String? = null
    var externalId: String? = null
    lateinit var name: String
    lateinit var firstName: String
    @OneToOne(fetch = FetchType.LAZY)
    var userData = UserData()
    @OneToMany(fetch = FetchType.EAGER)
    val roles: MutableList<UserRole> = mutableListOf()

    fun getFullName(): String {
        return "$firstName $name"
    }
}