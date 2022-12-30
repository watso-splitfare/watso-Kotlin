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
        private const val BASE_URL = "http://52.78.106.235:5000/" //"http://59.8.74.204:5000/" //"http://10.0.2.2:5000/" //"http://52.78.106.235:5000/"
        //private const val BASE_URL = "http://10.0.2.2:5000/"

        fun create(): APIS {
            //prefs.setString("Authentication", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTY2Mzk4OTg4OTgyNiwibmlja19uYW1lIjoiYm9uZyJ9.FULK5UjhV7UnoRa8lUP7MrW0wccROJf9GUp7bac1tvo")
            //Log.d("auth 키", auth)

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
                // 쿠키에 저장된 유저 정보를 불러오는 부분인데 개발 과정에서 더미데이터가 필요해서
                // authDebug 라는 String을 정의해서 사용했습니다.
                // 실제 서비스시에는 getString의 defValue 를 authDebug가 아닌 ""으로 넣어주면 됩니다.

                //var auth = MainActivity.prefs.getString("Authentication", "")
                val authDebug = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTY2Mzk4OTg4OTgyNiwibmlja19uYW1lIjoiYm9uZyJ9.FULK5UjhV7UnoRa8lUP7MrW0wccROJf9GUp7bac1tvo"
                val auth = MainActivity.prefs.getString("Authentication", authDebug)
                //Log.d("auth 키-인터셉터", auth)
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
