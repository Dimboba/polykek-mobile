package laz.dimboba.sounddetection.mobileserver.stats

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
@SecurityRequirement(name = "bearerAuth")
class StatsController(
    private val manager: StatsManager
) {
    @GetMapping
    fun getStats(@AuthenticationPrincipal userId: Long): ResponseEntity<StatsDto> {
        return ResponseEntity.ok(manager.getStatsForUser(userId))
    }
}