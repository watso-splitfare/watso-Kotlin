import android.util.Log
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.lang.Exception



interface APIS:AuthAPIS, BaedalAPIS, TaxiAPIS, AdminAPIS {

    /*interface ResListener {
        fun newres(res: Response)
    }

    class getNewRes(listener: ResListener) {
        var mCallback = listener

    }*/

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
            /*override fun intercept(chain: Interceptor.Chain):
                Response = with(chain) {
                    val auth = MainActivity.prefs.getString("Authentication", "")
                    val newRequest = request().newBuilder()
                        .addHeader("Authorization", auth)
                        .build()
                    proceed(newRequest)
            }*/

            override fun intercept(chain: Interceptor.Chain): Response {
                var accessToken = MainActivity.prefs.getString("Authentication", "")
                var request = chain.request().newBuilder()
                    .addHeader("Authorization", accessToken)
                    .build()
                var res = chain.proceed(request);

                if (res.code() == 401) {

                    //lateinit var newRes: Response
                    val auth = AuthA.create()
                    val refreshToken = MainActivity.prefs.getString("refresh", "")
                    Log.d("토큰 갱신 시도 access", accessToken)
                    Log.d("토큰 갱신 시도 refresh", refreshToken)
                    auth.refresh(refreshToken).enqueue(object : Callback<RefreshResult> {
                        override fun onResponse(call: Call<RefreshResult>, response: retrofit2.Response<RefreshResult>) {
                            if (response.code() == 200) {
                                val tokens = response.headers().get("Authentication").toString().split("/")
                                MainActivity.prefs.setString("Authentication", tokens[0])
                                MainActivity.prefs.setString("refresh", tokens[1])
                                Log.d("어세스 토큰 갱신 성공 access: ", tokens[0])
                                Log.d("어세스 토큰 갱신 성공 refresh: ", tokens[1])

                                accessToken = MainActivity.prefs.getString("Authentication", "")
                                request = chain.request().newBuilder()
                                    .addHeader("Authorization", accessToken)
                                    .build()
                                res = chain.proceed(request);

                            } else {
                                Log.e("어세스 토큰 갱신 실패1", response.toString())
                                Log.e("어세스 토큰 갱신 실패1", response.body().toString())
                                Log.e("어세스 토큰 갱신 실패1", response.headers().toString())
                            }
                        }

                        override fun onFailure(call: Call<RefreshResult>, t: Throwable) {
                            Log.e("어세스 토큰 갱신 실패2", t.message.toString())
                        }
                    })
                    return res
                }
                else {
                    return res
                }
            }

        }

    }
}

