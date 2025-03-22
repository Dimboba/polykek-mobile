package laz.dimboba.sounddetection.mobileserver.record

import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.hibernate.type.descriptor.jdbc.SmallIntJdbcType
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(name = "records")
class RecordEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var userId: Long? = null,
    var fileName: String? = null,
    var note: String? = null,
    @Column(columnDefinition = "int2")
    var octave: Int? = null,
    @CreatedDate
    var createdAt: Instant? = null,
) {
    fun toDto() = RecordDto(
        id!!, note!!, octave, createdAt!!
    )
}