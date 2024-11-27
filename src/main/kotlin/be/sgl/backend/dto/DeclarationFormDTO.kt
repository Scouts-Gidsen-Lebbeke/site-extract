package be.sgl.backend.dto

import be.sgl.backend.entity.ActivityRegistration
import be.sgl.backend.entity.Address
import be.sgl.backend.entity.Contact
import be.sgl.backend.entity.User
import java.time.format.DateTimeFormatter

data class DeclarationFormDTO(
    val user: User,
    val activity1: ActivityRegistration,
    val activity2: ActivityRegistration?,
    val activity3: ActivityRegistration?,
    val activity4: ActivityRegistration?,
    val rate: Double,
    private val index: Int
) {
    val year: String
        get() = activity1.start.year.toString()
    val id: String
        get() = "${user.name[0]}${user.firstName[0]}${user.userData.birthdate.format(ID_DATE_FORMAT)}-$year-${index + 1}"
    val address: Address
        get() = user.userData.addresses.first { it.postalAdress }
    val parent: Contact
        get() = user.userData.contacts.first { it.nis != null }
    val totalPrice: Double
        get() = activity1.price + (activity2?.price ?: 0.0) + (activity3?.price ?: 0.0) + (activity4?.price ?: 0.0)

    fun dailyPrice(activity: ActivityRegistration?) = activity?.price?.div(activity.calculateDays())?.takeIf { it > rate }
}

val ID_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")