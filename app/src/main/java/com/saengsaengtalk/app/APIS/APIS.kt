import android.util.Log
import com.saengsaengtalk.app.APIS.*
import com.saengsaengtalk.app.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

interface APIS:AuthAPIS, BaedalAPIS, TaxiAPIS, AdminAPIS {

    companion object {
        private const val BASE_URL = "http://52.78.106.235:5000/"

        fun create(): APIS {
            val gson :Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideOkHttpClient(AppInterceptor()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(APIS::class.java)
        }

        private fun provideOkHttpClient(interceptor: AppInterceptor):
                OkHttpClient = OkHttpClient.Builder().run {
                addInterceptor(interceptor)
                build()
            }

        class AppInterceptor : Interceptor { @Throws(IOException::class)

            override fun intercept(chain: Interceptor.Chain): Response {
                val accessToken = MainActivity.prefs.getString("accessToken", "")
                val refreshToken = MainActivity.prefs.getString("refreshToken", "")
                val tokenAddedRequest = chain.request().newBuilder()
                    .addHeader("Authorization", accessToken)
                    .build()

                val response = chain.proceed(tokenAddedRequest)

                if (response.code() == 401) {
                    response.close()
                    Log.d("어세스 토큰 갱신 시도 access", accessToken)
                    Log.d("어세스 토큰 갱신 시도 refresh", refreshToken)
                    val refreshRequest = chain.request().newBuilder()
                        .addHeader("Authorization", refreshToken)
                        .method("GET", null)
                        .url(BASE_URL + "auth/signin/refresh")
                        .build()

                    val refreshResponse = chain.proceed(refreshRequest)
                    val token = refreshResponse.headers().get("Authentication").toString()
                    MainActivity.prefs.setString("accessToken", token)

                    Log.d("어세스 토큰 갱신 성공", token)
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", token)
                        .build()
                    return chain.proceed(newRequest)
                }
                return response
            }
        }
    }
}
