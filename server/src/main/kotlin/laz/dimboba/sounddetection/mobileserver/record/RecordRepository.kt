package laz.dimboba.sounddetection.mobileserver.record

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecordRepository: JpaRepository<RecordEntity, Long>