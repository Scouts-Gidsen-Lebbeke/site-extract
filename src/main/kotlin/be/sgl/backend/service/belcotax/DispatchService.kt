package be.sgl.backend.service.belcotax

import be.sgl.backend.dto.DeclarationFormDTO
import be.sgl.backend.entity.Organization
import generated.*
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class DispatchService {

    /**
     * Given the [owner] and [certifier] [Organization]s, generate the dispatch for the listed [forms]
     * in the official [Verzendingen] format. Only the validity relevant for this specific dispatch is enforced.
     * This means that some things are NOT checked here:
     *  - The logical grouping of declarations on members in chunks of four.
     *  - The age restriction of participating members.
     *  - The validity of nis numbers (both on organization level and on user level).
     */
    fun createDispatch(owner: Organization, certifier: Organization, forms: List<DeclarationFormDTO>, previous: String? = null) : Verzendingen {
        check(forms.isNotEmpty()) { "At least one declaration should be present!" }
        val dispatch = Verzending()
        dispatch.v0002Inkomstenjaar = forms.first().year
        dispatch.v0010Bestandtype = "BELCOTAX"
        dispatch.v0011Aanmaakdatum = LocalDate.now().asBelcotaxDate()
        // dispatch.v0012Sequentieelnr => Not used for this form
        // dispatch.v0013Hoofdkantoor => Not used for this form
        dispatch.v0014Naam = owner.name.escaped().assertMaxLength("organization name", 41)
        dispatch.v0015Adres = owner.address.getStreetAdress().assertMaxLength("street address", 32)
        val zipcode = owner.address.zipcode
        if (owner.address.country == "BE") {
            dispatch.v0016Postcode = zipcode.assertLength("organization zipcode", 4)
        } else {
            dispatch.v0027Postcodebuitenl = zipcode.assertLength("organization zipcode", 12)
        }
        dispatch.v0017Gemeente = owner.address.city.assertMaxLength("organization town", 27)
        dispatch.v0018Telefoonnummer = owner.getMobile()?.assertMaxLength("organization phone number", 12)
        // dispatch.v0019Faxnummer => outdated
        dispatch.v0020Identificatie = "BConv86" // Identify as their shitty mapping application
        dispatch.v0021Contactpersoon = owner.getRepresentative().getFullName().assertMaxLength("representative", 34)
        dispatch.v0022Taalcode = getCurrentUserLanguage()
        dispatch.v0023Emailadres = owner.getEmail()?.assertMaxLength("organization phone number", 44)
        dispatch.v0024Nationaalnr = owner.kbo.assertLength("organization national number", 10)
        dispatch.v0025Typeenvoi = previous?.let { "1" } ?: "0"
        dispatch.v0026Referte = previous
        dispatch.v0028Landwoonplaats = owner.address.country.toCountryCode()
        // dispatch.v0030Nationaalnummer => outdated
        dispatch.aangiften = Aangiften()
        dispatch.aangiften.aangifte.add(Aangifte().apply {
            a1002Inkomstenjaar = dispatch.v0002Inkomstenjaar
            // a1003Gewestdirectie => Not used for this form
            // a1004Ontvangkantoor => Not used for this form
            a1005Registratienummer = dispatch.v0024Nationaalnr
            // a1007Division => Not used for this form
            // a1010Schrappingcode => Not used for this form
            a1017Telefoonnummer = dispatch.v0018Telefoonnummer
            // a1018Faxnummer = dispatch.v0019Faxnummer
            a1019Contactpersoon = dispatch.v0021Contactpersoon
            a1020Taalcode = dispatch.v0022Taalcode
            if (a1020Taalcode == "1") {
                a1011Naamnl1 = dispatch.v0014Naam.take(28)
                a1012Naamnl2 = dispatch.v0014Naam.substring(28)
                a1013Adresnl = dispatch.v0015Adres
                a1015Gemeente = dispatch.v0017Gemeente
            } else if (a1020Taalcode == "2") {
                a1027Naamfr1 = dispatch.v0014Naam.take(28)
                a1028Naamfr2 = dispatch.v0014Naam.substring(28)
                a1029Adresfr = dispatch.v0015Adres
                a1030Gemeentefr = dispatch.v0017Gemeente
            } else {
                a1032Naamde1 = dispatch.v0014Naam.take(28)
                a1033Naamde2 = dispatch.v0014Naam.substring(28)
                a1034Adresde = dispatch.v0015Adres
                a1035Gemeentede = dispatch.v0017Gemeente
            }
            a1014Postcodebelgisch = dispatch.v0016Postcode
            a1016Landwoonplaats = dispatch.v0028Landwoonplaats
            a1026Postcodebuitenl = dispatch.v0027Postcodebuitenl
            // a1021Ontvangkantoor => Not used for this form
            // a1022Naamtaxatiedienst => Not used for this form
            // a1023Taxatiedienst => Not used for this form
            // a1024Bijkantoor => Not used for this form
            // a1025Aansluitingsnr => Not used for this form
            // a1031Taalfr => One language is more than enough
            // a1036Taalde => One language is more than enough
            // a1037Nationaalnr => Only used for personal dispatches
            a1038Emailadres = dispatch.v0023Emailadres
            // a1039Foreignvat => Not used for this form
            opgaven = Opgaven()
            opgaven.opgave32500OrOpgave32501OrOpgave32510.addAll(forms.mapIndexed { index, formDTO ->
                createForm(index, formDTO, certifier, dispatch.v0024Nationaalnr)
            })
            r8002Inkomstenjaar = a1002Inkomstenjaar
            // r8003Gewestelijkedirectie => Not used for this form
            // r8004Ontvangkantoor => Not used for this form
            r8005Registratienummer = a1005Registratienummer
            // r8007Division => Not used for this form
            r8010Aantalrecords = (forms.size + 2).toString() // Don't search an explanation
            r8011Controletotaal = (forms.size * (forms.size - 1) / 2).toString() // Sum of all sequence numbers
            r8012Controletotaal = forms.sumOf { it.totalPrice * 2 }.toString() // Sum of all f86_2059_totaalcontrole fields
            r8013Totaalvoorheffingen = "0" // Don't search an explanation
        })
        dispatch.r9002Inkomstenjaar = dispatch.v0002Inkomstenjaar
        dispatch.r9010Aantallogbestanden = "3" // Don't search an explanation
        dispatch.r9011Totaalaantalrecords = (forms.size + 4).toString() // Don't search an explanation
        dispatch.aangiften.aangifte[0].let { // since we use only one declaration in each dispatch
            dispatch.r9012Controletotaal = it.r8011Controletotaal
            dispatch.r9013Controletotaal = it.r8012Controletotaal
            dispatch.r9014Controletotaal = it.r8013Totaalvoorheffingen
        }
        return Verzendingen().apply { verzending = dispatch }
    }

    private fun createForm(index: Int, formDTO: DeclarationFormDTO, certifier: Organization, nis: String) = Fiche28186().apply {
        f2002Inkomstenjaar = formDTO.year
        // f2003Gewestdirectie => Not used for this form
        // f2004Ontvangkantoor => Not used for this form
        f2005Registratienummer = nis
        // f2007Division => Not used for this form
        f2008Typefiche = "28186"
        f2009Volgnummer = index.toString()
        f2010Referentie = formDTO.id
        f2011Nationaalnr = formDTO.parent.nis.assertLength("parent nis number for $f2010Referentie", 11)
        f2012Geboortedatum = f2011Nationaalnr.toNis(f2002Inkomstenjaar)
        f2013Naam = formDTO.parent.name.assertMaxLength("parent name for $f2010Referentie", 41)
        // f2014Naampartner => Not used for this form
        f2015Adres = formDTO.address.getStreetAdress().assertMaxLength("street address for $f2010Referentie", 32)
        if (formDTO.address.country == "BE") {
            f2016Postcodebelgisch = formDTO.address.zipcode.assertLength("zipcode for $f2010Referentie", 4)
        } else {
            f2112Buitenlandspostnummer = formDTO.address.zipcode.assertLength("zipcode for $f2010Referentie", 12)
        }
        f2017Gemeente = formDTO.address.city.assertMaxLength("town for $f2010Referentie", 27)
        f2018Landwoonplaats = formDTO.address.country.toCountryCode()
        // f2019Burgerlijkstand => Not used for this form
        // f2020Echtgenote => Not used for this form
        // f2021Aantalkinderen => Not used for this form
        // f2022Anderentlaste => Not used for this form
        // f2023Diverse => Not used for this form
        // f2024Echtgehandicapt => Not used for this form
        // f2026Verkrghandicap => Not used for this form
        // f2027Taalcode => Not used for this form
        // f2028Typetraitement => Not used for this form
        // f2029Enkelopgave325 => Not used for this form
        // f2105Birthplace => Not used for this form
        f2114Voornamen = formDTO.parent.firstName.assertMaxLength("parent first name for $f2010Referentie", 30)
        f862031Certificationautorisation = "2" // Local association
        f862055Begindate1 = formDTO.activity1.start.asBelcotaxDate()
        f862056Enddate1 = formDTO.activity1.end.asBelcotaxDate()
        f862059Totaalcontrole = (2 * formDTO.totalPrice).toString()
        f862060Amount1 = formDTO.activity1.price.pricePrecision()
        f862061Amount2 = formDTO.activity2?.price.pricePrecision()
        f862062Amount3 = formDTO.activity3?.price.pricePrecision()
        f862063Amount4 = formDTO.activity4?.price.pricePrecision()
        f862064Totalamount = formDTO.totalPrice.toString()
        f862093Begindate2 = formDTO.activity2?.start?.asBelcotaxDate()
        f862100Certifierpostnr = certifier.address.zipcode.assertLength("certifier zipcode", 4) // Only belgian certifiers are allowed
        f862101Childcountry = f2018Landwoonplaats // This should be correct for 99.9% of all cases
        f862102Childaddress = f2015Adres // This should be correct for 99.9% of all cases
        f862106Childname = formDTO.user.name.assertMaxLength("child name for $f2010Referentie", 41)
        f862107Childfirstname = formDTO.user.firstName.assertMaxLength("child first name for $f2010Referentie", 30)
        f862109Certifiercbenumber = certifier.kbo.assertLength("organization national number", 10)
        f862110Numberofday1 = formDTO.activity1.calculateDays().toString()
        f862111Dailytariff1 = formDTO.dailyPrice(formDTO.activity1).pricePrecision()
        f862113Numberofday2 = formDTO.activity2?.calculateDays()?.toString()
        f862115Dailytariff2 = formDTO.dailyPrice(formDTO.activity2).pricePrecision()
        f862116Numberofday3 = formDTO.activity3?.calculateDays()?.toString()
        f862117Dailytariff3 = formDTO.dailyPrice(formDTO.activity3).pricePrecision()
        f862119Numberofday4 = formDTO.activity4?.calculateDays()?.toString()
        f862120Dailytariff4 = formDTO.dailyPrice(formDTO.activity4).pricePrecision()
        f862139Childpostnr = f2016Postcodebelgisch ?: f2112Buitenlandspostnummer // This should be correct for 99.9% of all cases
        f862140Childmunicipality = f2017Gemeente // This should be correct for 99.9% of all cases
        f862144Enddate2 = formDTO.activity2?.end?.asBelcotaxDate()
        f862153Nnchild = formDTO.user.userData.nis.assertLength("child nis number for $f2010Referentie", 11)
        f862154Certifiermunicipality = certifier.address.city.assertMaxLength("certifier town", 27)
        f862155Certifiername = certifier.name.escaped().assertMaxLength("certifier name", 41)
        f862156Certifieradres = certifier.address.getStreetAdress().assertMaxLength("certifier street address", 32)
        f862157Begindate3 = formDTO.activity3?.start?.asBelcotaxDate()
        f862158Enddate3 = formDTO.activity3?.end?.asBelcotaxDate()
        f862161Begindate4 = formDTO.activity4?.start?.asBelcotaxDate()
        f862162Enddate4 = formDTO.activity4?.end?.asBelcotaxDate()
        f862163Childbirthdate = formDTO.user.userData.birthdate.asBelcotaxDate()
        // f862164Beginvaliditycertification => Not required, and assume the organisation is certified the whole year
        // f862171Endvaliditycertification => Not required, and assume the organisation is certified the whole year
    }

    private fun LocalDate.asBelcotaxDate(): String = this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

    private fun LocalDateTime.asBelcotaxDate() = toLocalDate().asBelcotaxDate()

    private fun Double?.pricePrecision() = this?.let { String.format("%.2f", it) }

    private fun String.toCountryCode() = when (this) {
        "BE" -> "150"
        "NL" -> "129"
        "UK" -> "112"
        "LU" -> "113"
        "FR" -> "111"
        "DE" -> "103"
        else -> TODO("Find out which stupid country mapping system our stupid Belgian government uses.")
    }

    // No charset defined in the xsd, but this is from experience.
    // FOD Justitie lets you create a company with it, but FOD Financiën doesn't accept declarations with it, big fun.
    private fun String.escaped() = replace("&", "")

    private fun String?.assertLength(field: String, length: Int) = apply {
        checkNotNull(this) { "Missing $field" }
        check(this.length == length) { "Invalid $field length (should be $length): $this!" }
    }

    private fun String.assertMaxLength(field: String, maxLength: Int) = apply {
        check(this.length <= maxLength) { "Invalid $field length (max $maxLength): $this!" }
    }

    private fun getCurrentUserLanguage() = when (LocaleContextHolder.getLocale().language) {
        "de" -> "3"
        "fr" -> "2"
        else -> "1"
    }

    private fun String.toNis(formYear: String): String {
        val day = substring(4, 6)
        val month = substring(2, 4)
        val year = substring(0, 2)
        val yearPrefix = if (formYear.substring(2).toInt() > year.toInt()) {
            formYear.substring(0, 2)
        } else {
            formYear.substring(0, 2).toInt().minus(1).toString()
        }
        return "$day-$month-$yearPrefix$year"
    }
}