package laz.dimboba.sounddetection.mobileserver.record

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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
    @GetMapping
    fun getRecord(): ResponseEntity<String> {
        return ResponseEntity.ok("OK")
    }
}