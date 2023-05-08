import android.util.Log
import com.watso.app.API.*
import com.watso.app.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.reflect.Type

interface API:AuthAPI, UserAPI, BaedalAPI, TaxiAPIS, AdminAPIS {

    companion object {
        //private const val BASE_URL = "https://24489c78-e8fa-4f59-9466-05c9d568ce74.mock.pstmn.io/"
        private const val BASE_URL = "http://129.154.49.156/api/"
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

                val tokenAddedRequest = chain.request().newBuilder()
                    .addHeader("Authorization", accessToken)
                    .build()

                val response = chain.proceed(tokenAddedRequest)

                if (response.code() == 401 && (targetUrl !in EXCEPTION_URL)) {
                    try {
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
                        val token = refreshResponse.headers().get("Authentication").toString()
                        prefs.setString("accessToken", token)

                        Log.d("어세스 토큰 갱신 성공", token)
                        val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", token)
                            .build()

                        val newRes = chain.proceed(newRequest)
                        Log.d("API intercept newRes", newRes.toString())
                        Log.d("API intercept newRes.code", newRes.code().toString())
                        return newRes
                    } catch (e: Exception) { }
                    finally { }
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

