package laz.dimboba.sounddetection.mobileserver.user

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val manager: UserManager,
) {
    @PutMapping
    fun updateUser(
        @AuthenticationPrincipal id: Long,
        @RequestBody userRequest: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok(manager.updateUser(id, userRequest))
    }
}