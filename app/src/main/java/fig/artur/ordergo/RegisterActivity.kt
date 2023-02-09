package fig.artur.ordergo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private val emailPattern = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tv_login.setOnClickListener{
             onBackPressed()
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DBK)

        btn_register.setOnClickListener {
            val username: String = et_username_register.text.toString().trim()
            val email: String = et_email_register.text.toString().trim()
            val password: String = et_password_register.text.toString().trim()
            val confpassword: String = et_confirm_password_register.text.toString().trim()

            til_register_password.isPasswordVisibilityToggleEnabled = true
            til_register_confirm_password.isPasswordVisibilityToggleEnabled = true

            if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confpassword.isEmpty()){
                if(username.isEmpty()){
                    et_username_register.error = "ERROR! Enter your username!"
                }
                if(email.isEmpty()){
                    et_email_register.error = "ERROR! Enter your e-mail!"
                }
                if(password.isEmpty()){
                    et_password_register.error = "ERROR! Enter your password!"
                    til_register_password.isPasswordVisibilityToggleEnabled = false
                }
                if(confpassword.isEmpty()){
                    et_confirm_password_register.error = "ERROR! Re-enter your password!"
                    til_register_password.isPasswordVisibilityToggleEnabled = false
                }
            }else if(!email.matches(emailPattern.toRegex())){
                et_email_register.error = "ERROR! Enter valid e-mail address!"
            }else if(password.length < 7){
                et_password_register.error = "ERROR! Enter a password longer than 7 characters!"
            }else if(confpassword != password){
                et_confirm_password_register.error = "ERROR! Password not matched, try again."
            }else{
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ it ->
                    if(it.isSuccessful){
                        val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                        val users : Users = Users(username=username, email=email,  uid=auth.currentUser!!.uid)

                        databaseRef.setValue(users).addOnCompleteListener{
                            if(it.isSuccessful){
                                startActivity(Intent(this@RegisterActivity, TelNumberActivity::class.java))
                            }else{
                                Toast.makeText(this, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(this, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }

}