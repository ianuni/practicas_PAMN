package com.example.ereceipt

import android.app.AlertDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ereceipt.Databases.FirebaseImplementation
import com.example.ereceipt.Databases.SQLite
import com.example.ereceipt.Fragments.AddInvoiceFragment
import com.example.ereceipt.Fragments.InboxFragment
import com.example.ereceipt.Fragments.InvoicesFragment
import com.example.ereceipt.Fragments.ProfileFragment
import com.example.ereceipt.Model.Company
import com.example.ereceipt.Model.Invoice
import com.example.ereceipt.ViewModels.CompanyViewModel
import com.example.ereceipt.ViewModels.DatabasesViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch


class DockActivity : AppCompatActivity() {
    private lateinit var invoices: ImageButton
    private lateinit var addInvoice: ImageButton
    private lateinit var inbox: ImageButton
    private lateinit var profile: ImageButton
    private lateinit var currentBtn :ImageButton
    private lateinit var qr :ImageView
    private val dbViewModel : DatabasesViewModel by viewModels()
    private val companyViewModel : CompanyViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dock)
        dbViewModel.setFirebase(FirebaseImplementation())
        dbViewModel.setSQLite(SQLite(this))
        loadDataOnViewModels()

        qr = findViewById(R.id.qr)
        invoices = findViewById(R.id.invoices)
        addInvoice = findViewById(R.id.add_invoice)
        inbox = findViewById(R.id.inbox)
        profile = findViewById(R.id.profile)
        currentBtn = invoices
        val addInvoiceFragment = AddInvoiceFragment()
        val invoicesFragment = InvoicesFragment()
        val inboxFragment = InboxFragment()
        val profileFragment = ProfileFragment()

        invoices.setOnClickListener {
            onClickHandle(invoicesFragment, invoices)
        }
        addInvoice.setOnClickListener {
            onClickHandle(addInvoiceFragment, addInvoice)
        }
        inbox.setOnClickListener {
            onClickHandle(inboxFragment, inbox)
        }
        profile.setOnClickListener {
            onClickHandle(profileFragment, profile)
        }

        invoices.isClickable = false
        addInvoice.isClickable = false
        inbox.isClickable = false
        profile.isClickable = false

        qr.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val creationView = layoutInflater.inflate(R.layout.dialog_qr, null)
            builder.setView(creationView)
            val dialog = builder.create()
            setDialogData(creationView)
            dialog.show()
        }

    }

    private fun setDialogData(creationView: View) {
        try {
            var barcodeEncoder = BarcodeEncoder()
            var bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                companyViewModel.company.value?.nif.toString(),
                BarcodeFormat.QR_CODE,
                300,
                300
            )
            creationView.findViewById<ImageView>(R.id.QRCodeImageView).setImageBitmap(bitmap)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        return
    }

    private fun colorIcon (btn: ImageButton, color : Int) {
        btn.background.setTint(resources.getColor(color))
    }

    private fun navigate (nextFragment : Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.currentFragment, nextFragment).commit()
    }

    private fun updateDock (nextBtn : ImageButton) {
        colorIcon(currentBtn, R.color.black)
        colorIcon(nextBtn, R.color.blue)
        currentBtn = nextBtn
    }

    private fun onClickHandle(nextFragment : Fragment, nextBtn : ImageButton) {
        if (nextBtn != currentBtn){
            navigate(nextFragment)
            updateDock(nextBtn)
        }
    }

    private fun loadDataOnViewModels(){
        dbViewModel.setFirebase(FirebaseImplementation())
        dbViewModel.setSQLite(SQLite(this))
        var company: Company? = null
        lifecycleScope.launch {
            for (i in 1..10){
                company = dbViewModel.myFirebase.value?.getCompany()
                if (company != null) {
                    companyViewModel.setCompany(company!!)
                    Log.e("a","go to fragment")
                    navigate(InvoicesFragment())
                    invoices.isClickable = true
                    addInvoice.isClickable = true
                    inbox.isClickable = true
                    profile.isClickable = true
                    Log.e("INTENTOS", "el número de intentos para cargar la compañia fueron: " + i.toString())
                    break
                }
            }
            if (company != null) {
                val invoices: Collection<Invoice>? = dbViewModel.myFirebase.value?.getInvoices(company!!.nif)
                if (invoices != null) {
                    companyViewModel.setInvoices(invoices)
                    //updateLocalDatabase()
                } else Log.e("a", "couldnt load invoices")
            } else Log.e("a", "couldnt load company")
            Log.e("a", "cargo los datos")
        }
    }


}
