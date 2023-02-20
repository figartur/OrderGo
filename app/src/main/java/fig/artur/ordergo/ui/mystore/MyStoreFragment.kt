package fig.artur.ordergo.ui.mystore

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fig.artur.ordergo.R

class MyStoreFragment : Fragment() {

    companion object {
        fun newInstance() = MyStoreFragment()
    }

    private lateinit var viewModel: MyStoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_store, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MyStoreViewModel::class.java)
        // TODO: Use the ViewModel
    }

}