package com.rahul.openapi.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.rahul.openapi.R
import com.rahul.openapi.ui.*
import com.rahul.openapi.ui.main.create_blog.state.CreateBlogStateEvent
import com.rahul.openapi.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.rahul.openapi.util.ErrorHandling.ERROR_MUST_SELECT_IMAGE
import com.rahul.openapi.util.ErrorHandling.ERROR_SOMETHING_WRONG_WITH_IMAGE
import com.rahul.openapi.util.SuccessHandling
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_blog.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class CreateBlogFragment : BaseCreateBlogFragment() {

    private val TAG = "AppDebug"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_blog, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        blog_image.setOnClickListener {
            if (dataStateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        update_textview.setOnClickListener {
            if (dataStateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
        subscribeObservers()

    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                dataStateChangeListener.onDataStateChanged(dataState)
                dataState.data?.let { data ->
                    data.response?.let { event ->
                        event.peekContent()?.let { response ->
                            response.message?.let {
                                if (it == SuccessHandling.SUCCESS_BLOG_CREATED) {
                                    viewModel.clearNewBlogFields()
                                }
                            }
                        }
                    }
                }
            }

        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.newBlogFields.let { newBlogFields ->
                setBlogProperties(
                    newBlogFields.newBlogTitle,
                    newBlogFields.newBlogBody,
                    newBlogFields.newBlogImage
                )
            }
        })
    }

    private fun setBlogProperties(title: String?, body: String?, image: Uri?) {
        if (image != null) {
            dependencyProvider.getGlideRequestManager()
                .load(image)
                .into(blog_image)
        } else {
            dependencyProvider.getGlideRequestManager()
                .load(R.drawable.default_image)
                .into(blog_image)
        }

        blog_title.setText(title)
        blog_body.setText(body)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "CROP: RESULT OK")
            when (requestCode) {

                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let {
                            launchImageCrop(uri)
                        }
                    } ?: showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Log.d(TAG, "CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: $resultUri")
                    viewModel.setNewBlogFields(
                        title = null,
                        body = null,
                        uri = resultUri
                    )
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    Log.d(TAG, "CROP: ERROR")
                    showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.publish -> {
                val callback = object : AreyouSureCallback {
                    override fun cancel() {
                        //Ignore
                    }

                    override fun proceed() {
                        publishNewBlogPost()
                    }

                }
                uiCommunicationListener.onUIMessageRecieved(
                    UIMessage(
                        getString(R.string.are_you_sure_publish),
                        UIMessageType.AreYouSureDialog(callback)
                    )
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchImageCrop(uri: Uri) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        dataStateChangeListener.onDataStateChanged(
            DataState(
                Event(StateError(Response(errorMessage, ResponseType.Dialog()))),
                Loading(isLoading = false),
                Data(Event.dataEvent(null), null)
            )
        )
    }

    private fun publishNewBlogPost() {
        var multiPartBody: MultipartBody.Part? = null
        viewModel.getNewImageUri()?.let { uri ->
            uri.path?.let {
                val imageFile = File(it)
                val requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
                multiPartBody =
                    MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
            }
        }
        multiPartBody?.let {
            viewModel.setStateEvent(
                CreateBlogStateEvent.CreateNewBlogEvent(
                    blog_title.text.toString(),
                    blog_body.text.toString(),
                    it
                )
            )
            dataStateChangeListener.hideSoftKeyboard()
        } ?: showErrorDialog(ERROR_MUST_SELECT_IMAGE)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewBlogFields(
            blog_title.text.toString(),
            blog_body.text.toString(),
            null
        )
    }

}