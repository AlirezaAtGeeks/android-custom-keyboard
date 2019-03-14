package payzone.uk.co.customkeyboarddemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.payzone.lib.keyboard.CustomKeyboard
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var mCustomKeyboard: CustomKeyboard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCustomKeyboard = CustomKeyboard(
            this,
            this.mainScrollView,
            R.id.keyboardview,
            R.xml.keyboard,
            true
        )

        mCustomKeyboard!!.registerEditText(R.id.edittext0)
        mCustomKeyboard!!.registerEditText(R.id.edittext9)
        mCustomKeyboard!!.registerEditText(R.id.edittext11)
    }
}
