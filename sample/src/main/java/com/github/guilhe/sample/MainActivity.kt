package com.github.guilhe.sample

import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.guilhe.recyclerpickerdialog.Item
import com.github.guilhe.recyclerpickerdialog.RecyclerPickerDialogFragment
import com.github.guilhe.recyclerpickerdialog.SelectionType
import com.github.guilhe.recyclerpickerdialog.SelectorType
import com.github.guilhe.recyclerpickerdialog.sample.R
import com.github.guilhe.recyclerpickerdialog.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val dialogA = RecyclerPickerDialogFragment.newInstance(onItemsPicked = { selected -> showToast(selected) }).apply {
            title = "Single Selection A"
            showSearchBar = true
            inputHint = "search by name…"
            data = arrayListOf<Item>().also {
                for (i in 1..20) {
                    it.add(Item("Choice $i"))
                }
            }
            itemsLayoutAnimator = R.anim.layout_animation_slide_down
            lifecycleOwner = this@MainActivity
        }
        binding.dialogAMaterialButton.setOnClickListener { dialogA.show(supportFragmentManager, "ADialogFragment") }

        val dialogB =
            RecyclerPickerDialogFragment.newInstance(
                SelectionType.SINGLE,
                SelectorType.SWITCH,
                R.style.DialogA,
                onItemsPicked = { selected -> showToast(selected) }
            ).apply {
                title = "Single Selection B"
                data = arrayListOf<Item>().also {
                    for (i in 1..10) {
                        it.add(Item("Choice $i"))
                    }
                }
                itemsLayoutAnimator = R.anim.layout_animation_slide_left
                dialogHeight = (500 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
                lifecycleOwner = this@MainActivity
                isCancelable = false
                isChoiceMandatory = true
            }
        binding.dialogBMaterialButton.setOnClickListener { dialogB.show(supportFragmentManager, "BDialogFragment") }

        val dialogC =
            RecyclerPickerDialogFragment.newInstance(
                SelectionType.MULTIPLE,
                SelectorType.RADIO_BUTTON,
                R.style.DialogB,
                onItemsPicked = { selected -> showToast(selected) }
            ).apply {
                title = "Multiple Selection C"
                data = arrayListOf<Item>().also {
                    for (i in 1..5) {
                        it.add(Item("Choice $i"))
                    }
                }
                dialogHeight = (500 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
                lifecycleOwner = this@MainActivity
                isChoiceMandatory = true
            }
        binding.dialogCMaterialButton.setOnClickListener { dialogC.show(supportFragmentManager, "CDialogFragment") }

        val dialogD =
            RecyclerPickerDialogFragment.newInstance(
                SelectionType.MULTIPLE,
                theme = R.style.DialogC,
                onItemsPicked = { selected -> showToast(selected) }
            ).apply {
                title = "Multiple Selection D"
                data = arrayListOf<Item>().also {
                    for (i in 1..10) {
                        it.add(Item("Choice $i"))
                    }
                }
                showSearchBar = true
                itemsLayoutAnimator = R.anim.layout_animation_slide_up
                lifecycleOwner = this@MainActivity
            }
        binding.dialogDMaterialButton.setOnClickListener { dialogD.show(supportFragmentManager, "DDialogFragment") }
    }

    private fun showToast(list: List<Item>) {
        toast?.cancel()
        toast = Toast.makeText(this@MainActivity, if (list.isEmpty()) "None" else list.joinToString(" - ") { it.text }, Toast.LENGTH_LONG)
        toast!!.show()
    }
}