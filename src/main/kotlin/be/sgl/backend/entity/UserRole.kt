package be.sgl.backend.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDate

@Entity
class UserRole() : Auditable() {

    @EmbeddedId
    private lateinit var id: UserRoleId
    @MapsId("userId")
    @ManyToOne
    @JoinColumn
    lateinit var user: User
    @MapsId("roleId")
    @ManyToOne
    @JoinColumn
    lateinit var role: Role
    lateinit var startDate: LocalDate
    var endDate: LocalDate? = null

    constructor(user: User, role: Role, startDate: LocalDate, endDate: LocalDate?) : this() {
        this.user = user
        this.role = role
        this.startDate = startDate
        this.endDate = endDate
    }

    @Embeddable
    private data class UserRoleId(var userId: Int? = null, var roleId: Int? = null) : Serializable
}