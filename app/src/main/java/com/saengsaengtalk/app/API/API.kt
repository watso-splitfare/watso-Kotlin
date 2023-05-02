import android.util.Log
import com.saengsaengtalk.app.API.*
import com.saengsaengtalk.app.MainActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.saengsaengtalk.app.API.DataModels.ErrorResponse
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
        private const val BASE_URL = "http://52.78.106.235/api/"

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
                val tokenAddedRequest = chain.request().newBuilder()
                    .addHeader("Authorization", accessToken)
                    .build()

                val response = chain.proceed(tokenAddedRequest)

                Log.d("API intercept response", response.toString())
                Log.d("API intercept response.code", response.code().toString())

                if (response.code() == 401) {
                    val body = response.body()?.string()!!
                    val gson = Gson()
                    val errorResponse = gson.fromJson(body, ErrorResponse::class.java)
                    Log.e("API errorResponse.msg", errorResponse.msg)
                    Log.e("API errorResponse.code", errorResponse.code.toString())
                    try {
                        if (errorResponse.code == 202) {
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
                        } else {
                            prefs.removeString("accessToken")
                            prefs.removeString("refreshToken")
                            prefs.removeString("userId")
                            prefs.removeString("nickname")
                        }
                    }
                    catch(e:Exception) { }
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

