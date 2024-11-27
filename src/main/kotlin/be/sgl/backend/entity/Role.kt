package be.sgl.backend.entity

import be.sgl.backend.entity.enum.RoleLevel
import jakarta.persistence.*

@Entity
class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var externalId: String? = null
    var backupExternalId: String? = null
    lateinit var name: String
    var level = RoleLevel.GUEST
}