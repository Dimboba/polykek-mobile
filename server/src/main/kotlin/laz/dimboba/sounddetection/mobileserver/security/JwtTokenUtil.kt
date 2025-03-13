package laz.dimboba.sounddetection.mobileserver.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import laz.dimboba.sounddetection.mobileserver.user.UserEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*
import java.util.function.Function


@Component
class JwtTokenUtil {
    @Value("\${app.auth.secret}")
    private lateinit var secretKey: String

    @Value("\${app.auth.access-token-time-to-live-ms}")
    private var accessTtl: Long = 0

    @Value("\${app.auth.refresh-token-time-to-live-ms}")
    private var refreshTtl: Long = 0

    private final val accessString = "access"
    private final val refreshString = "refresh"
    private final val typeKey = "type"
    private final val idKey = "id"

    fun extractUsername(token: String): String {
        return extractClaim(token) { obj: Claims -> obj.subject }
    }

    fun extractId(token: String): Long {
        return extractClaim(token) { obj: Claims -> obj[idKey].toString().toLong() }
    }

    fun extractExpiration(token: String): Date {
        return extractClaim(token) { obj: Claims -> obj.expiration }
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        val key: Key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun generateAccessToken(userDetails: UserEntity): String = generateToken(userDetails, accessString, accessTtl)

    fun generateRefreshToken(userDetails: UserEntity): String = generateToken(userDetails, refreshString, refreshTtl)

    private fun generateToken(userDetails: UserEntity, type: String, expirationTime: Long): String {
        val claims: MutableMap<String, Any?> = mutableMapOf()
        claims[typeKey] = type
        claims[idKey] = userDetails.id!!

        val key: Key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(key, SignatureAlgorithm.HS256).compact()
    }

    fun validateAccessToken(token: String): Boolean =
        validateToken(token, accessString)

    fun validateRefreshToken(token: String): Boolean =
        validateToken(token, refreshString)

    private fun validateToken(token: String, tokenType: String): Boolean {
        val type = extractClaim(token) { obj: Claims -> obj[typeKey] }
        return type is String
            && type == tokenType
            && !isTokenExpired(token)
    }

}