package laz.dimboba.sounddetection.mobileserver.security

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtTokenFilter(
    private val jwtTokenUtil: JwtTokenUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authorizationHeader: String? = request.getHeader("Authorization")

        var username: String? = null
        var jwt: String? = null
        var id: Long? = null

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                jwt = authorizationHeader.substring(7)
                username = jwtTokenUtil.extractUsername(jwt)
                id = jwtTokenUtil.extractId(jwt)
            } catch (e: AuthException) {
                response.status = HttpServletResponse.SC_FORBIDDEN
            }
        }

        if (username != null && SecurityContextHolder.getContext().authentication == null) {

            if (jwt != null && jwtTokenUtil.validateAccessToken(jwt)) {
                val usernamePasswordAuthenticationToken =
                    UsernamePasswordAuthenticationToken(id, null, mutableListOf())
                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
        }
        chain.doFilter(request, response)
    }
}