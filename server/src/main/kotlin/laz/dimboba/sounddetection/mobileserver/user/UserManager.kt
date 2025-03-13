package laz.dimboba.sounddetection.mobileserver.user

import laz.dimboba.sounddetection.mobileserver.security.AuthController
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserManager (
    private val encoder: PasswordEncoder,
    private val userRepository: UserRepository,
): UserDetailsService {

    fun createUser(request: AuthController.RegisterRequest): UserEntity {
        val user = UserEntity(
            userName = request.username,
            userPassword = encoder.encode(request.password),
            createdAt = Instant.now()
        )
        return userRepository.save(user)
    }

    override fun loadUserByUsername(username: String): UserEntity {
        return userRepository.findByUserName(username) ?: throw UsernameNotFoundException("User $username not found")
    }
}