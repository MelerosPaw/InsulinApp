package julioverne.insulinapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import julioverne.insulinapp.R
import julioverne.insulinapp.databinding.ActivityNavigationBinding
import julioverne.insulinapp.extensions.bind

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bind(R.layout.activity_navigation)
    }
}