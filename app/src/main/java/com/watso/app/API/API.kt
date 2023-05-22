import android.os.Build
import android.util.Log
import com.watso.app.API.*
import com.watso.app.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

interface API:AuthAPI, UserAPI, BaedalAPI, TaxiAPIS, AdminAPIS {

    companion object {
        private val TAG = "API"
        //private const val BASE_URL = "https://24489c78-e8fa-4f59-9466-05c9d568ce74.mock.pstmn.io/"
        private const val BASE_URL = "https://api.watso.kr/"
        private val EXCEPTION_URL = listOf(
            "${BASE_URL}auth/login",
            "${BASE_URL}user/signup",
            "${BASE_URL}user/signup/validation-check",
            "${BASE_URL}user/forgot/password"
        )
        fun create(): API {
            val gson :Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideOkHttpClient(AppInterceptor()))
                .addConverterFactory(nullOnEmptyConverterFactory)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(API::class.java)
        }

        private fun provideOkHttpClient(interceptor: AppInterceptor):
                OkHttpClient = OkHttpClient.Builder().run {
                addInterceptor(interceptor)
                build()
            }

        class AppInterceptor : Interceptor { @Throws(IOException::class)

            override fun intercept(chain: Interceptor.Chain): Response {
                val prefs = MainActivity.prefs
                val accessToken = prefs.getString("accessToken", "")
                val refreshToken = prefs.getString("refreshToken", "")

                val targetUrl = chain.request().url().toString()
                Log.d("$TAG[targetURL]", targetUrl)

                if (targetUrl == BASE_URL + "auth/refresh") {
                    Log.d("[${TAG}]refresh token", refreshToken)
                    val tokenAddedRequest = chain.request().newBuilder()
                        .addHeader("Authorization", refreshToken)
                        .build()
                    val response = chain.proceed(tokenAddedRequest)
                    if (response.code() == 200) {
                        val token = response.headers().get("Authentication").toString()
                        val payload = decodeToken(token)
                        val dUserId = JSONObject(payload).getString("user_id")
                        val dNickname = JSONObject(payload).getString("nickname")
                        prefs.setString("accessToken", token)
                        prefs.setString("userId", dUserId)
                        prefs.setString("nickname", dNickname)
                        Log.d("어세스 토큰 갱신 성공", token)
                        Log.d("어세스 토큰 갱신 성공", dUserId)
                        Log.d("어세스 토큰 갱신 성공", dNickname)
                    }
                    return response
                }

                val tokenAddedRequest = chain.request().newBuilder()
                    .addHeader("Authorization", accessToken)
                    .build()
                val response = chain.proceed(tokenAddedRequest)

                if (response.code() == 401) {
                    var isExpired = true
                    EXCEPTION_URL.forEach {
                        if (targetUrl.contains(it)) isExpired = false
                    }
                    if (isExpired) {
                        Log.d("API", "토큰 만료")
                        response.close()
                        Log.d("어세스 토큰 갱신 시도 access", accessToken)
                        Log.d("어세스 토큰 갱신 시도 refresh", refreshToken)
                        val refreshRequest = chain.request().newBuilder()
                            .addHeader("Authorization", refreshToken)
                            .method("GET", null)
                            .url(BASE_URL + "auth/refresh")
                            .build()

                        val refreshResponse = chain.proceed(refreshRequest)
                        if (refreshResponse.code() == 200) {
                            val token =
                                refreshResponse.headers().get("Authentication").toString()
                            prefs.setString("accessToken", token)

                            Log.d("[$TAG]어세스 토큰 갱신 성공", token)
                            val newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", token)
                                .build()

                            val newRes = chain.proceed(newRequest)
                            Log.d("API intercept newRes", newRes.toString())
                            Log.d("API intercept newRes.code", newRes.code().toString())
                            return newRes
                        } else {
                            Log.d("[$TAG]어세스 토큰 갱신 실패", "")
                            return refreshResponse
                        }
                    }
                }
                return response
            }
        }

        private val nullOnEmptyConverterFactory = object : Converter.Factory() {
            fun converterFactory() = this
            override fun responseBodyConverter(
                type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
            ) = object : Converter<ResponseBody, Any?> {
                val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(
                    converterFactory(), type, annotations
                )
                override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) {
                    try{ nextResponseBodyConverter.convert(value) }
                    catch (e:Exception){
                        e.printStackTrace()
                        null
                    }
                } else{
                    null
                }
            }
        }
    }
}

fun decodeToken(jwt: String): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Requires SDK 26"
    val parts = jwt.split(".")
    return try {
        val charset = charset("UTF-8")
        val header = String(Base64.getUrlDecoder().decode(parts[0].toByteArray(charset)), charset)
        val payload = String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
        "$header"
        "$payload"
    } catch (e: Exception) {
        "Error parsing JWT: $e"
    }
}

