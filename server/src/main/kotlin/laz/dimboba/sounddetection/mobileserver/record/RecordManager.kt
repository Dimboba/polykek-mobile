package laz.dimboba.sounddetection.mobileserver.record

import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Component
class RecordManager(
    private val recordRepository: RecordRepository,
    private val minioClient: MinioClient
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

        val entity = recordRepository.save(record)
        //todo: add note recognition
        return entity.toDto()
    }

}