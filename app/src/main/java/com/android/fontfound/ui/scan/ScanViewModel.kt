package com.android.fontfound.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fontfound.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
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
        createdAt: String,
        updatedAt: String,
        result: String,
        deviceId: String
    ) {
        viewModelScope.launch {
            try {
                _uploadResult.value = scanRepository.uploadHistory(
                    imageFile = imageFile,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    result = result,
                    deviceId = deviceId
                )
            } catch (e: Exception) {
                _uploadResult.value = Result.Error("Unexpected error: ${e.message}")
            }
        }
    }
}