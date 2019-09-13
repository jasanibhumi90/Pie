package com.pie.ui.editprofile

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
import com.app.utils.RxBus
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
import com.pie.model.Profile
import com.pie.ui.base.BaseActivity
import com.pie.utils.AppConstant
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.PermissionUtils
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_register_profile.*
import kotlinx.android.synthetic.main.editprofile.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import java.io.File
import java.util.*
import com.pie.ui.editprofile.UpdateThingsActivity as UpdateThingsActivity1

class EditProfileActivity : BaseActivity(), View.OnClickListener, ImagePickerCallback,
    PermissionUtils.OnPermissionResponse, FilePickerCallback {
    private var vBirthDate: String = ""
    private var pickerPath: String = ""
    private var uploadPath: String = ""
    private var vFirstName: String = ""
    private var vLastName: String = ""
    private var vMobileNo: String = ""
    private var vBirthdate: String = ""
    private var vEmail: String = ""
    private var vThingsIds: String = ""
    private var vBio: String = ""
    private var imagePicker: ImagePicker? = null
    private var cameraPicker: CameraImagePicker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editprofile)
        pref.getLoginData()?.let {
            if (it.profile_pic.isNotEmpty()) {
                Glide.with(this).load(it.profile_pic).into(ivProPicEdit)
            }
            vFirstName = it.first_name
            vEmail = it.email_id
            vLastName = it.last_name
            vMobileNo = it.phone_no
            uploadPath = it.profile_pic
            vBirthDate = it.birth_date
            vThingsIds = it.things_ids
            vBio=it.profile_status
            etFirstName.setText(vFirstName)
            etLastName.setText(vLastName)
            etEmailAddress.setText(vEmail)
            etMobileNo.setText("+" + it.country_code + " " + vMobileNo)
            tvDob.setText(it.birth_date)
            etBio.setText(it.profile_status)
        }
        ivBack.setOnClickListener(this)
        ivProPicEdit.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        permissionUtils = PermissionUtils(this)
        tvDob.setOnClickListener(this)
        tvInterest.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvSave -> {
                editProfile()
            }
            R.id.tvInterest -> {
                startActivityForResult<com.pie.ui.editprofile.UpdateThingsActivity>(100)
            }

            R.id.tvDob -> {
                val calendar = Calendar.getInstance()
                val yy = calendar.get(Calendar.YEAR)
                val mm = calendar.get(Calendar.MONTH)
                val dd = calendar.get(Calendar.DAY_OF_MONTH)
                val mdiDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker,
                        year: Int,
                        monthOfYear: Int,
                        dayOfMonth: Int
                    ) {
                        vBirthDate = year.toString() + "-" + String.format(
                            "%02d",
                            monthOfYear+1
                        ) + "-" + String.format("%02d", dayOfMonth)
                        tvDob.text = vBirthDate

                    }
                }, yy, mm, dd)
                mdiDialog.show()
            }
            R.id.ivProPicEdit -> {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
                                .into(ivProPicEdit)
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
                100->{
                    if(data?.hasExtra("things_ids")!!) {
                        vThingsIds = data.getStringExtra("things_ids").toString()
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
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onFileUpload(
        resp: BaseResponse<String>
    ) {
        Log.e("tag", "resp" + resp)
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
            if (etFirstName.text.isNotEmpty())
                vFirstName = etFirstName.text.toString()
            if (etLastName.text.isNotEmpty())
                vLastName = etLastName.text.toString()
            if (etBio.text.isNotEmpty())
                vBio = etBio.text.toString()
            if (tvDob.text.isNotEmpty())
                vBirthDate = tvDob.text.toString()

            val data: HashMap<String, String> = HashMap()
            val auth = HashMap<String, Any>()
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            data[getString(R.string.param_first_name)] = vFirstName
            data[getString(R.string.param_last_name)] = vLastName
            data[getString(R.string.param_profile_status)] = vBio
            data[getString(R.string.param_profile_pic)] = uploadPath
            data[getString(R.string.param_birth_date)] = vBirthDate
            data[getString(R.string.param_country_name)] = pref.getLoginData()!!.country_name
            data[getString(R.string.param_email_id)] = pref.getLoginData()!!.email_id
            data[getString(R.string.param_password)] = pref.getLoginData()!!.password
            data[getString(R.string.param_phone_no)] = pref.getLoginData()!!.phone_no
            data[getString(R.string.param_country_code)] = pref.getLoginData()!!.country_code
            data[getString(R.string.param_gender)] = pref.getLoginData()!!.gender
            data[getString(R.string.param_Things_ids)] = vThingsIds
            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()
            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_update_profile)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.register(service), true)
                ?.subscribe({ onRegister(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun onRegister(
        resp: BaseResponse<Profile>
    ) {
        if(onStatusFalse(resp,true)) return
        resp.data?.let {
          /*  val bundle = Bundle()
            bundle.putString(AppConstant.ARG_DETAIL_DELETE, "")
            RxBus.publish(Bundle(bundle))*/
            sneakerSuccess(this, resp.message)
            pref.setLoginData(it)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

}
