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

interface APIS {
    /** 계정 관련 API */

    @GET("auth/username-overlap-check")
    fun usernameOverlapCheck(
        @Query("user_name") username: String
    ): Call<OverlapResult>

    @GET("auth/nickname-overlap-check")
    fun nicknameOverlapCheck(
        @Query("nickname") nickname: String
    ): Call<OverlapResult>

    @GET("auth/studentnum-overlap-check")
    fun studentnumOverlapCheck(
        @Query("studentnum") studentnum: Int
    ): Call<OverlapResult>

    @POST("auth/signup")
    @Headers("accept: application/json", "content-type: application/json")
    fun signup(
        @Body jsonparams: SignUpModel
    ): Call<SignUpResult>

    @POST("auth/login")
    @Headers("accept: application/json", "content-type: application/json")
    fun login(
        @Body jsonparams: LoginModel
    ): Call<LoginResult>

    @POST("auth/logout")
    fun logout(
    ): Call<LogoutResult>


    /** 배달 게시물 관련 api */

    @GET("order/store/list")                // 201 가게 리스트 조회
    fun getStoreList(): Call<List<StoreListModel>>

    @GET("order/menu/list")                 // 202 가게 메뉴 조회
    fun getSectionMenu(
        @Query("store_id") storeId: Int
    ): Call<List<SectionMenuModel>>

    @GET("order/menu/detail")               // 203 가게 메뉴 디테일 조회
    fun getGroupOption(
        @Query("menu_id") menuId: Int,      // 테스트용(실제 사용 x)
        @Query("menu_name") menuName: String,
        @Query("store_id") storeId: Int
    ): Call<List<GroupOptionModel>>

    @POST("order/post/posting")             // 204 배달 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPostingModel
    ): Call<BaedalPostingResponse>

    @GET("order/post/detail/{post_id}")     // 205 배달 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: Int
        //@Query("post_id") postId: Int,
    ): Call<BaedalPostModel>

    @PATCH("order/post/update")             // 206 배달 게시글 수정
    fun updateBaedalPost(
        @Body jsonparams: BaedalUpdateModel
    ): Call<BaedalPostingResponse>

    @POST("order/ordering")                 // 209 배달 주문 등록
    fun baedalOrdering(
        @Body jsonparams: OrderingModel
    ): Call<OrderingResponse>

    @GET("order/post/list")                 // 213 배달 게시글 리스트 조회
    fun getBaedalOrderListPreview(
        @Query("from_index") fromIndex: Int,
    ): Call<List<BaedalOrderListPreviewModel>>

    // 테스트
    @GET("order/test")
    fun getTest(

    ): Call<TestModel>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000/" //"http://59.8.74.204:5000/" //
        var auth = MainActivity.prefs.getString("Authentication", "")

        fun create(): APIS {
            var auth = MainActivity.prefs.getString("Authentication", "")
            Log.d("auth 키", auth)

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
                var auth = MainActivity.prefs.getString("Authentication", "")
                Log.d("auth 키-인터셉터", auth)
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
