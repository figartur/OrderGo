package fig.artur.ordergo

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_tel_number.*
import kotlinx.android.synthetic.main.activity_tel_number.btn_continue
import kotlinx.android.synthetic.main.activity_verify_num.*
import java.util.concurrent.TimeUnit

//TODO("https://stackoverflow.com/questions/53140930/android-progressbar-hiving-nullpointerexception-android-widget-progressbar-setvi")

class TelNumberActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var number : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tel_number)

        number = et_phone_contact.text.toString().trim {it <= ' '}
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DBK)
        progressBar2.visibility = View.INVISIBLE

        btn_continue.setOnClickListener{
            val countrycode : String = et_country_code.text.toString().trim {it <= ' '}
            val countrycodehandling: Char = countrycode[0]

            if(countrycodehandling != '+'){
                number = "+"+"$countrycode"+"$number"
                progressBar2.visibility = View.VISIBLE
            }else if(countrycode.length < 4 || number.isEmpty()){
                et_country_code.error = "ERROR! Country code have 3 digits. Example: +999"
            }else if(number.length > 10 || number.length < 9 || number.isEmpty()){
                et_country_code.error = "ERROR! Phone number have 9 digits."
            }else{
                number = "$countrycode"+"$number"
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(number)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                     .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            val intent = Intent(this@TelNumberActivity, VerifyNumActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", number)
            startActivity(intent)
            progressBar2.visibility = View.INVISIBLE
        }
    }

    override fun onStart(){
        super.onStart()
        if(auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@TelNumberActivity, MainActivity::class.java))
                } else {
                    Log.d(TAG, "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d(TAG, "FirebaseAuthInvalidCredentialsException: ${task.exception.toString()}")
                    }
                }
            }

    }
}