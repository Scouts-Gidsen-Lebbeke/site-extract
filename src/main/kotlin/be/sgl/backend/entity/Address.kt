package be.sgl.backend.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Address : Auditable() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null
    var externalId: String? = null
    lateinit var street: String
    var number: Int = -1
    var subPremise: String? = null
    lateinit var zipcode: String
    lateinit var city: String
    lateinit var country: String
    var description: String? = null
    var postalAdress: Boolean = false

    fun getStreetAdress(): String {
        return "$street $number${subPremise ?: ""}"
    }
}