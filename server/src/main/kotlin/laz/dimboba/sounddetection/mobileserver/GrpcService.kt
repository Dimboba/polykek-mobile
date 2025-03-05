package laz.dimboba.sounddetection.mobileserver

import TestResponseKt
import org.springframework.stereotype.Component
import testResponse

@Component
class GrpcService: TestServiceGrpcKt.TestServiceCoroutineImplBase () {
    override suspend fun get(request: Test.TestRequest): Test.TestResponse {
        val response: Test.TestResponse = testResponse {
            num = 13
        }
        return response
    }
}