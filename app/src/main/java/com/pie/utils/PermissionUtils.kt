package com.pie.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionUtils {

    private var activity: Activity? = null
    private var fragment: Fragment? = null

    private var onPermissionResponse: OnPermissionResponse? = null

    constructor(activity: Activity) {
        this.activity = activity
        onPermissionResponse = activity as OnPermissionResponse
    }

    constructor(fragment: Fragment) {
        this.activity = fragment.activity
        this.fragment = fragment
        onPermissionResponse = fragment as OnPermissionResponse
    }

    /**
     * @param permissions string list of permission you want to ask for
     * @param requestCode int code for requesting permission
     */
    fun requestPermissions(permissions: Array<String>, requestCode: Int) {

        if (checkPermission(permissions)) {
            if (onPermissionResponse != null) {
                onPermissionResponse!!.onPermissionGranted(requestCode)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (fragment != null)
                    (fragment as Fragment).requestPermissions(permissions, requestCode)
                else
                    (activity as Activity).requestPermissions(permissions, requestCode)
            }
        }

    }

    /**
     * @param permissions string list of permission you want to ask for
     * @return boolean  returns true if permission already granted
     */
    public fun checkPermission(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        } else {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(activity!!, permission) != 0) {
                    return false
                }
            }
            return true
        }
    }


    interface OnPermissionResponse {
        fun onPermissionGranted(requestCode: Int)

        fun onPermissionDenied(requestCode: Int)
    }

    /**
     * @param requestCode  requestCode received from onRequestPermissionsResult()
     * @param permissions  permissions received from onRequestPermissionsResult()
     * @param grantResults grantResults received from onRequestPermissionsResult()
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (verifyPermissionResults(grantResults)) {

            if (onPermissionResponse != null) {
                onPermissionResponse!!.onPermissionGranted(requestCode)
            }
        } else {
            if (onPermissionResponse != null) {
                onPermissionResponse!!.onPermissionDenied(requestCode)
            }
        }
    }

    private fun verifyPermissionResults(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) return false

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    companion object {

        val REQUEST_CODE_CAMERA_PERMISSION = 101
        val REQUEST_CODE_GALLERY_PERMISSION = 106
        val REQUEST_CODE_LOCATION_PERMISSION = 102
        val REQUEST_CODE_EXTERNAL_STORAGE = 103
        val REQUEST_CODE_CALL_PERMISSION = 105
        val REQUEST_CAMERA_GALLERY_PERMISSION =
            arrayOf(android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val REQUEST_LOCATION_PERMISSION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val REQUEST_EXTERNAL_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val REQUEST_CALL_PERMISSION = arrayOf("android.permission.CALL_PHONE")

    }
}