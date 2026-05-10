package com.example.dsmforofire.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

object FirebaseRefs {
    val auth: FirebaseAuth = Firebase.auth
    val firestore: FirebaseFirestore = Firebase.firestore

    val currentUser: FirebaseUser?
        get() = auth.currentUser
}