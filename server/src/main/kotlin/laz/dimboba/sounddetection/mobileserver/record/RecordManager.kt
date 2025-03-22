package laz.dimboba.sounddetection.mobileserver.record

import io.minio.GetObjectArgs
import io.minio.GetObjectResponse
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
class RecordManager(
    private val recordRepository: RecordRepository,
    private val minioClient: MinioClient,
    private val pitchDetector: PitchDetector
) {
    @Value("\${app.minio.sound-bucket}")
    private lateinit var soundBucketName: String

    fun createRecord(userId: Long, file: MultipartFile): RecordDto {
        //Thread.sleep(5000)
        val fileName = UUID.randomUUID().toString() + (file.originalFilename ?: "")
        val record = RecordEntity(
            id = null,
            userId = userId,
            fileName = fileName,
            note = "undefined",
            createdAt = Instant.now()
        )

        val mediaType = MediaTypeFactory.getMediaType(file.originalFilename)
            .orElseThrow { IllegalArgumentException("Unexpected file type ${file.originalFilename}") }

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(soundBucketName)
                .`object`(fileName)
                .stream(file.inputStream, file.size, -1)
                .contentType(mediaType.toString())
                .build()
        )

        val tempFile = File.createTempFile("audio-temp-file", ".m4a")
        FileOutputStream(tempFile).use { fos -> fos.write(file.bytes) }
        val result = pitchDetector.detectPitch(tempFile)
        if (result != null) {
            record.note = result.first
            record.octave = result.second
        }
        val entity = recordRepository.save(record)
        //todo: add note recognition
        return entity.toDto()
    }

    @Transactional
    fun getRecordsPagedForUser(userId: Long, before: Instant = Instant.now(), limit: Int = 20): List<RecordDto> {
        return recordRepository.findByUserIdPaged(userId, before, limit)
            .map { it.toDto() }
    }

    @Transactional
    fun getById(recordId: Long): RecordDto? {
        return recordRepository.findById(recordId)
            .map { it.toDto() }
            .getOrNull()
    }

    @Transactional
    fun downloadFile(recordId: Long): GetObjectResponse {
        val record = recordRepository.findById(recordId)
            .orElseThrow { RuntimeException("Record not found") }
        val response = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(soundBucketName)
                .`object`(record.fileName)
                .build()
        )
        return response
    }

    @Transactional
    fun getRecordCountForUser(userId: Long): Long {
        return recordRepository.countByUserId(userId)
    }

    @Transactional
    fun getMostPopularNotes(userId: Long): NoteCountDto {
        val projections = recordRepository.getMostPopularNotesForUser(userId)
        return NoteCountDto(
            projections.map { it.getField() },
            projections.firstOrNull()?.getCount() ?: 0
        )
    }

    @Transactional
    fun getMostPopularOctaves(userId: Long): OctaveCountDto {
        val projections = recordRepository.getMostPopularOctavesForUser(userId)
        return OctaveCountDto(
            projections.map { it.getField() },
            projections.firstOrNull()?.getCount() ?: 0
        )
    }

    @Transactional
    fun getLastRecordForUser(userId: Long): RecordDto? {
        return recordRepository.getNewestRecordsByUserId(userId, 1)
            .firstOrNull()
            ?.toDto()
    }

}