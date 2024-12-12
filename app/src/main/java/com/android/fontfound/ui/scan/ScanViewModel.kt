package com.android.fontfound.ui.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fontfound.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.android.fontfound.data.util.Result

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> get() = _uploadResult

    fun uploadHistory(
        imageFile: File,
        result: String,
        deviceId: String,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                _uploadResult.value = scanRepository.uploadHistory(
                    imageFile = imageFile,
                    result = result,
                    deviceId = deviceId,
                    context = context
                )
            } catch (e: Exception) {
                _uploadResult.value = Result.Error("Unexpected error: ${e.message}")
            }
        }
    }
}