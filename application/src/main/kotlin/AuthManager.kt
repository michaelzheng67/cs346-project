import com.auth0.jwt.JWT
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.apache.commons.codec.binary.Base64
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.serialization.json.Json

// Credit to Sean Proctor (github.com/sproctor) for the base of the Compose Desktop auth0 implementation
class AuthManager {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val callbackJob = MutableStateFlow<Job?>(null)

    val isLoggingIn = callbackJob.map { it?.isActive == true }

    var accessToken = ""
    var userName = ""

    private val _userId = MutableStateFlow("")
    val userId = _userId.asStateFlow()

    fun authenticateUser(
        domain: String,
        clientId: String,
        redirectUri: String,
        scope: String,
        audience: String,
    ) {
        val job = coroutineScope.launch {
            try {
                val verifier = createVerifier()
                val challenge = createChallenge(verifier)
                val url = createLoginUrl(
                    domain = domain,
                    clientId = clientId,
                    redirectUri = redirectUri,
                    scope = scope,
                    challenge = challenge,
                    audience = audience,
                )

                println("Launching URL: $url")

                withContext(Dispatchers.IO) {
                    Desktop.getDesktop().browse(URI(url))
                }

                val code = waitForCallback()

                getToken(
                    domain = domain,
                    clientId = clientId,
                    verifier = verifier,
                    code = code,
                    redirectUri = redirectUri,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        callbackJob.value = job
        job.invokeOnCompletion { callbackJob.value = null }
    }

    private suspend fun getToken(
        domain: String,
        clientId: String,
        verifier: String,
        code: String,
        redirectUri: String,
    ) {
        val encodedRedirectUri = URLEncoder.encode(redirectUri, Charsets.UTF_8)

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }


        val response = client.post("https://$domain/oauth/token") {
            headers {
                append("content-type", "application/x-www-form-urlencoded")
            }
            setBody(
                "grant_type=authorization_code&client_id=$clientId&code_verifier=$verifier" +
                        "&code=$code&redirect_uri=$encodedRedirectUri"
            )
        }

        val responseBody = response.toString()
        val responseBodyAsText = response.bodyAsText()
        println("response: $response")
        println("responseBody: $responseBody")
        println("readtext: ${responseBodyAsText}")
        val gson = GsonBuilder().create()
        val responseInfo = gson.fromJson(responseBodyAsText, TokenResponse::class.java)
        println(responseInfo)
        accessToken = responseInfo.access_token
        _userId.value = extractUserId(responseInfo.access_token)

        val client2 = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://$domain/userinfo"))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .GET()
            .build()
        val response2 = client2.send(request, HttpResponse.BodyHandlers.ofString())
        val decoded = Json.decodeFromString<Boolean>(response.body())
        userName = responseInfo.name
    }

    private fun extractUserId(token: String): String {
        val decodedJwt = JWT.decode(token)
        return decodedJwt.getClaim("sub").asString().split("|")[1]
    }

    fun getAuthId(): String {
        return _userId.value
    }

    private suspend fun waitForCallback(): String {
        var server: NettyApplicationEngine? = null

        val code = suspendCancellableCoroutine<String> { continuation ->
            server = embeddedServer(Netty, port = 5789) {
                routing {
                    get("/callback") {
                        val code = call.parameters["code"] ?: throw RuntimeException("Received a response with no code")
                        println("got code: $code")
                        call.respondText("OK")

                        continuation.resume(code)
                    }
                }
            }.start(wait = false)
        }

        coroutineScope.launch {
            server!!.stop(1, 5, TimeUnit.SECONDS)
        }

        return code
    }

    private fun createLoginUrl(
        domain: String,
        clientId: String,
        redirectUri: String,
        scope: String,
        challenge: String,
        audience: String,
    ): String {
        val encodedRedirectUri = URLEncoder.encode(redirectUri, Charsets.UTF_8)
        val encodedScope = URLEncoder.encode(scope, Charsets.UTF_8)
        val encodedAudience = URLEncoder.encode(audience, Charsets.UTF_8)
        return "https://$domain/authorize?response_type=code&code_challenge=$challenge" +
                "&code_challenge_method=S256&client_id=$clientId&redirect_uri=$encodedRedirectUri" +
                "&scope=$encodedScope&audience=$encodedAudience"
    }

    private fun createVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(code)
    }

    private fun createChallenge(verifier: String): String {
        val bytes: ByteArray = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(bytes, 0, bytes.size)
        val digest = md.digest()
        return Base64.encodeBase64URLSafeString(digest)
    }

    fun cancelLogin() {
        callbackJob.value?.cancel()
        callbackJob.value = null
    }
}

@Serializable
data class TokenResponse(
    var access_token: String,
    var id_token: String,
    var scope: String,
    var expires_in: Int,
    var token_type: String,
    var name: String,
)

//@Serializable
//data class UserinfoResponse (
//    val name
//)