package be.sgl.backend.entity

import be.sgl.backend.entity.enum.ContactMethodType
import be.sgl.backend.entity.enum.OrganizationType
import jakarta.persistence.*

@Entity
class Organization : Auditable() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
    var externalId: String? = null
    lateinit var name: String
    @Enumerated(EnumType.STRING)
    var type = OrganizationType.OWNER
    var kbo: String? = null
    @ManyToOne
    lateinit var address: Address
    @OneToMany(cascade = [(CascadeType.ALL)])
    val contactMethods: List<ContactMethod> = listOf()

    fun getEmail(): String? {
        return contactMethods.firstOrNull { it.type == ContactMethodType.EMAIL }?.value
    }

    fun getMobile(): String? {
        return contactMethods.firstOrNull { it.type == ContactMethodType.MOBILE }?.value
    }

    fun getRepresentative(): User {
        return User()
    }
}