package laz.dimboba.sounddetection.mobileserver.record

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RecordRepository: JpaRepository<RecordEntity, Long> {
    @Query(
        """
            select * from records
            where user_id = :userId and created_at < :before
            order by created_at desc 
            limit :limit
        """,
        nativeQuery = true,
    )
    fun findByUserIdPaged(userId: Long, before: Instant, limit: Int): List<RecordEntity>
}