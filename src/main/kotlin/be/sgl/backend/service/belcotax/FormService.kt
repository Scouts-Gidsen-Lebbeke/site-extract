package be.sgl.backend.service.belcotax

import be.sgl.backend.dto.DeclarationFormDTO
import be.sgl.backend.entity.ActivityRegistration
import be.sgl.backend.entity.Organization
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Service
class FormService {

    private val log = LoggerFactory.getLogger(FormService::class.java)

    fun createForm(owner: Organization, certifier: Organization, form: DeclarationFormDTO): ByteArray {
        val formData = mapOf(
            "instanceName" to owner.name,
            "instanceKBO" to owner.kbo,
            "instanceStreet" to owner.address.street,
            "instanceNr" to "${owner.address.number}${owner.address.subPremise}",
            "instanceZip" to owner.address.zipcode,
            "instanceTown" to owner.address.city,
            "certifierName" to certifier.name,
            "certifierKBO" to certifier.kbo,
            "certifierStreet" to certifier.address.street,
            "certifierNr" to "${certifier.address.number}${certifier.address.subPremise}",
            "certifierZip" to certifier.address.zipcode,
            "certifierTown" to certifier.address.city,
            "name" to form.user.name,
            "firstName" to form.user.firstName,
            "birthDate" to form.user.userData.birthdate,
            "street" to form.parent.address?.street,
            "nr" to "${form.parent.address?.number}${form.parent.address?.subPremise}",
            "zip" to form.parent.address?.zipcode,
            "town" to form.parent.address?.city,
            "nis" to form.parent.nis,
            "debtorName" to form.parent.name,
            "debtorFirstName" to form.parent.firstName,
            "debtorStreet" to form.parent.address?.street,
            "debtorNr" to "${form.parent.address?.number}${form.parent.address?.subPremise}",
            "debtorZip" to form.parent.address?.zipcode,
            "debtorTown" to form.parent.address?.city,
            "debtorNis" to form.parent.nis,
            "id" to form.id,
            "taxYear" to form.year,
            "period1" to form.activity1.asPeriod(),
            "period1Days" to form.activity1.calculateDays(),
            "period1Rate" to form.dailyPrice(form.activity1).pricePrecision(),
            "period1Price" to form.activity1.price.pricePrecision(),
            "period1" to form.activity1.asPeriod(),
            "period1Days" to form.activity1.calculateDays(),
            "period1Rate" to form.dailyPrice(form.activity1).pricePrecision(),
            "period1Price" to form.activity1.price.pricePrecision(),
            "period2" to form.activity2.asPeriod(),
            "period2Days" to form.activity2?.calculateDays(),
            "period2Rate" to form.dailyPrice(form.activity2).pricePrecision(),
            "period2Price" to form.activity2?.price.pricePrecision(),
            "period3" to form.activity2.asPeriod(),
            "period3Days" to form.activity3?.calculateDays(),
            "period3Rate" to form.dailyPrice(form.activity3).pricePrecision(),
            "period3Price" to form.activity3?.price.pricePrecision(),
            "period4" to form.activity3.asPeriod(),
            "period4Days" to form.activity4?.calculateDays(),
            "period4Rate" to form.dailyPrice(form.activity4).pricePrecision(),
            "period4Price" to form.activity4?.price.pricePrecision(),
            "totalPrice" to form.totalPrice.pricePrecision(),
            "location" to owner.address.city,
            "authorizer" to owner.getRepresentative().getFullName(),
            "authorizationRole" to "Verantwoordelijke"
        )
        val resultStream = ByteArrayOutputStream()
        Loader.loadPDF(ClassPathResource("forms/form28186.pdf").contentAsByteArray).use { document ->
            val acroForm = document.documentCatalog.acroForm
            fillFormFields(acroForm, formData)
            acroForm.flatten()
            document.save(resultStream)
        }
        return resultStream.toByteArray()
    }

    private fun fillFormFields(acroForm: PDAcroForm, data: Map<String, Any?>) {
        for ((fieldName, value) in data) {
            val field = acroForm.getField(fieldName)
            if (field == null) {
                log.error("Field with name '$fieldName' not found in the PDF form.")
                continue
            }
            field.setValue(value?.toString())
        }
    }

    private fun ActivityRegistration?.asPeriod(): String? {
        this ?: return null
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return "${start.format(format)} - ${end.format(format)}"
    }

    private fun Double?.pricePrecision() = this?.let { String.format("%.2f", it) }
}