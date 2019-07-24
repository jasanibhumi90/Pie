package com.pie.ui.register_profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kbeanie.multipicker.api.CameraImagePicker
import com.kbeanie.multipicker.api.ImagePicker
import com.kbeanie.multipicker.api.Picker
import com.kbeanie.multipicker.api.callbacks.FilePickerCallback
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback
import com.kbeanie.multipicker.api.entity.ChosenFile
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.LoginModel
import com.pie.ui.base.BaseActivity
import com.pie.ui.main.MainActivity
import com.pie.utils.AppConstant
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.PermissionUtils
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_register_profile.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*


class RegisterProfileActivity : BaseActivity(), View.OnClickListener, ImagePickerCallback,
    PermissionUtils.OnPermissionResponse, FilePickerCallback {
    private var uploadPath: String = ""
    private var vUserName: String = ""
    private var vBirthDate: String = ""
    private var pickerPath: String = ""
    private var imagePicker: ImagePicker? = null
    private var cameraPicker: CameraImagePicker? = null
    var isMale = true
    var data: HashMap<String, String> = HashMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_profile)
        tvSubmit.setOnClickListener(this)
        tvMale.setOnClickListener(this)
        tvFemale.setOnClickListener(this)
        ivProPic.setOnClickListener(this)
        etBirthdate.setOnClickListener(this)
        permissionUtils= PermissionUtils(this)
        if (intent.hasExtra(AppConstant.ARG_DATA)) {
            data = intent.getSerializableExtra(AppConstant.ARG_DATA) as HashMap<String, String>
        }
    }

    private fun requestPermission() {
        permissionUtils?.requestPermissions(
            PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION,
            PermissionUtils.REQUEST_CODE_GALLERY_PERMISSION
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
                showImageChooserDialog()
            }
        }
    }

    override fun onPermissionDenied(requestCode: Int) {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
                            Glide.with(this).load(pickerPath)
                                .apply(RequestOptions())
                                .into(ivProPic)
                            uploadPic(pickerPath)
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
            }
        }

    }

    private fun isValid(): Boolean {
        vUserName = etUserName.text.toString()


        if (vUserName.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_username))
            return false
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSubmit -> {
                if (isValid()) {
                    registerApi()
                }
            }
            R.id.ivProPic -> {
                if (!permissionUtils!!.checkPermission(PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION)) {
                    requestPermission()
                } else {
                    showImageChooserDialog()
                }
            }
            R.id.tvMale -> {
                if (!isMale) {
                    tvMale.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_blue)
                    tvFemale.background = ContextCompat.getDrawable(this, R.drawable.bg_editext)
                    isMale = true
                }
            }

            R.id.tvFemale -> {
                if (isMale) {
                    tvMale.background = ContextCompat.getDrawable(this, R.drawable.bg_editext)
                    tvFemale.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_blue)
                    isMale = false
                }
            }

            R.id.etBirthdate -> {
                val calendar = Calendar.getInstance()
                  val yy = calendar.get(Calendar.YEAR)
                val mm = calendar.get(Calendar.MONTH)
                val dd = calendar.get(Calendar.DAY_OF_MONTH)
                val mdiDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                        vBirthDate =year.toString() + "-" + String.format("%02d", monthOfYear) + "-" + String.format("%02d", dayOfMonth)
                        etBirthdate.text=vBirthDate
                       /* Toast.makeText(
                            getApplicationContext(),
                            year.toString() + " " + monthOfYear.toString() + " " + dayOfMonth.toString(),
                            Toast.LENGTH_LONG
                        ).show()*/
                    }
                }, yy, mm, dd)
                mdiDialog.show()
            }
        }
    }


    private fun uploadPic(path: String) {
        if (AppGlobal.isNetworkConnected(this)) run {


            callApi(requestInterface.uploadPic(getFileToUpload("user_image", path)), true)
                ?.subscribe({ onFileUpload(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onFileUpload(
        resp:BaseResponse<String>
    ) {
        Log.e("tag","resp"+resp)
       if (super.onStatusFalse(resp, true)) return
        uploadPath = resp.data.toString()
    }

    private fun getFileToUpload(filename: String, path: String): MultipartBody.Part {
        lateinit var fileToUpload: MultipartBody.Part
        var requestFile: RequestBody = RequestBody.create(MediaType.parse("text/plain"), "")
        if (path.isNotBlank()) {
            val file = File(path)
            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            fileToUpload =
                MultipartBody.Part.createFormData(filename, file.name, requestFile)
        } else {
            fileToUpload = MultipartBody.Part.createFormData(filename, "", requestFile)
        }
        return fileToUpload
    }

    private fun getRequestBody(param: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), param)
    }

    override fun onError(p0: String?) {
        AppLogger.e("tag", "error" + p0)
    }


    override fun onFilesChosen(files: List<ChosenFile>) {
        for (file in files) {
            Log.d("tag", "onFilesChosen: $file")
        }


    }




    private fun registerApi() {
        if (AppGlobal.isNetworkConnected(this)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            data[getString(R.string.param_user_name)] = vUserName
            data[getString(R.string.param_profile_pic)] = uploadPath
            data[getString(R.string.param_birth_date)] = vBirthDate
            if (isMale) {
                data[getString(R.string.param_gender)] = "M"
            } else {
                data[getString(R.string.param_gender)] = "F"

            }
            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_signup)
            service[getString(R.string.request)] = request
            callApi(requestInterface.register(service), true)
                ?.subscribe({ onRegister(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onRegister(
        resp: BaseResponse<LoginModel>
    ) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            pref.setLogin(true)
            pref.setLoginData(resp.data)
            pref.setToken(resp.token)
            startActivity(intentFor<MainActivity>().clearTask().newTask())
        }
    }


}
