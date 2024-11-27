package be.sgl.backend.entity

import be.sgl.backend.entity.enum.Sex
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Entity
class UserData : Auditable() {
    @Id
    val id: Int? = null
    @OneToOne
    @PrimaryKeyJoinColumn
    lateinit var user: User
    var memberId: String? = null
    var birthdate: LocalDate = LocalDate.now()
    lateinit var email: String
    var mobile: String? = null
    var nis: String? = null
    var accountNo: String? = null
    var sex = Sex.UNKNOWN
    var hasReduction = false
    var hasHandicap = false
    @OneToMany(fetch = FetchType.EAGER)
    val addresses: MutableList<Address> = mutableListOf()
    @OneToMany
    val contacts: MutableList<Contact> = mutableListOf()

    fun getAge(referenceDate: LocalDateTime = LocalDateTime.now()): Int {
        return Period.between(birthdate, referenceDate.toLocalDate()).years
    }
}