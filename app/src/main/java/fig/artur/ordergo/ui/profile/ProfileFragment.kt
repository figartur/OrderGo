package fig.artur.ordergo.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import fig.artur.ordergo.BuildConfig
import fig.artur.ordergo.R
import fig.artur.ordergo.databinding.FragmentProfileBinding
import fig.artur.ordergo.logreg.LoginActivity
import java.net.URL

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var storage : StorageReference
    private lateinit var imageUrl : URL


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
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
            val txtusername : TextView = binding.usernameProfileFrag
            val txtemail : TextView = binding.emailProfileFrag
            val txtphone : TextView = binding.phoneProfileFrag

            txtusername.text = username
            txtemail.text = email
            txtphone.text = phone
        }else{
            Toast.makeText(context, "ERROR! Something went wrong, try again.", Toast.LENGTH_SHORT).show()
        }


        val btn : Button = binding.btnLogout
        btn.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)}

        val pic : CircleImageView = binding.profileFragImage
        pic.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}