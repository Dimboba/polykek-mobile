package laz.dimboba.sounddetection.mobileserver

import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [MinioProperties::class])
class AppConfiguration(
    private val minioProperties: MinioProperties
) {
    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(minioProperties.url)
            .credentials(minioProperties.username, minioProperties.password)
            .build()
    }
}

@ConfigurationProperties("app.minio")
data class MinioProperties(
    val username: String,
    val password: String,
    val url: String,
    val soundBucket: String
)