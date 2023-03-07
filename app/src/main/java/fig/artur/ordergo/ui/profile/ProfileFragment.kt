package fig.artur.ordergo.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import fig.artur.ordergo.BuildConfig
import fig.artur.ordergo.databinding.FragmentProfileBinding
import fig.artur.ordergo.logreg.LoginActivity
import java.util.Date

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var selectedImg : Uri


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(BuildConfig.DBK)

        if(auth.currentUser != null){
            val myRef = database.getReference("users")

            val currentUserUid = auth.currentUser?.uid
            val query = myRef.orderByChild("uid").equalTo(currentUserUid)

            var email = ""
            var phone = ""
            var username = ""

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        email = childSnapshot.child("email").value.toString()
                        phone = childSnapshot.child("phone").value.toString()
                        username = childSnapshot.child("username").value.toString()

                        val txtusername : TextView = binding.usernameProfileFrag
                        val txtemail : TextView = binding.emailProfileFrag
                        val txtphone : TextView = binding.phoneProfileFrag

                        txtusername.text = username
                        txtemail.text = email
                        txtphone.text = phone
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }else{
            Toast.makeText(context, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
        }


        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)}

        binding.profileFragImage.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.btnSave.setOnClickListener{
            if(selectedImg == null){
                Toast.makeText(context, "Please enter your profile pic", Toast.LENGTH_SHORT).show()
            }else{
                uploadData()
            }
        }

        return root
    }

    private fun uploadData(){
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImg).addOnCompleteListener{
            if(it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task ->
                    uploadInfo(task.toString())
                }
            }
        }
    }

    private fun uploadInfo(imgUrl: String) {
        val usersRef = database.getReference("Users")
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = usersRef.child(currentUserUid!!)

        val updates = mapOf<String, Any>(
            "profilepic" to "$imgUrl"
        )

        userRef.updateChildren(updates)
        //TODO("ADD IMG URL IN USERS (DATABASE)")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data != null){
            if(data.data != null){
                selectedImg = data.data!!

                binding.profileFragImage.setImageURI(selectedImg)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}