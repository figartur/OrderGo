package fig.artur.ordergo.logreg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import fig.artur.ordergo.MainActivity
import fig.artur.ordergo.R
import kotlinx.android.synthetic.main.activity_tel_number.*
import java.util.concurrent.TimeUnit

class TelNumberActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var number : String
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var email : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tel_number)

        username = intent.getStringExtra("username")!!
        password = intent.getStringExtra("password")!!
        email = intent.getStringExtra("email")!!

        auth = FirebaseAuth.getInstance()

        btn_continue.setOnClickListener{
            val countrycode : String = et_country_code.text.toString().trim {it <= ' '}
            val num : String = et_phone_contact.text.toString().trim {it <= ' '}
            val countrycodehandling: Char = countrycode[0]

            if(countrycodehandling != '+'){
                number = "+$countrycode$num"
                OptionsAuth()
            }else if(countrycode.length < 4){
                et_country_code.error = "ERROR! The country code has a maximum of 3 digits. Example: +999"
            }else if(num.length > 12 || num.length < 4){
                et_country_code.error = "ERROR! The phone number have between 4 and 12 numbers."
            }else if(num.isEmpty()){
                et_phone_contact.error = "ERROR! Enter the phone number!"
            }else if(countrycode.isEmpty()){
                et_country_code.error = "ERROR! Enter the country code!"
            }else{
                number = "$countrycode$num"
                OptionsAuth()
            }
        }
    }

    private fun OptionsAuth(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@TelNumberActivity, MainActivity::class.java))
                } else {
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d("TAG", "FirebaseAuthInvalidCredentialsException: ${task.exception.toString()}")
                    }
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

        override fun onCodeSent( verificationId: String, token: PhoneAuthProvider.ForceResendingToken ) {
            val intent = Intent(this@TelNumberActivity, VerifyNumActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", number)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }
    }

}