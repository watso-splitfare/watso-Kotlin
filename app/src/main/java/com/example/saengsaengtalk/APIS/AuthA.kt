import android.util.Log
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Type


interface AuthA {
    @GET("auth/logout")                    // 105 로그아웃
    fun logout(
        @Header("Authorization") refreshToken: String
    ): Call<LogoutResult>

    @GET("auth/signin/refresh")             // 토큰 갱신
    fun refresh(
        @Header("Authentication") refreshToken: String
    ): Call<RefreshResult>

    companion object {
        private const val BASE_URL = "http://52.78.106.235:5000/"

        private val nullOnEmptyConverterFactory = object : Converter.Factory() {
            fun converterFactory() = this
            override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object : Converter<ResponseBody, Any?> {
                val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
                override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) {
                    try{
                        nextResponseBodyConverter.convert(value)
                    }catch (e:Exception){
                        e.printStackTrace()
                        null
                    }
                } else{
                    null
                }
            }
        }

        fun create(): AuthA {

            val gson :Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(AuthA.BASE_URL)
                .addConverterFactory(nullOnEmptyConverterFactory)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(AuthA::class.java)
        }

    }
}
