package laz.dimboba.sounddetection.mobileserver.record

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

@RestController
@RequestMapping("/api/record")
@SecurityRequirement(name = "bearerAuth")
class RecordController(
    private val manager: RecordManager
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createRecord(
        @RequestPart("file") multipartFile: MultipartFile,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<RecordDto> {
        return ResponseEntity.ok(manager.createRecord(userId, multipartFile))
    }

    @GetMapping("/{id}")
    fun getRecord(@PathVariable id: Long): ResponseEntity<RecordDto> {
        return ResponseEntity.ok(manager.getById(id))
    }

    @GetMapping("/{id}/download", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun downloadRecord(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val objectStream = manager.downloadFile(id)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"${objectStream.`object`()}\"")
            .body(objectStream.readAllBytes())
    }

    @GetMapping
    fun getRecords(
        @RequestParam before: Instant,
        @RequestParam limit: Int,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<List<RecordDto>> {
        return ResponseEntity.ok(
            manager.getRecordsPagedForUser(userId, before, limit)
        )
    }

}