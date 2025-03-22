package laz.dimboba.sounddetection.mobileserver.user

import laz.dimboba.sounddetection.mobileserver.security.AuthController
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class UserManager (
    private val encoder: PasswordEncoder,
    private val userRepository: UserRepository,
): UserDetailsService {

    @Transactional
    fun createUser(request: AuthController.RegisterRequest): UserEntity {
        val user = UserEntity(
            userName = request.username,
            userPassword = encoder.encode(request.password),
            createdAt = Instant.now()
        )
        return userRepository.save(user)
    }

    @Transactional
    fun getUserById(userId: Long): UserDto {
        return userRepository.findById(userId)
            .map { it.toDto() }
            .orElseThrow{ RuntimeException("User not found") }
    }

    @Transactional
    fun updateUser(userId: Long, request: UpdateUserRequest): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow{ RuntimeException("User not found") }

        val updatedUser = UserEntity(
            id = user.id,
            userName = request.username ?: user.userName,
            userPassword = request.password?.apply { encoder.encode(this) } ?: user.userPassword,
            createdAt = user.createdAt ?: error("Wrong user state")
        )

        return userRepository.save(updatedUser)
            .toDto()
    }

    @Transactional
    override fun loadUserByUsername(username: String): UserEntity {
        return userRepository.findByUserName(username) ?: throw UsernameNotFoundException("User $username not found")
    }
}