package be.sgl.backend.controller

import be.sgl.backend.config.CustomUserDetails
import be.sgl.backend.config.security.LevelSecurityService
import be.sgl.backend.service.belcotax.BelcotaxService
import be.sgl.backend.util.zipped
import generated.Verzendingen
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors

@RestController
@RequestMapping("/belcotax")
@Tag(name = "Belcotax", description = "Endpoints for creating Belcotax forms and dispatches.")
class BelcotaxController {

    @Autowired
    private lateinit var levelSecurityService: LevelSecurityService
    @Autowired
    private lateinit var belcotaxService: BelcotaxService

    @GetMapping("/dispatch/{fiscalYear}", produces = ["application/xml"])
    @PreAuthorize("@levelSecurityService.isAdmin()")
    @Operation(summary = "Retrieve the Belcotax dispatch xml file.")
    fun getDispatchForFiscalYear(@PathVariable fiscalYear: Int?, @RequestParam rate: Double?): ResponseEntity<Verzendingen> {
        return ResponseEntity.ok(belcotaxService.getDispatchForFiscalYearAndRate(fiscalYear, rate))
    }

    @GetMapping("/form/{fiscalYear}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retrieve the Belcotax forms for the current user.")
    fun getUserFormsForFiscalYear(@PathVariable fiscalYear: Int?, @RequestParam rate: Double?, @AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<ByteArray> {
        val forms = belcotaxService.getFormsForUserFiscalYearAndRate(userDetails.username, fiscalYear, rate)
        return if (forms.size == 1) {
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"form.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(forms.first())
        } else {
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"forms.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(forms.zipped())
        }
    }

    @GetMapping("/mail/{fiscalYear}")
    @PreAuthorize("@levelSecurityService.isAdmin()")
    @Operation(summary = "Mail the Belcotax forms to all relevant users.")
    fun mailFormsForFiscalYear(@PathVariable fiscalYear: Int?, @RequestParam rate: Double?): SseEmitter {
        val emitter = SseEmitter()
        Executors.newSingleThreadExecutor().submit {
            emitter.send("Generating forms...")
            val forms = belcotaxService.getFormsForFiscalYearAndRate(fiscalYear, rate)
            try {
                forms.onEachIndexed { i, (user, userForms) ->
                    emitter.send("Sending email $i of ${forms.size}")
                    belcotaxService.mailFormsToUser(fiscalYear, user, userForms)
                }
                emitter.send("All emails sent successfully!")
                emitter.complete()
            } catch (e: Exception) {
                emitter.send("Error occurred: ${e.message}")
                emitter.completeWithError(e)
            }
        }
        return emitter
    }
}