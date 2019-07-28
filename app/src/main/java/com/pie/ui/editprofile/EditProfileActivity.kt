package com.pie.ui.editprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.PermissionUtils
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_register_profile.*
import kotlinx.android.synthetic.main.editprofile.*
import kotlinx.android.synthetic.main.editprofile.ivProPic
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import java.io.File
import java.util.HashMap

class EditProfileActivity : BaseActivity(), View.OnClickListener, ImagePickerCallback,
    PermissionUtils.OnPermissionResponse, FilePickerCallback {
    private var vBirthDate: String = ""
    private var pickerPath: String = ""
    private var uploadPath: String = ""
    private var vFirstName: String = ""
    private var vLastName: String = ""
    private var vEmail: String = ""
    private var vBio: String = ""
    private var vBirthdate: String = ""
    private var imagePicker: ImagePicker? = null
    private var cameraPicker: CameraImagePicker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editprofile)
        pref.getLoginData()?.let {
            if (it.profile_pic.isNotEmpty()) {
                Glide.with(this).load(it.profile_pic).into(ivProPic)
            }
            etFirstName.setText(it.first_name)
            etLastName.setText(it.last_name)
            etEmailAddress.setText(it.email_id)
            etMobileNo.setText(it.phone_no)
        }
        ivBack.setOnClickListener(this)
        ivProPic.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        //tvDob.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivBack -> {
                finish()
            }


            R.id.tvSave -> {
                editProfile()
            }
            R.id.ivProPic -> {
                if (!permissionUtils!!.checkPermission(PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION)) {
                    requestPermission()
                } else {
                    showImageChooserDialog()
                }
            }
           /* R.id.tvDob -> {
                finish()
            }*/
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
        resp: BaseResponse<String>
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




    private fun editProfile() {
        if (AppGlobal.isNetworkConnected(this)) run {

            var data: HashMap<String, String> = HashMap()
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            data[getString(R.string.param_first_name)] = vFirstName
            data[getString(R.string.param_last_name)] = vLastName
            data[getString(R.string.param_profile_status)] = vBio
            data[getString(R.string.param_profile_pic)] = uploadPath
            data[getString(R.string.param_birth_date)] = vBirthDate

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
            sneakerSuccess(this,resp.message)
            pref.setLoginData(resp.data)

        }
    }

}
