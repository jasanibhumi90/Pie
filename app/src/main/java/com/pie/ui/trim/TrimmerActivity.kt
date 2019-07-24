package com.pie.ui.trim

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import com.pie.R
import com.pie.utils.AppConstant.Companion.ARG_INPUT_VIDEO
import kotlinx.android.synthetic.main.activity_trimmer.*
import java.io.File
import android.os.Environment.getExternalStorageDirectory

import android.os.Environment
import com.pie.ui.createpie.CreatePieActivity
import com.pie.ui.createpie.CreatePieActivity.Companion.EXTRA_INPUT_URI


class TrimmerActivity : AppCompatActivity(), VideoTrimmingListener {
//    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)
        val inputVideoUri: Uri? = intent?.getParcelableExtra(CreatePieActivity.EXTRA_INPUT_URI)
        if (inputVideoUri == null) {
            finish()
            return
        }
        //setting progressbar
//        progressDialog = ProgressDialog(this)
//        progressDialog!!.setCancelable(false)
//        progressDialog!!.setMessage(getString(R.string.trimming_progress))
        videoTrimmerView.setMaxDurationInMs(300000)
        videoTrimmerView.setOnK4LVideoListener(this)
        val parentFolder = getExternalFilesDir(null)!!
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"
        val trimmedVideoFile = File(parentFolder, fileName)
        videoTrimmerView.setDestinationFile(trimmedVideoFile)

        videoTrimmerView.setVideoURI(inputVideoUri)
        videoTrimmerView.setVideoInformationVisibility(true)
    }

    override fun onTrimStarted() {
        trimmingProgressView.visibility = View.VISIBLE
    }

    override fun onFinishedTrimming(uri: Uri?) {
        trimmingProgressView.visibility = View.GONE
        if (uri == null) {
            Toast.makeText(this@TrimmerActivity, "failed trimming", Toast.LENGTH_SHORT).show()
        } else {
           // val msg = getString(R.string.video_saved_at, uri.path)
            Toast.makeText(this@TrimmerActivity, uri.path, Toast.LENGTH_SHORT).show()
            val data = Intent()
            data.putExtra(EXTRA_INPUT_URI, uri)
           setResult(Activity.RESULT_OK,data)
            finish()
        }

    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        trimmingProgressView.visibility = View.GONE
        Toast.makeText(this@TrimmerActivity, "error while previewing video", Toast.LENGTH_SHORT).show()
    }

    override fun onVideoPrepared() {
        //        Toast.makeText(TrimmerActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
    }
}
