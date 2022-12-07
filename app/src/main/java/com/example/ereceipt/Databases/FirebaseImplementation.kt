package com.example.ereceipt.Databases

import android.util.Log
import com.example.ereceipt.Model.Company
import com.example.ereceipt.Model.Invoice
import com.example.ereceipt.Repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

class FirebaseImplementation constructor(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val invoiceDB: CollectionReference = firebaseFireStore.collection("invoices"),
    private val companyDB: CollectionReference = firebaseFireStore.collection("companies")

): FirebaseRepository {

    override suspend fun signIn(email: String, password: String) : Boolean {
        return try {
            var res = false
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    res = task.isSuccessful
                    if (res){
                        Log.e("b","success")
                    }
                    else {
                        Log.e("b", "invalid email or password")
                    }
                }
                .await()
            res
        } catch (e: Exception){
            false
        }
    }

    override suspend fun signUp(email: String, password: String) : FirebaseUser? {
        var user: FirebaseUser? = null
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    user = firebaseAuth.currentUser
                    Log.e("b","success")
                }
                else {
                    Log.e("b", "user already exists")
                }
            }
            .await()
        return user
    }

    override fun logOut() : Boolean{
        if (firebaseAuth.currentUser?.uid != null){
            firebaseAuth.signOut()
            return true
        }
        return false
    }


    override suspend fun createCompany(user: FirebaseUser, company: Company) : Boolean{
        return try{
            var res = false
            companyDB.document(user.uid).set(company)
                .addOnCompleteListener{ task ->
                    res = task.isSuccessful
                    if (res){
                        Log.e("c","success on creating company in firestore")
                    } else{
                        Log.e("b","failed on creating company in firestore")
                    }
                }
                .await()
            res
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCompany(): Company? {
        return try {
            var company: Company? = null
            companyDB.document(firebaseAuth.currentUser!!.uid)
                .get().addOnSuccessListener { document ->
                    company = document.toObject<Company>()
                }
                .await()
            company
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCompany(nif: String): Company? {
        return try {
            var company: Company? = null
            companyDB.whereEqualTo("nif", nif)
                .get().addOnSuccessListener { documents ->
                    company = documents.toObjects<Company>()
                }
                .await()
            company
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getInvoices(nif: String): Collection<Invoice>{
        return try {
            var invoices: MutableCollection<Invoice> = mutableListOf()
            invoiceDB.whereEqualTo("sellerNif", nif)
                .get().addOnSuccessListener { documents ->
                    invoices.addAll(documents.toObjects<Invoice>())
                }
                .await()
            invoiceDB.whereEqualTo("buyerNif", nif)
                .get().addOnSuccessListener { documents ->
                    invoices.addAll(documents.toObjects<Invoice>())
                }
                .await()
            invoices
        } catch (e: Exception) {
            listOf()
        }
    }

    override suspend fun createInvoice(invoice: Invoice): Boolean {
        return try{
            var res = false
            firebaseFireStore.collection("invoices").document().set(invoice)
                .addOnCompleteListener{ task ->
                    res = task.isSuccessful
                    if (res){
                        Log.e("c","success on creating invoice in firestore")
                    } else{
                        Log.e("b","failed on creating invoice in firestore")
                    }
                }
                .await()
            res
        } catch (e: Exception) {
            false
        }
    }

    fun getFireAuth() {
        Log.e("awd", this.firebaseAuth.app.toString())
    }
}