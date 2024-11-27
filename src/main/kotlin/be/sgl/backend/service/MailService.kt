package be.sgl.backend.service

import jakarta.mail.util.ByteArrayDataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.io.File

@Service
class MailService {

    @Autowired
    private lateinit var mailSender: JavaMailSender
    @Autowired
    private lateinit var templateEngine: SpringTemplateEngine

    fun builder(): MailBuilder {
        return MailBuilder()
    }

    private fun loadTemplate(templateName: String, placeholders: Map<String, Any>): String {
        val locale = LocaleContextHolder.getLocale()
        return templateEngine.process(templateName, Context(locale, placeholders))
    }

    inner class MailBuilder {

        private lateinit var from: String
        private val to = mutableListOf<String>()
        private lateinit var subject: String
        private lateinit var body: String
        private val cc = mutableListOf<String>()
        private val attachments = mutableListOf<Attachment>()

        fun from(from: String) = apply { this.from = from }

        fun to(vararg to: String) = apply { this.to.addAll(to) }

        fun subject(subject: String) = apply { this.subject = subject }

        fun body(body: String) = apply { this.body = body }

        fun template(templateName: String, placeholders: Map<String, Any> = emptyMap()) = apply {
            this.body = loadTemplate(templateName, placeholders)
        }

        fun addAttachment(content: File, name: String = content.name) = apply {
            attachments.add(FileAttachment(name, content))
        }

        fun addAttachment(content: ByteArray, name: String, mimeType: String) = apply {
            attachments.add(ByteArrayAttachment(name, content, mimeType))
        }

        fun send() {
            try {
                val mimeMessage = mailSender.createMimeMessage()
                val helper = MimeMessageHelper(mimeMessage, true)
                helper.setFrom(from)
                to.forEach(helper::addTo)
                helper.setSubject(subject)
                helper.setText(body, true)
                cc.forEach(helper::addCc)
                attachments.forEach { it.addAttachment(helper) }
                mailSender.send(mimeMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    sealed class Attachment(val name: String) {
        abstract fun addAttachment(helper: MimeMessageHelper)
    }

    class FileAttachment(name: String, private val file: File) : Attachment(name) {
        override fun addAttachment(helper: MimeMessageHelper) {
            helper.addAttachment(name, file)
        }
    }

    class ByteArrayAttachment(name: String, private val content: ByteArray, private val mimeType: String) : Attachment(name) {
        override fun addAttachment(helper: MimeMessageHelper) {
            helper.addAttachment(name, ByteArrayDataSource(content, mimeType))
        }
    }
}