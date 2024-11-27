package be.sgl.backend.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class Auditable : Serializable {
    @CreatedBy
    var createdBy: String? = null
    @CreatedDate
    var createdDate: LocalDateTime? = null
    @LastModifiedBy
    var lastModifiedBy: String? = null
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null
    @Version
    var version: Int = 0
}