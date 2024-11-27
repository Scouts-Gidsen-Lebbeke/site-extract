package be.sgl.backend.util

// Put all the nasty SGV entities together and don't let the nastiness leak out
data class Lid(
    val id: String, // only used at user creation
    val aangepast: String, // ignored
    val persoonsgegevens: Persoonsgegevens,
    val vgagegevens: Vgagegevens,
    val verbondsgegevens: Verbondsgegevens,
    val gebruikersnaam: String, // only used at user creation
    val adressen: List<Adres>,
    val contacten: List<Contact>,
    val email: String,
    val functies: List<Functie>,
    val groepseigenVelden: Map<String, GroepseigenVelden>,
)

data class Persoonsgegevens(
    val geslacht: String?,
    val gsm: String?,
    val beperking: Boolean,
    val verminderdlidgeld: Boolean,
    val rekeningnummer: String?
)

data class Vgagegevens(
    val voornaam: String, // only used at user creation
    val achternaam: String, // only used at user creation
    val geboortedatum: String
)

data class Verbondsgegevens(
    val lidnummer: String,
    val klantnummer: String, // ignored
    val lidgeldbetaald: Boolean, // ignored
    val lidkaartafgedrukt: Boolean // ignored
)

data class Adres(
    val id: String,
    val land: String,
    val postcode: String,
    val gemeente: String,
    val straat: String,
    val giscode: String?, // ignored
    val nummer: String,
    val bus: String?,
    val telefoon: String?, // ignored
    val postadres: Boolean,
    val omschrijving: String?,
    val status: String?, // ignored
)

data class Contact(
    val id: String, // ignored
    val voornaam: String,
    val achternaam: String,
    val rol: String,
    val adresId: String?,
    val gsm: String?,
    val email: String?
)

data class Functie(
    val groep: String,
    val functie: String,
    val begin: String,
    val einde: String?,
)

data class GroepseigenVelden(val waarden: Map<String, String?>)
