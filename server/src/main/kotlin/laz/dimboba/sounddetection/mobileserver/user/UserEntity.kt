package laz.dimboba.sounddetection.mobileserver.user

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant

@Entity
@Table(name = "users")
class UserEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "username")
    val userName: String? = null,
    @Column(name = "password")
    val userPassword: String? = null,
    @CreatedDate
    val createdAt: Instant? = null
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
    }

    override fun getPassword(): String? = userPassword

    override fun getUsername(): String? = userName

    fun toDto(): UserDto {
        return UserDto(id!!, userName!!, createdAt!!)
    }
}