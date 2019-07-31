package com.pie.data.network;

import com.pie.PieApp
import com.pie.utils.AppConstant
import com.pie.utils.AppConstant.Companion.TIME_OUT
import com.pie.utils.AppLogger
import com.google.gson.GsonBuilder
import com.pie.model.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface RequestInterface {


    @POST("WebService/service")
    fun login(@Body map: HashMap<String, Any>): Observable<BaseResponse<LoginModel>>
    //endregion

    @POST("WebService/service")
    fun forgotpass(@Body map: HashMap<String, Any>): Observable<BaseResponse<Any>>
    //endregion


    @POST("WebService/service")
    fun register(@Body map: HashMap<String, Any>): Observable<BaseResponse<LoginModel>>


    @Multipart
    @POST("upload/profile_image")
    fun uploadPic(@Part docFile: MultipartBody.Part): Observable<BaseResponse<String>>


    @POST("upload/upload_pie_image")
    fun uploadPiePic(@Body file:RequestBody ): Observable<BaseResponse<String>>

    @POST("WebService/service")
    fun createPie(@Body map: HashMap<String, Any>): Observable<BaseResponse<PostModel>>

    @POST("WebService/service")
    fun getPies(@Body map: HashMap<String, Any>): Observable<BaseResponse<ArrayList<PostModel>>>


    @POST("WebService/service")
    fun getReportss(@Body map: HashMap<String, Any>): Observable<BaseResponse<ArrayList<ReportModel>>>

    @POST("WebService/service")
    fun postReport(@Body map: HashMap<String, Any>): Observable<BaseResponse<Any>>

    @POST("WebService/service")
    fun postLike(@Body map: HashMap<String, Any>):Observable<BaseResponse<LikePost>>

    @POST("WebService/service")
    fun postComments(@Body map: HashMap<String, Any>):Observable<BaseResponse<PostComment>>

    @POST("WebService/service")
    fun sharePost(@Body map: HashMap<String, Any>):Observable<BaseResponse<PostModel>>

    @POST("WebService/service")
    fun getPiePostComment(@Body map:HashMap<String,Any>):Observable<BaseResponse<GetPieView>>

    @POST("WebService/service")
    fun getLikes(@Body map:HashMap<String,Any>):Observable<BaseResponse<GetPieView>>

    @POST("WebService/service")
    fun getSuggestionUser(@Body map:HashMap<String,Any>):Observable<BaseResponse<Suggestion>>

    @POST("upload/upload_pie_video")
    fun uploadVideo(@Body file:RequestBody ): Observable<BaseResponse<String>>

    @POST("WebService/service")
    fun getProfile(@Body map: HashMap<String, Any> ): Observable<BaseResponse<Profile>>






    //endregion
    /*
    //region editProfile
    @Multipart
    @POST("user_registration")
    fun register(@PartMap map1: HashMap<String, RequestBody>, @Part profilePicFile: MultipartBody.Part): Observable<BaseResponse<LoginModel>>
    //endregion


    //region editProfile
    @Multipart
    @POST("user/update-profile")
    fun updateProfile(@PartMap map1: HashMap<String, RequestBody>, @Part profilePicFile: MultipartBody.Part): Observable<BaseResponse<LoginModel>>
    //endregion*/

    companion object {
        fun create(): RequestInterface {

            val httpClient = OkHttpClient.Builder()
            httpClient.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            httpClient.readTimeout(TIME_OUT, TimeUnit.SECONDS)
            httpClient.writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            if (AppLogger.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                httpClient.addInterceptor(logging)
            }

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                AppLogger.e("tag","TOKEN++"+ "Bearer " + PieApp.getInstance().getAppPreferencesHelper().getToken())
                val request = original.newBuilder()
                    .method(original.method(), original.body())
                    .addHeader("Accept","application/json")
                    .addHeader(
                        AppConstant.Authorization,
                        "Bearer " + PieApp.getInstance().getAppPreferencesHelper().getToken()
                    )
                    .build()
                val response: Response = chain.proceed(request)
                response
            }


            return Builder()
                .baseUrl(AppConstant.BASE_URL)
                .client(httpClient.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .build().create(RequestInterface::class.java)
        }
    }
}