package laz.dimboba.sounddetection.mobileserver.record

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RecordRepository : JpaRepository<RecordEntity, Long> {
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

    fun countByUserId(userId: Long): Long

    @Query(
        """
            with x as (
                select count(*) as cnt, note as cnt_note
                from records
                where user_id = :userId
                group by note
            )
            select x.cnt_note as field, x.cnt as cnt from x 
                where x.cnt = (
                    select max(x.cnt) from x
                )
        """,
        nativeQuery = true,
    )
    fun getMostPopularNotesForUser(userId: Long): List<FieldCountProjection<String>>

    @Query(
        """
            with x as (
                select count(*) as cnt, octave as cnt_octave
                from records
                where user_id = :userId and octave is not null
                group by octave
            )
            select x.cnt_octave as field, x.cnt from x 
                where x.cnt = (
                    select max(x.cnt) from x
                )
        """,
        nativeQuery = true,
    )
    fun getMostPopularOctavesForUser(userId: Long): List<FieldCountProjection<Int>>

    @Query(
        """
            select * from records
            where user_id = :userId
            order by created_at desc
            limit :limit
        """,
        nativeQuery = true,
    )
    fun getNewestRecordsByUserId(userId: Long, limit: Int): List<RecordEntity>
}

interface FieldCountProjection<T> {
    @Value("#{target.field}")
    fun getField(): T

    @Value("#{target.cnt}")
    fun getCount(): Long
}
