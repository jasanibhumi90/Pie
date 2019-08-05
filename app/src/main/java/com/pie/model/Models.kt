package com.pie.model

import com.bignerdranch.expandablerecyclerview.model.Parent
import java.io.Serializable

data class BaseResponse<T>(
    var message: String,
    var token: String,
    var success: Int,
    var otpcode: Int,
    val data: T? = null
) : Serializable

data class FollowResponse(
    var message: String,
    var success: Int,
    var followstatus:String
)



data class LoginModel(
    var user_id: String,
    var first_name: String,
    var last_name: String,
    var user_name: String,
    var profile_pic: String,
    var gender: String,
    var things_ids: String,
    var profile_status: String,
    var country_code: String,
    var country_name: String,
    var phone_no: String,
    var wallet: String,
    var email_id: String,
    var password: String,
    var mobile_code: String,
    var is_verify: String,
    var device_type: String,
    var device_id: String,
    var is_blocked: String,
    var birth_date: String
) : Serializable


data class PostModel(
    var id: String = "",
    var user_id: String = "",
    var pie_user_id: String = "",
    var parent_id: String,
    var post_type: String,
    var pies_text: String,
    var pies_type: String,
    var pies_media: String,
    var profile_pic: String,
    var first_name: String,
    var last_name: String,
    var creation_datetime: String,
    var likes: String,
    var comments: String,
    var view_count: String,
    var shared: String,
    var pies_media_url: ArrayList<String>? = ArrayList(),
    var comment_list: ArrayList<CommentModel>? = ArrayList(),
    var post_at: String,
    var like_flag: String? = "",
    val followstatus:String="1"
) : Serializable, Cloneable {
    public override fun clone(): PostModel {
        return super.clone() as PostModel
    }
}

data class ReportModel(
    var id: String = "",
    var report_name: String = ""
) : Serializable

data class CommentModel(
    val id: String? = "",
    val parent_id: String? = "",
    val pie_id: String? = "",
    val user_id: String? = "",
    val comment: String? = "",
    val first_name: String? = "",
    val last_name: String? = "",
    val profile_pic: String? = "",
    val creation_datetime: String? = "",
    val post_at: String? = "",
    val subcomment: ArrayList<SubComment> = ArrayList()
) : Serializable,
    Parent<SubComment> {
    override fun getChildList(): MutableList<SubComment> {
        return subcomment
    }


    override fun isInitiallyExpanded(): Boolean {
        return true
    }
}

data class SubComment(
    val id: String? = "",
    val parent_id: String? = "",
    val pie_id: String? = "",
    val user_id: String? = "",
    val comment: String? = "",
    val first_name: String? = "",
    val last_name: String? = "",
    val profile_pic: String? = "",
    val creation_datetime: String? = "",
    val post_at: String? = ""
) : Serializable

data class LikePost(var count: Int? = 0)
data class PostComment(
    val id: String? = "",
    val parent_id: String? = "",
    val pie_id: String? = "",
    val user_id: String? = "",
    val comment: String? = "",
    val first_name: String? = "",
    val last_name: String? = "",
    val profile_pic: String? = "",
    val creation_datetime: String? = "",
    val post_at: String? = ""
) : Serializable

data class GetPieView(
    var id: String = "",
    var user_id: String = "",
    var pie_user_id: String = "",
    var parent_id: String,
    var post_type: String,
    var pies_text: String,
    var pies_media: String,
    var profile_pic: String,
    var first_name: String,
    var last_name: String,
    var creation_datetime: String,
    var likes: String,
    var comments: String,
    var view_count: String,
    var shared: String,
    var pies_type: String,
    var pies_media_url: ArrayList<String>,
    var post_at: String,
    var like_flag: String? = "",
    val comment_list: ArrayList<CommentModel>? = ArrayList(),
    val like_list: ArrayList<LikeModel>? = ArrayList()
) : Serializable

data class SharePost(
    val id: String? = "",
    val user_id: String? = "",
    val pie_user_id: String? = "",
    val parent_id: String? = "",
    val post_type: String? = "",
    val pies_text: String? = "",
    val pies_media: String? = "",
    val profile_pic: String = "",
    val first_name: String = "",
    val last_name: String? = "",
    val creation_datetime: String? = "",
    val likes: String? = "",
    val like_flag: String? = "",
    val comments: String? = "",
    val shared: String? = ""
) : Serializable

data class LikeModel(
    var user_id: String? = "",
    var profile_pic: String? = "",
    var first_name: String? = "",
    var last_name: String? = "",
    var user_name: String?="",
    var creation_datetime: String? = "",
    var followstatus: String? = "0",
    var post_at: String? = ""
) : Serializable

data class Profile(
    var user_id: String,
    var first_name: String,
    var last_name: String,
    var user_name: String,
    var profile_pic: String,
    var gender: String,
    var things_ids:String,
    var profile_status: String,
    var country_code: String,
    var country_name: String,
    var phone_no: String,
    var wallet: String,
    var email_id: String,
    var password: String,
    var mobile_code: String,
    var is_verify: String,
    var device_type: String,
    var device_id: String,
    var is_blocked: String,
    var birth_date: String,
    var is_deleted:String,
    var last_login:String,
    var creation_datetime:String,
    var modification_datetime:String,
    var deletion_datetime:String,
    var post_at:String,
    var countpies:String,
    var piemate:ArrayList<Piemate>?=ArrayList(),
    var countpiemate:String,
    var mealsorevent:String,
    var followstatus: String,
    val pie_list:ArrayList<PostModel>?=ArrayList()
):Serializable

data class Piemate(
    var user_id:String,
    var profile_pic:String,
    var first_name:String,
    var last_name:String,
    var user_name:String,
    var creation_datetime:String,
    var followstatus:String,
    val post_at:String
):Serializable

data class Suggestion(
    val user_id: String? = "",
    val first_name: String? = "",
    val user_name: String? = "",
    val profile_pic: String? = ""
) : Serializable

