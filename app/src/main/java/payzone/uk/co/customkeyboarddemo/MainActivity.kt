package payzone.uk.co.customkeyboarddemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    var mCustomKeyboard: CustomKeyboard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCustomKeyboard = CustomKeyboard(this, R.id.keyboardview, payzone.uk.co.customkeyboard.xml.keyboard)

        mCustomKeyboard!!.registerEditText(R.id.edittext0)
        mCustomKeyboard!!.registerEditText(R.id.edittext1)
    }
}
