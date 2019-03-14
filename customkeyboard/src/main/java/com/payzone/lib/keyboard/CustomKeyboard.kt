package com.payzone.lib.keyboard

import android.annotation.SuppressLint
import android.text.InputType
import android.widget.EditText
import android.view.View.OnFocusChangeListener
import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.media.AudioManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView

class CustomKeyboard {
    /** A link to the activity that hosts the [.mKeyboardView].  */
    private var mHostActivity: Activity = Activity()

    /** A link to the KeyboardView that is used to render this CustomKeyboard.  */
    private var mKeyboardView: KeyboardView? = null

    private var scrollView: ScrollView? = null

    /** The key (code) handler.  */
    private val mOnKeyboardActionListener = object: OnKeyboardActionListener {

        val CodeUpperCase = -1
        val CodeLowerCase = -2
        val CodeSymbol = -4
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
            val editText = focusCurrent as EditText
            val editable = editText.text
            val start = editText.selectionStart
            // Apply the key to the edit text
            if (primaryCode == CodeCancel) {
                hideCustomKeyboard()
            } else if (primaryCode == CodeDelete) {
                if (editable != null && start > 0) editable.delete(start - 1, start)
            } else if (primaryCode == CodeClear) {
                editable?.clear()
            } else if (primaryCode == CodeLeft) {
                if (start > 0) editText.setSelection(start - 1)
            } else if (primaryCode == CodeRight) {
                if (start < editText.length()) editText.setSelection(start + 1)
            } else if (primaryCode == CodeAllLeft) {
                editText.setSelection(0)
            } else if (primaryCode == CodeAllRight) {
                editText.setSelection(editText.length())
            } else if (primaryCode == CodePrev) {
                val focusNew = editText.focusSearch(View.FOCUS_RIGHT)
                focusNew?.requestFocus()
            } else if (primaryCode == CodeNext) {
                val focusNew = editText.focusSearch(View.FOCUS_RIGHT)
                focusNew?.requestFocus()
            } else if (primaryCode == CodeUpperCase) {
                mKeyboardView!!.keyboard = Keyboard(mHostActivity, R.xml.cap_keyboard)
                mKeyboardView!!.invalidateAllKeys()
            } else if (primaryCode == CodeLowerCase) {
                mKeyboardView!!.keyboard = Keyboard(mHostActivity, R.xml.keyboard)
                mKeyboardView!!.invalidateAllKeys()
            } else if (primaryCode == CodeSymbol) {
                mKeyboardView!!.keyboard = Keyboard(mHostActivity, R.xml.symbols)
                mKeyboardView!!.invalidateAllKeys()
            } else{ // insert character
                editable!!.insert(start, Character.toString(primaryCode.toChar()))
            }

            if(enableSound)
                beep()
        }

        override fun onPress(arg0: Int) {}

        override fun onRelease(primaryCode: Int) {}

        override fun onText(text: CharSequence) {}

        override fun swipeDown() {}

        override fun swipeLeft() {}

        override fun swipeRight() {}

        override fun swipeUp() {}

        private fun beep(){
            val vol = 1.0f
            audioManager!!.playSoundEffect(AudioManager.FX_KEY_CLICK, vol)
        }
    }

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see [Keyboard] for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the [.registerEditText].
     *
     * @param host The hosting activity.
     * @param scrollView The scroll view reference with the all contents in it
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     * @param enableSound Enabling and disabling the key tone
     *
     */
    constructor(host: Activity, scrollview: ScrollView, viewid: Int, layoutid: Int, enableSound: Boolean = false){
        mHostActivity = host
        scrollView = scrollview
        mKeyboardView = mHostActivity.findViewById<View>(viewid) as KeyboardView
        mKeyboardView!!.keyboard = Keyboard(mHostActivity, layoutid)
        mKeyboardView!!.isPreviewEnabled = false // NOTE Do not show the preview balloons
        mKeyboardView!!.setOnKeyboardActionListener(mOnKeyboardActionListener)
        // Hide the standard keyboard initially
        mHostActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        this.enableSound = enableSound

        // setup audio manager
        this.audioManager = mHostActivity.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    }

    /** Returns whether the CustomKeyboard is visible.  */
    fun isCustomKeyboardVisible(): Boolean {
        return mKeyboardView!!.visibility == View.VISIBLE
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v.  */
    fun showCustomKeyboard(v: View?) {
        mKeyboardView!!.visibility = View.VISIBLE
        mKeyboardView!!.isEnabled = true
        if (v != null) (mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            v.windowToken,
            0
        )
    }

    /** Make the CustomKeyboard invisible.  */
    fun hideCustomKeyboard() {
        scrollView!!.setPadding(0, 0, 0, 0)
        scrollView!!.scrollTo(scrollView!!.height, scrollView!!.width)

        mKeyboardView!!.visibility = View.GONE
        mKeyboardView!!.isEnabled = false
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
        // Disable the select text functionality
        editText.setTextIsSelectable(true)
        // Make the custom keyboard appear
        editText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            if (hasFocus) {
                dismissKeyboard(editText)
                val left = v.left
                val top = v.top
                val bottom = v.bottom
                val keyboardHeight = mKeyboardView!!.height

                // if the bottom of edit text is greater than scroll view height divide by 3,
                // it means that the keyboard is visible
                if (bottom > keyboardHeight) {
                    // increase scroll view with padding
                    scrollView!!.setPadding(0, 0, 0, keyboardHeight)
                    // scroll to the edit text position
                    scrollView!!.scrollTo(left, top)
        }

                var handler = android.os.Handler()
                handler.postDelayed({
                    showCustomKeyboard(v)
                }, 100)
            }
            else hideCustomKeyboard()
        }

        editText.setOnClickListener { v ->
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            showCustomKeyboard(v)


        }

        // Disable spell check (hex strings look like words to Android)
        editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    }

    fun dismissKeyboard(v: View) {
        val imm = mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}