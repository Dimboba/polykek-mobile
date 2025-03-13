package laz.dimboba.sounddetection.mobileserver.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DefaultAuthenticationManager(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationManager {

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name
        val password = authentication.credentials as String

        val userDetails: UserDetails
        try {
            userDetails = userDetailsService.loadUserByUsername(username)
        } catch (e: UsernameNotFoundException) {
            throw object : AuthenticationException("User not found") {
            }
        }

        if (passwordEncoder.matches(password, userDetails.password)) {
            return UsernamePasswordAuthenticationToken(userDetails, password, userDetails.authorities)
        } else {
            throw object : AuthenticationException("Bad credentials") {
            }
        }
    }
}
