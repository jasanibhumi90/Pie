package com.pie.ui.editpie

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.github.tcking.giraffecompressor.GiraffeCompressor
import com.kbeanie.multipicker.api.CameraImagePicker
import com.kbeanie.multipicker.api.ImagePicker
import com.kbeanie.multipicker.api.Picker
import com.kbeanie.multipicker.api.VideoPicker
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.kbeanie.multipicker.api.entity.ChosenVideo
import com.kbeanie.multipicker.core.VideoPickerImpl
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.PostModel
import com.pie.ui.base.BaseActivity
import com.pie.ui.trim.ThirdPartyIntentsUtil
import com.pie.ui.trim.TrimmerActivity
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppConstant.Companion.ARG_INPUT_VIDEO
import com.pie.utils.AppConstant.Companion.ARG_PIE_DATA
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.PermissionUtils
import com.theartofdev.edmodo.cropper.CropImage

import kotlinx.android.synthetic.main.activity_create_pie.*
import kotlinx.android.synthetic.main.activity_forgot_pass.ivBack
import kotlinx.android.synthetic.main.listitem_image.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers

import java.io.File
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set


class EditPieActivity : BaseActivity(), View.OnClickListener, ImagePickerCallback,
        PermissionUtils.OnPermissionResponse, VideoPickerCallback {
    lateinit var pieData: PostModel
    var isdone = false





    companion object {
        private const val REQUEST_VIDEO_TRIMMER = 1
        private const val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 2
        internal const val EXTRA_INPUT_URI = "EXTRA_INPUT_URI"
        private val allowedVideoFileExtensions = arrayOf("mkv", "mp4", "3gp", "mov", "mts")
        private val videosMimeTypes = java.util.ArrayList<String>(allowedVideoFileExtensions.size)
    }

    init {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        for (fileExtension in allowedVideoFileExtensions) {
            val mimeTypeFromExtension = mimeTypeMap.getMimeTypeFromExtension(fileExtension)
            if (mimeTypeFromExtension != null)
                videosMimeTypes.add(mimeTypeFromExtension)
        }
    }

    lateinit var videoPicker: VideoPicker
    private var pickerPath: String = ""
    private var videoPath: String = ""
    private var pickerType: Int = 0
    private var imagePicker: ImagePicker? = null
    private var cameraPicker: CameraImagePicker? = null
    var arFiles: ArrayList<String> = ArrayList()
    var arRemoveFiles: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pie)
        ivBack.setOnClickListener(this)
        ivCamera.setOnClickListener(this)
        ivGallery.setOnClickListener(this)
        ivVideo.setOnClickListener(this)
        tvPie.setOnClickListener(this)
        ivRemoveVideo.setOnClickListener(this)
        permissionUtils = PermissionUtils(this)
        GiraffeCompressor.init(this)
        if (intent.hasExtra(ARG_PIE_DATA)) {
            pieData = intent?.getSerializableExtra(ARG_PIE_DATA) as PostModel
            setData()
        }
    }

    fun setData() {
        if (::pieData.isInitialized) {
            pieData.let {
                etPie.setText(it.pies_text)
                pieData.pies_media_url?.let {
                    if (it.size != 0) {
                        for (i in 0 until it.size) {
                            addView(it[i], false, i)
                        }
                    }
                }


            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }
            R.id.tvPie -> {
                if (arFiles.size == 0 && etPie.text.isEmpty() && videoPath.isEmpty()) {
                    sneakerError(this, "Please write something or add image or upload pic")
                } else {
                    if (arFiles.size > 0) {
                        uploadPic()
                    } else if (videoPath.isNotEmpty()) {
                        uploadVideo()
                    } else {
                        createPie("","")

                    }
                }

            }
            R.id.ivRemove -> {
                val pView = v.parent as View
                if (llImages.indexOfChild(pView) + 1 <= pieData.pies_media_url?.size!!) {
                    arRemoveFiles.add(pieData.pies_media_url!![llImages.indexOfChild(pView)])
                } else {
                    arFiles.removeAt(llImages.indexOfChild(pView))
                }
                llImages.removeView(pView)

                AppLogger.e("tag", "arRemoveFiles++" + arRemoveFiles.size)
            }
            R.id.ivRemoveVideo -> {
                rlVideo.visibility = View.GONE
                videoPath = ""
            }
            R.id.ivCamera -> {
                if (arFiles.size <= 3) {
                    if (!permissionUtils!!.checkPermission(PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION)) {
                        requestPermission(PermissionUtils.REQUEST_CODE_CAMERA_PERMISSION)
                    } else {
                        takePicture()
                    }
                } else {
                    sneakerError(this, "you can upload max 4 image")
                }
            }
            R.id.ivGallery -> {
                if (llImages.childCount <= 3) {
                    if (!permissionUtils!!.checkPermission(PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION)) {
                        requestPermission(PermissionUtils.REQUEST_CODE_GALLERY_PERMISSION)
                    } else {
                        pickImageSingle()
                    }
                } else {
                    sneakerError(this, "you can upload max 4 image")
                }

            }
            R.id.ivVideo -> {
                if (llImages.childCount == 0) {
                    pickFromGallery()
                } else {
                    sneakerError(this, "you can image or video")
                }


            }


        }

    }

    private fun requestPermission(code: Int) {
        permissionUtils?.requestPermissions(
                PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION,
                code
        )
    }

    private fun showImageChooserDialog() {
        val items = arrayOf(
                resources.getString(R.string.takephoto),
                resources.getString(R.string.choosefromgallery),
                resources.getString(R.string.cancel)
        )

        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == resources.getString(R.string.takephoto) -> takePicture()
                items[item] == resources.getString(R.string.choosefromgallery) -> pickImageSingle()
                items[item] == resources.getString(R.string.cancel) -> dialog.dismiss()
            }
        }
        builder.show()

    }


    private fun takePicture() {
        cameraPicker = CameraImagePicker(this)
        cameraPicker!!.setImagePickerCallback(this)
        pickerPath = cameraPicker!!.pickImage()

    }


    private fun pickImageSingle() {
        CropImage.activity()
                .start(this)

    }

    override fun onPermissionGranted(requestCode: Int) {
        when (requestCode) {
            PermissionUtils.REQUEST_CODE_GALLERY_PERMISSION -> {
                pickImageSingle()
            }

            PermissionUtils.REQUEST_CODE_CAMERA_PERMISSION -> {
                takePicture()
            }
        }
    }

    override fun onPermissionDenied(requestCode: Int) {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_READ_ACCESS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onImagesChosen(p0: MutableList<ChosenImage>?) {

        if (p0!!.isNotEmpty()) {
            CropImage.activity(Uri.parse(p0[0].queryUri))
                    .start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        @Suppress("DEPRECATION")
                        result.uri.path?.let {

                            pickerPath = it
                            addView(pickerPath, true, llImages.childCount)

                        }
                    }
                }

                Picker.PICK_IMAGE_DEVICE -> {
                    if (imagePicker == null) {
                        imagePicker = ImagePicker(this)
                        imagePicker!!.setImagePickerCallback(this)
                    }
                    imagePicker!!.submit(data)
                }

                Picker.PICK_IMAGE_CAMERA -> {
                    try {
                        val path: String = pickerPath

                        if (cameraPicker == null) {
                            cameraPicker = CameraImagePicker(this)
                            cameraPicker!!.setImagePickerCallback(this)
                            cameraPicker!!.reinitialize(path)
                        }
                        cameraPicker!!.submit(data)
                    } catch (e: Exception) {
                    }

                }
                Picker.PICK_VIDEO_DEVICE -> {
                    try {
                        if (videoPicker == null) {
                            videoPicker = prepareVideoPicker();
                        }
                        var videoPickerImpl: VideoPickerImpl? = null
                        videoPickerImpl = videoPicker
                        videoPickerImpl.submit(data);
                    } catch (e: Exception) {
                    }

                }

                REQUEST_VIDEO_TRIMMER -> {
                    val uri = data!!.data
                   if (uri != null && checkIfUriCanBeUsedForVideo(uri)) {
                        startTrimActivity(uri)
                    } else {
                        Toast.makeText(
                            this@EditPieActivity,
                            R.string.toast_cannot_retrieve_selected_video,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                }
                201 -> {
                    val  progressDoalog =  ProgressDialog(this)
                    progressDoalog.setCancelable(false)
                    progressDoalog.setMessage("Compressing....")
                    progressDoalog.show()
                    val trimmedUri: Uri? = data?.getParcelableExtra(EditPieActivity.EXTRA_INPUT_URI)
                    videoPath = trimmedUri?.path.toString()
                    val bitmap2 = ThumbnailUtils.createVideoThumbnail(
                            trimmedUri?.getPath(),
                            MediaStore.Images.Thumbnails.MINI_KIND
                    )
                    rlVideo.visibility = View.VISIBLE
                    ivPreviewVideo.setImageBitmap(bitmap2)
                    val parentFolder = getExternalFilesDir(null)!!
                    parentFolder.mkdirs()
                    val fileName = "compressVideo.mp4"
                    val compressVideoFile = File(parentFolder, fileName)

                    GiraffeCompressor.create(GiraffeCompressor.TYPE_FFMPEG) //two implementations: mediacodec and ffmpeg,default is mediacodec
                            .input(videoPath) //set video to be compressed
                            .output(compressVideoFile) //set compressed video output
                            .resizeFactor(1.0F)
                            .ready()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : Subscriber<GiraffeCompressor.Result>() {
                                override fun onCompleted() {
                                    progressDoalog.cancel()
                                    videoPath = compressVideoFile.absolutePath
                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                    Log.e("tag", "error" + e.printStackTrace())
                                    progressDoalog.cancel()
                                }

                                override fun onNext(s: GiraffeCompressor.Result) {

                                }
                            })
                }

            }
        }

    }

    override fun onError(p0: String?) {
        AppLogger.e("tag", "error" + p0)
    }

    fun addView(pickerPath: String, isNew: Boolean, pos: Int) {

        val linearLayout = LayoutInflater.from(llImages.context)
                .inflate(R.layout.listitem_image, llImages, false) as RelativeLayout

        arFiles.add(pickerPath)

        linearLayout.ivRemove.setOnClickListener(this)
        Glide.with(this).load(pickerPath).into(linearLayout.ivImage)
        llImages.addView(linearLayout, pos)
    }


    private fun uploadPic() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart("token_id", pref.getToken());

            // Map is used to multipart the file using okhttp3.RequestBody
            // Multiple Images
            var size = 0
            for (i in 0 until arFiles.size) {
                if (!AppGlobal.containsUrl(arFiles.get(i))) {
                    val file = File(arFiles.get(i))
                    size++
                    builder.addFormDataPart(
                            "pie_image[]",
                            file.getName(),
                            RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    )
                }
            }
            if (size != 0) {
                val requestBody: MultipartBody = builder.build()
                callApi(requestInterface.uploadPiePic(requestBody), true)
                        ?.subscribe({ onFileUpload(it) }) { onResponseFailure(it, true) }
                        ?.let { mCompositeDisposable.add(it) }
            } else {
                if (videoPath.isNotEmpty()) {
                    uploadVideo()
                } else {
                    createPie("","")
                }
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onFileUpload(
            resp: BaseResponse<String>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
        val imagePaths = resp.data
        if (imagePaths != null) {
            createPie("image",imagePaths)
        }
    }


    private fun uploadVideo() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart("token_id", pref.getToken());

            // Map is used to multipart the file using okhttp3.RequestBody
            // Multiple Images

            val file = File(videoPath)
            builder.addFormDataPart(
                    "videofile",
                    file.getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), file)
            )


            val requestBody: MultipartBody = builder.build()
            callApi(requestInterface.uploadVideo(requestBody), true)
                    ?.subscribe({ onVideoFileUpload(it) }) { onResponseFailure(it, true) }
                    ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onVideoFileUpload(
            resp: BaseResponse<String>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
        val videoPath = resp.data
        if (videoPath != null) {
            createPie("video",videoPath)
        }
    }


    private fun createPie(type:String,imagePaths: String) {
        if (AppGlobal.isNetworkConnected(this)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = pieData.id
            data[getString(R.string.param_pies_text)] = etPie.text.toString()
            data[getString(R.string.param_pies_media)] = imagePaths
            data[getString(R.string.param_remove_medias)] = TextUtils.join(",", arRemoveFiles)
            data[getString(R.string.param_pies_type)] = type




            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_edit_pies)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.createPie(service), true)
                    ?.subscribe({ onCreatePie(it) }) { onResponseFailure(it, true) }
                    ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onCreatePie(
            resp: BaseResponse<PostModel>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return

        val data = Intent()
        data.putExtra(ARG_DATA, resp.data)
        setResult(RESULT_OK, data)
        finish()
    }


    protected fun pickSingleVideo() {
        videoPicker = prepareVideoPicker()
        videoPicker.pickVideo()
        pickerType = Picker.PICK_VIDEO_DEVICE
    }

    private fun prepareVideoPicker(): VideoPicker {
        val videoPicker = VideoPicker(this)
        videoPicker.setVideoPickerCallback(this)
        return videoPicker
    }

    override fun onVideosChosen(p0: MutableList<ChosenVideo>?) {
        rlVideo.visibility = View.VISIBLE
        Glide.with(this).load(p0!![0].previewThumbnail).into(ivPreviewVideo)
        videoPath = p0[0].originalPath
        val intent = Intent(this, TrimmerActivity::class.java)
        intent.putExtra(ARG_INPUT_VIDEO, videoPath)
        startActivity(intent)

    }

    private fun pickFromGallery() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION
            )
        } else {
            var intentForChoosingVideos = ThirdPartyIntentsUtil.getPickFileChooserIntent(
                    this,
                    null,
                    false,
                    true,
                    "video/*",
                    videosMimeTypes.toTypedArray(),
                    null
            )
            if (intentForChoosingVideos == null)
                intentForChoosingVideos =
                        ThirdPartyIntentsUtil.getPickFileIntent(this, "video/*,", videosMimeTypes.toTypedArray())
            if (intentForChoosingVideos != null)
                startActivityForResult(intentForChoosingVideos, REQUEST_VIDEO_TRIMMER)
        }
    }

    private fun checkIfUriCanBeUsedForVideo(uri: Uri): Boolean {
        val mimeType = ThirdPartyIntentsUtil.getMimeType(this, uri)
        val identifiedAsVideo = mimeType != null && videosMimeTypes.contains(mimeType)
        if (!identifiedAsVideo)
            return false
        try {
            //check that it can be opened and trimmed using our technique
            val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
            val inputStream = (if (fileDescriptor == null) null else contentResolver.openInputStream(uri))
                    ?: return false
            inputStream.close()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun startTrimActivity(uri: Uri) {
        val intent = Intent(this, TrimmerActivity::class.java)
        intent.putExtra(EXTRA_INPUT_URI, uri)
        startActivityForResult(intent, 201)
    }

    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.permission_title_rationale))
            builder.setMessage(rationale)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(
                        this@EditPieActivity,
                        arrayOf(permission),
                        requestCode
                )
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */


}
