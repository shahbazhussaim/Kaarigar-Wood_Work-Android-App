package com.kaarigar.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.data.Resource
import com.kaarigar.data.local.dao.UserDao
import com.kaarigar.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {

    private val _currentUser = MutableLiveData<Resource<UserEntity>>()
    val currentUser: LiveData<Resource<UserEntity>> get() = _currentUser

    suspend fun login(email: String, pass: String): Resource<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // Hardcoded Admin Check
                if (email.equals("Admin@gmail.com", ignoreCase = true) && pass == "12345678") {
                    val adminUser = UserEntity(
                        uid = "admin_001",
                        name = "Administrator",
                        email = "Admin@gmail.com",
                        role = "ADMIN",
                        phoneNumber = null
                    )
                    return@withContext Resource.success(adminUser)
                }

                Log.d("AuthRepo", "Attempting login for $email")
                val authResult = try {
                    auth.signInWithEmailAndPassword(email, pass).await()
                } catch (e: Exception) {
                    throw Exception("Auth Failed: ${e.message}")
                }
                
                val uid = authResult.user?.uid ?: throw Exception("UID is null")
                Log.d("AuthRepo", "Login auth success, UID: $uid")
                
                // Fetch role to determine navigation
                Log.d("AuthRepo", "Fetching user role from Firestore...")
                val doc = try {
                     firestore.collection("users").document(uid).get().await()
                } catch (e: Exception) {
                    Log.e("AuthRepo", "Firestore unreachable: ${e.message}")
                    null
                }
                
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: "User"
                    val role = doc.getString("role") ?: "CUSTOMER"
                    val phone = doc.getString("phone")
                    
                    val userEntity = UserEntity(
                        uid = uid,
                        name = name,
                        email = email,
                        role = role.trim().uppercase(),
                        phoneNumber = phone
                    )
                    userDao.insertUser(userEntity)
                    return@withContext Resource.success(userEntity)
                } else {
                    Log.e("AuthRepo", "User doc MISSING for $uid. No default to CUSTOMER to avoid misrouting.")
                    return@withContext Resource.error("Profile not found. Please contact Admin.")
                }
            } catch (e: Exception) {
                Log.e("AuthRepo", "Login error: ${e.message}", e)
                return@withContext Resource.error(e.message ?: "Login Failed")
            }
        }
    }

    suspend fun register(email: String, pass: String, name: String, role: String, phone: String?): Resource<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepo", "Starting registration for $email")
                
                // Check if email is in worker_invites
                val inviteSnapshot = firestore.collection("worker_invites")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                    
                val finalRole = if (!inviteSnapshot.isEmpty) "WORKER" else role
                Log.d("AuthRepo", "Assigned Role: $finalRole")

                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = authResult.user?.uid ?: throw Exception("UID is null")
                Log.d("AuthRepo", "Auth success, UID: $uid")
                
                val userData = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to finalRole.trim().uppercase(),
                    "phone" to phone,
                    "createdAt" to System.currentTimeMillis()
                )
                
                // Save to Firestore
                Log.d("AuthRepo", "Saving to Firestore...")
                firestore.collection("users").document(uid).set(userData).await()
                Log.d("AuthRepo", "Firestore success")
                
                // If it was an invite, maybe delete the invite? Or keep it as record. 
                // Let's delete it to prevent reuse if we care, or keep it.
                // For now, keep it simple.
                
                val userEntity = UserEntity(
                    uid = uid,
                    name = name,
                    email = email,
                    role = finalRole,
                    phoneNumber = phone
                )
                
                // Cache to Room
                Log.d("AuthRepo", "Saving to Room...")
                userDao.insertUser(userEntity)
                Log.d("AuthRepo", "Room success")
                
                return@withContext Resource.success(userEntity)
            } catch (e: Exception) {
                Log.e("AuthRepo", "Registration error: ${e.message}", e)
                return@withContext Resource.error(e.message ?: "Registration Failed")
            }
        }
    }

    fun logout() {
        auth.signOut()
        // clear local data logic here if needed (e.g. userDao.clearUsers())
        // but often we might want to keep some non-sensitive cache. 
        // For security, let's clear.
    }
}
