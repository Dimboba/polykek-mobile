package laz.dimboba.sounddetection.mobileserver.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<UserEntity, Long> {
    fun findByUserName(username: String): UserEntity?
}