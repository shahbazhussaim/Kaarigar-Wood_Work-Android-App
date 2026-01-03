package com.kaarigar.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaarigar.data.Resource
import com.kaarigar.data.local.entity.UserEntity
import com.kaarigar.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<Resource<UserEntity>>()
    val authState: LiveData<Resource<UserEntity>> = _authState

    fun login(email: String, pass: String) {
        _authState.value = Resource.loading()
        viewModelScope.launch {
            _authState.value = repository.login(email, pass)
        }
    }

    fun register(email: String, pass: String, name: String, role: String, phone: String?) {
        _authState.value = Resource.loading()
        viewModelScope.launch {
            _authState.value = repository.register(email, pass, name, role, phone)
        }
    }
}
