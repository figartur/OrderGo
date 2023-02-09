package fig.artur.ordergo

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_tel_number.*
import java.util.concurrent.TimeUnit

class TelNumberActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var number : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tel_number)

        number = " "
        auth = FirebaseAuth.getInstance()

        btn_continue.setOnClickListener{
            val countrycode : String = et_country_code.text.toString().trim {it <= ' '}
            val num : String = et_phone_contact.text.toString().trim {it <= ' '}
            val countrycodehandling: Char = countrycode[0]

            if(countrycodehandling != '+'){
                number = "+$countrycode $num"
                Toast.makeText(this, "$number", Toast.LENGTH_SHORT).show()
                OptionsAuth()
            }else if(countrycode.length < 4 || number.isEmpty()){
                et_country_code.error = "ERROR! Country code have 3 digits. Example: +999"
            }else if(number.length > 10 || number.length < 9 || number.isEmpty()){
                et_country_code.error = "ERROR! Phone number have 9 digits."
            }else{
                number = "$countrycode $num"
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
            println("C")
            val intent = Intent(this@TelNumberActivity, VerifyNumActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", number)
            startActivity(intent)
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