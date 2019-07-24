package com.pie.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.pie.model.LoginModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


class AppPreferencesHelper(context: Context, prefFileName: String) {

    companion object {
        const val KEY_IS_FIRST_TIME = "KEY_IS_FIRST_TIME"
        const val KEY_ISLOGIN = "KEY_ISLOGIN"
        const val KEY_NOT_NOW_LOCATION = "KEY_NOT_NOW_LOCATION"
        const val KEY_SELEDCTED_ADDRESS = "KEY_SELEDCTED_ADDRESS"
        const val KEY_LANGUAGE = "KEY_LANGUAGE"
        const val KEY_TOKEN = "KEY_TOKEN"
        const val KEY_LOGIN_DATA = "KEY_LOGIN_DATA"
        const val KEY_USERID = "KEY_USERID"
        const val KEY_ORDERID = "KEY_ORDERID"
        const val KEY_SETTINGS_DATA = "KEY_SETTINGS_DATA"
        const val KEY_DEVICE_ID = "KEY_DEVICE_ID"
        const val KEY_EX_ID = "KEY_EX_ID"
        const val KEY_SHOOT_DURATION_MODE = "KEY_SHOOT_DURATION_MODE"
        const val KEY_RECENT_HASH_TAGS = "KEY_RECENT_HASH_TAGS"
        const val KEY_CONTACTS = "KEY_CONTACTS"
        const val KEY_CONTACTS_FB = "KEY_CONTACTS_FB"

        const val KEY_LAST_VIDEO_ID = "KEY_LAST_VIDEO_ID"
        const val KEY_RANDOM_PAGE = "KEY_RANDOM_PAGE"

        const val KEY_CART = "KEY_CART"
        const val KEY_SETTING_DATA = "KEY_SETTING_DATA"
    }

    private var prefs: SharedPreferences? = null

    init {
        prefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)
    }

    //==================================================================
    fun setBoolean(key: String, value: Boolean) {
        prefs!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs!!.getBoolean(key, defaultValue)
    }

    //==================================================================
    fun setString(key: String, value: String) {
        prefs!!.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String? {
        return prefs!!.getString(key, defaultValue)
    }

    //==================================================================
    fun setInt(key: String, value: Int) {
        prefs!!.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return prefs!!.getInt(key, defaultValue)
    }
    fun setExId( value: Int) {
        prefs!!.edit().putInt(KEY_EX_ID, value).apply()
    }

    fun getExId(): Int {
        return prefs!!.getInt(KEY_EX_ID, 0)
    }

    //<editor-fold desc="IsFirstTime">
    fun setIsFirstTime(flag: Boolean) {
        prefs!!.edit().putBoolean(KEY_IS_FIRST_TIME, flag).apply()
    }

    fun getIsFirstTime(): Boolean {
        return prefs!!.getBoolean(KEY_IS_FIRST_TIME, true)
//        return true
    }
    //</editor-fold>


    //<editor-fold desc="DeviceId">
    fun setDeviceId(token: String) {
        prefs!!.edit().putString(KEY_DEVICE_ID, token).apply()

    }

    fun getDeviceId(): String {
        return prefs!!.getString(KEY_DEVICE_ID, "")!!
    }
    //</editor-fold>


    //<editor-fold desc="Token">
    fun setToken(token: String) {
        prefs!!.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String {
        return prefs!!.getString(KEY_TOKEN, "")!!
    }
    //</editor-fold>


    //<editor-fold desc="isLogin">
    fun setLogin(isLogin: Boolean) {
        prefs!!.edit().putBoolean(KEY_ISLOGIN, isLogin).apply()
    }

    fun isLogin(): Boolean {
        return prefs!!.getBoolean(KEY_ISLOGIN, false)
    }
    //</editor-fold>

    fun setNotNowLocationPermission(b: Boolean) {
        prefs!!.edit().putBoolean(KEY_NOT_NOW_LOCATION, b).apply()
    }

    fun getNotNowLocationPermission(): Boolean {
        return prefs!!.getBoolean(KEY_NOT_NOW_LOCATION, false)
    }


    //<editor-fold desc="UserId">
    fun setUserId(userId: Int) {
        prefs!!.edit().putInt(KEY_USERID, userId).apply()
    }

    fun getUserId(): Int {
        return prefs!!.getInt(KEY_USERID, 0)
    }
    //</editor-fold>

    //<editor-fold desc="UserId">
    fun setOrderId(userId: Int) {
        prefs!!.edit().putInt(KEY_ORDERID, userId).apply()
    }

    fun getOrderId(): Int {
        return prefs!!.getInt(KEY_ORDERID, 0)
    }
    //</editor-fold>

    fun getLanguage(): String? {
        return prefs!!.getString(KEY_LANGUAGE, Locale.getDefault().language)
    }

    fun setLanguage(lang: String) {
        prefs!!.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    //<editor-fold desc="Clear App Data">
    fun clearAppData() {
        setToken("")
        setLogin(false)
        setUserId(0)
    }
    //</editor-fold>


    //<editor-fold desc="Clear All Preferences">
    fun clearPreferences() {
        prefs!!.edit().clear().apply()
    }
    //</editor-fold>

   /* fun getSettingsData(): SettingsData? {
        return try {
            Gson().fromJson<SettingsData>(
                prefs?.getString(KEY_SETTING_DATA, null),
                SettingsData::class.java
            )
        } catch (e: Exception) {
            SettingsData()
        }
    }

    fun setSettingsData(settingsData: SettingsData) {
        val settings = Gson().toJson(settingsData)
        prefs?.edit()?.putString(KEY_SETTING_DATA, settings)?.apply()
    }


    fun getSelectedAddress(): ShippingAddress? {
        return try {
            Gson().fromJson<ShippingAddress>(
                prefs?.getString(KEY_SELEDCTED_ADDRESS, null),
                ShippingAddress::class.java
            )
        } catch (e: Exception) {
            ShippingAddress()
        }
    }

    fun setSelectedAddress(settingsData: ShippingAddress?) {
        val settings = Gson().toJson(settingsData)
        prefs?.edit()?.putString(KEY_SELEDCTED_ADDRESS, settings)?.apply()
    }


    //==================================================================
    fun getCart(): ArrayList<CategoryItemsInfo> {

        val categoryItemsInfo: ArrayList<CategoryItemsInfo>

        try {
            val listType = object : TypeToken<ArrayList<CategoryItemsInfo>>() {

            }.type
            categoryItemsInfo = Gson().fromJson<ArrayList<CategoryItemsInfo>>(prefs?.getString(KEY_CART, ""), listType)
            return categoryItemsInfo

        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    fun setCart(categoryItemsInfo: List<CategoryItemsInfo>) {
        val info = Gson().toJson(categoryItemsInfo)
        prefs!!.edit().putString(KEY_CART, info).apply()
    }*/

    fun getLoginData(): LoginModel? {

        var loginInfo: LoginModel? = null
        try {
            loginInfo =
                Gson().fromJson<Any>(prefs!!.getString(KEY_LOGIN_DATA, ""), LoginModel::class.java) as LoginModel
        } catch (e: Exception) {
// loginInfo = LoginData()
        }

        return loginInfo
    }

    fun setLoginData(result: LoginModel) {
        val info = Gson().toJson(result)
        prefs!!.edit().putString(KEY_LOGIN_DATA, info).apply()
    }


}