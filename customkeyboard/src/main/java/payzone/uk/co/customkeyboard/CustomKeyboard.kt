package payzone.uk.co.customkeyboard

import android.annotation.SuppressLint
import android.text.InputType
import android.widget.EditText
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.View.OnFocusChangeListener
import android.app.Activity
import android.view.WindowManager
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.view.View
import android.view.inputmethod.InputMethodManager


class CustomKeyboard {
    /** A link to the KeyboardView that is used to render this CustomKeyboard.  */
    private var mKeyboardView: KeyboardView
    /** A link to the activity that hosts the [.mKeyboardView].  */
    private var mHostActivity: Activity = Activity()

    /** The key (code) handler.  */
    private val mOnKeyboardActionListener = object: OnKeyboardActionListener {

        val CodeDelete = -5 // Keyboard.KEYCODE_DELETE
        val CodeCancel = -3 // Keyboard.KEYCODE_CANCEL
        val CodePrev = 55000
        val CodeAllLeft = 55001
        val CodeLeft = 55002
        val CodeRight = 55003
        val CodeAllRight = 55004
        val CodeNext = 55005
        val CodeClear = 55006

        override fun onKey(primaryCode: Int, keyCodes: IntArray) {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file; all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText and its Editable
            val focusCurrent = mHostActivity.window.currentFocus
            if (focusCurrent == null || focusCurrent::class.java !== android.support.v7.widget.AppCompatEditText::class.java) return
            val edittext = focusCurrent as EditText
            val editable = edittext.text
            val start = edittext.selectionStart
            // Apply the key to the edit text
            if (primaryCode == CodeCancel) {
                hideCustomKeyboard()
            } else if (primaryCode == CodeDelete) {
                if (editable != null && start > 0) editable.delete(start - 1, start)
            } else if (primaryCode == CodeClear) {
                editable?.clear()
            } else if (primaryCode == CodeLeft) {
                if (start > 0) edittext.setSelection(start - 1)
            } else if (primaryCode == CodeRight) {
                if (start < edittext.length()) edittext.setSelection(start + 1)
            } else if (primaryCode == CodeAllLeft) {
                edittext.setSelection(0)
            } else if (primaryCode == CodeAllRight) {
                edittext.setSelection(edittext.length())
            } else if (primaryCode == CodePrev) {
                val focusNew = edittext.focusSearch(View.FOCUS_RIGHT)
                focusNew?.requestFocus()
            } else if (primaryCode == CodeNext) {
                val focusNew = edittext.focusSearch(View.FOCUS_RIGHT)
                focusNew?.requestFocus()
            } else { // insert character
                editable!!.insert(start, Character.toString(primaryCode.toChar()))
            }
        }

        override fun onPress(arg0: Int) {}

        override fun onRelease(primaryCode: Int) {}

        override fun onText(text: CharSequence) {}

        override fun swipeDown() {}

        override fun swipeLeft() {}

        override fun swipeRight() {}

        override fun swipeUp() {}
    }

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see [Keyboard] for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the [.registerEditText].
     *
     * @param host The hosting activity.
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    constructor(host: Activity, viewid: Int, layoutid: Int){
        mHostActivity = host
        mKeyboardView = mHostActivity.findViewById<View>(viewid) as KeyboardView
        mKeyboardView.keyboard = Keyboard(mHostActivity, layoutid)
        mKeyboardView.isPreviewEnabled = false // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener)
        // Hide the standard keyboard initially
        mHostActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    /** Returns whether the CustomKeyboard is visible.  */
    fun isCustomKeyboardVisible(): Boolean {
        return mKeyboardView.visibility == View.VISIBLE
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v.  */
    fun showCustomKeyboard(v: View?) {
        mKeyboardView.visibility = View.VISIBLE
        mKeyboardView.isEnabled = true
        if (v != null) (mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            v.windowToken,
            0
        )
    }

    /** Make the CustomKeyboard invisible.  */
    fun hideCustomKeyboard() {
        mKeyboardView.visibility = View.GONE
        mKeyboardView.isEnabled = false
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
    </var></var> */
    @SuppressLint("ClickableViewAccessibility")
    fun registerEditText(resid: Int) {
        // Find the EditText 'resid'
        val editText = mHostActivity.findViewById<View>(resid) as EditText
        // Make the custom keyboard appear
        editText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            if (hasFocus) showCustomKeyboard(v) else hideCustomKeyboard()
        }
        editText.setOnClickListener { v ->
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            showCustomKeyboard(v)


        }
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        editText.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val edittext = v as EditText
                val inType = edittext.inputType       // Backup the input type
                edittext.inputType = InputType.TYPE_NULL // Disable standard keyboard
                edittext.onTouchEvent(event)               // Call native handler
                edittext.inputType = inType              // Restore input type
                return true // Consume touch event
            }
        })

        // Disable spell check (hex strings look like words to Android)
        editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    }
}