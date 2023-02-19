package fig.artur.ordergo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    private val emailPattern = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tv_login.setOnClickListener{
             onBackPressed()
        }

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
                val intent = Intent(this@RegisterActivity, TelNumberActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
            }

        }

    }

}