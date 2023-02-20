import android.util.Log
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
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

        private fun provideOkHttpClient(
            interceptor: AppInterceptor
        ): OkHttpClient = OkHttpClient.Builder()
            .run {
                addInterceptor(interceptor)
                build()
            }

        class AppInterceptor : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain)
                    : Response = with(chain) {
                val auth = MainActivity.prefs.getString("Authentication", "")
                val newRequest = request().newBuilder()
                    .addHeader("Authorization", auth)
                    .build()
                proceed(newRequest)
            }
        }

        /*protected fun getRedirectInterceptor(): Interceptor? {
            return Interceptor { chain ->
                var request = chain.request()
                var response = chain.proceed(request)
                if (response.code() == 308) {
                    request = request.newBuilder()
                        .url(response.header("Location"))
                        .build()
                    response = chain.proceed(request)
                }
                response
            }
        }*/
    }
}
