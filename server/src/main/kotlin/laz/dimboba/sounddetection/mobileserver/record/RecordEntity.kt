package laz.dimboba.sounddetection.mobileserver.record

import jakarta.persistence.*
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
    @CreatedDate
    var createdAt: Instant? = null,
) {
    fun toDto() = RecordDto(
        id!!, fileName!!, note!!, createdAt!!
    )
}