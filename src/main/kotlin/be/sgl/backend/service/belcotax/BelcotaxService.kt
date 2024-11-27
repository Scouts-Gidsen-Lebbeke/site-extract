package be.sgl.backend.service.belcotax

import be.sgl.backend.dto.DeclarationFormDTO
import be.sgl.backend.entity.ActivityRegistration
import be.sgl.backend.entity.User
import be.sgl.backend.entity.enum.OrganizationType
import be.sgl.backend.repository.ActivityRegistrationRepository
import be.sgl.backend.repository.OrganizationRepository
import be.sgl.backend.service.MailService
import be.sgl.backend.service.exception.IncompleteConfigurationException
import be.sgl.backend.service.user.UserDataProvider
import generated.Verzendingen
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BelcotaxService {

    @Autowired
    private lateinit var userDataProvider: UserDataProvider
    @Autowired
    private lateinit var organizationRepository: OrganizationRepository
    @Autowired
    private lateinit var registrationRepository: ActivityRegistrationRepository
    @Autowired
    private lateinit var dispatchService: DispatchService
    @Autowired
    private lateinit var formService: FormService
    @Autowired
    private lateinit var mailService: MailService

    fun getDispatchForFiscalYearAndRate(fiscalYear: Int?, rate: Double?): Verzendingen {
        val (beginOfYear, endOfYear) = getPeriod(fiscalYear)
        val activities = registrationRepository.getByStartBetweenOrderByStart(beginOfYear, endOfYear).filter(::relevantActivity)
        val forms = activities.groupBy { it.user }.flatMap { (user, activities) -> activities.asForms(user, rate) }
        return dispatchService.createDispatch(fetchOwner(), fetchCertifier(), forms)
    }

    fun getFormsForUserFiscalYearAndRate(username: String, fiscalYear: Int?, rate: Double?): List<ByteArray> {
        val (beginOfYear, endOfYear) = getPeriod(fiscalYear)
        val user = userDataProvider.getUserWithAllData(username) ?: return emptyList()
        val activities = registrationRepository.getByUserAndStartBetweenOrderByStart(user, beginOfYear, endOfYear).filter(::relevantActivity)
        val owner = fetchOwner()
        val certifier = fetchCertifier()
        return activities.asForms(user, rate).map { formService.createForm(owner, certifier, it) }
    }

    fun getFormsForFiscalYearAndRate(fiscalYear: Int?, rate: Double?): Map<User, List<ByteArray>> {
        val (beginOfYear, endOfYear) = getPeriod(fiscalYear)
        val activities = registrationRepository.getByStartBetweenOrderByStart(beginOfYear, endOfYear).filter(::relevantActivity)
        val owner = fetchOwner()
        val certifier = fetchCertifier()
        return activities.groupBy { it.user }.flatMap { (user, activities) -> activities.asForms(user, rate) }
            .groupBy(DeclarationFormDTO::user) { formService.createForm(owner, certifier, it) }
    }

    fun mailFormsToUser(fiscalYear: Int?, user: User, forms: List<ByteArray>) {
        val year = fiscalYear ?: (LocalDate.now().year - 1)
        val params = mapOf("member.first.name" to user.firstName, "fiscal-year" to year)
        val mailBuilder = mailService.builder()
            .from(fetchOwner().getEmail() ?: throw IncompleteConfigurationException("No organization email configured, not able to send forms!"))
            .to(user.userData.email)
            .subject("Fiscaal attest kinderopvang $year")
            .template("declaration-form-confirmation.html", params)
        forms.forEach { mailBuilder.addAttachment(it, "", "application/pdf") }
        mailBuilder.send()
    }

    private fun getPeriod(fiscalYear: Int?): Pair<LocalDateTime, LocalDateTime> {
        val year = fiscalYear ?: (LocalDate.now().year - 1)
        val beginOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0, 0)
        val endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59, 999999999)
        return beginOfYear to endOfYear
    }

    private fun relevantActivity(registration: ActivityRegistration): Boolean {
        val userData = registration.user.userData
        return userData.getAge(registration.start) < (if (userData.hasHandicap) 21 else 14)
    }

    private fun List<ActivityRegistration>.asForms(user: User, rate: Double?) = chunked(4).mapIndexed { index, it ->
        DeclarationFormDTO(user, it[0], it.getOrNull(1), it.getOrNull(2), it.getOrNull(3), rate ?: 14.4, index)
    }

    private fun fetchOwner() = organizationRepository.getByType(OrganizationType.OWNER)
        ?: throw IncompleteConfigurationException("No organization configured!")

    private fun fetchCertifier() = organizationRepository.getByType(OrganizationType.CERTIFIER)
        ?: throw IncompleteConfigurationException("No certifier configured!")
}