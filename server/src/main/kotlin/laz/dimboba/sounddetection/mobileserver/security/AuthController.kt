package laz.dimboba.sounddetection.mobileserver.security

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import laz.dimboba.sounddetection.mobileserver.user.UserDto
import laz.dimboba.sounddetection.mobileserver.user.UserManager
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenUtil: JwtTokenUtil,
    private val userManager: UserManager
) {

    @PostMapping("/login")
    fun logIn(@RequestBody loginRequest: @Valid LoginRequest): ResponseEntity<LoginRegisterResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )

        val user = userManager.loadUserByUsername(loginRequest.username)
        val access = jwtTokenUtil.generateAccessToken(user)
        val refresh = jwtTokenUtil.generateRefreshToken(user)

        return ResponseEntity.ok(LoginRegisterResponse(access, refresh, user.toDto()))
    }

    @PostMapping("/signup")
    fun register(@RequestBody registerRequest: @Valid RegisterRequest): ResponseEntity<LoginRegisterResponse> {
        val user = userManager.createUser(registerRequest)
        val access = jwtTokenUtil.generateAccessToken(user)
        val refresh = jwtTokenUtil.generateRefreshToken(user)

        return ResponseEntity.ok(LoginRegisterResponse(access, refresh, user.toDto()))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: @Valid RefreshRequest): ResponseEntity<LoginRegisterResponse> {
        val username = jwtTokenUtil.extractUsername(request.refreshToken)
        val user = userManager.loadUserByUsername(username)
        if(!jwtTokenUtil.validateRefreshToken(request.refreshToken)) {
            //todo: remake all of this AuthenticationExceptions and check for status codes
            throw object : AuthenticationException("User not found") {
            }
        }
        val access = jwtTokenUtil.generateAccessToken(user)
        val refresh = jwtTokenUtil.generateRefreshToken(user)

        return ResponseEntity.ok(LoginRegisterResponse(access, refresh, user.toDto()))
    }

    data class RegisterRequest(
        val username: @NotBlank String,
        val password: @NotBlank String
    )

    data class LoginRegisterResponse(
        val accessToken: String,
        val refreshToken: String,
        val user: UserDto
    )

    data class RefreshRequest(
        val refreshToken: String
    )

    data class LoginRequest(
        val username: @NotBlank String,
        val password: @NotBlank String
    )
}