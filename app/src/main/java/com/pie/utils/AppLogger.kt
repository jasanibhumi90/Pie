package com.pie.utils

import android.util.Log

class AppLogger {

    companion object {
        val DEBUG = true

        fun e(tag: String, msg: String) {
            if (DEBUG) {
                Log.e(tag, msg)
            }
        }

        fun e(tag: String, msg: String, exception: Throwable) {
            if (DEBUG) {
                Log.e(tag, msg, exception)
            }
        }

        fun i(tag: String, msg: String) {
            if (DEBUG) {
                Log.i(tag, msg)
            }
        }

        fun w(tag: String, msg: String) {
            if (DEBUG) {
                Log.w(tag, msg)
            }
        }

        fun d(tag: String, msg: String) {
            if (DEBUG) {
                Log.d(tag, msg)
            }
        }

        fun v(tag: String, msg: String) {
            if (DEBUG) {
                Log.v(tag, msg)
            }
        }
    }
}