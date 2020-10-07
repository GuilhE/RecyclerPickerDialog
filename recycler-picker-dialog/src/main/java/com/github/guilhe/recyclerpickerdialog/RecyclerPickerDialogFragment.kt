@file:Suppress("unused")

package com.github.guilhe.recyclerpickerdialog

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AnimRes
import androidx.annotation.StyleRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DefaultItemAnimator
import com.github.guilhe.recyclerpickerdialog.databinding.FragmentRecyclerPickerDialogBinding
import java.io.Serializable
import java.util.*

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
class RecyclerPickerDialogFragment(private val onDismiss: (selected: List<Item>) -> Unit) : DialogFragment(), LifecycleEventObserver {

    private var dataFiltered: ArrayList<Item> = arrayListOf()
    private lateinit var dataAdapter: ItemsAdapter
    private lateinit var binding: FragmentRecyclerPickerDialogBinding

    var title = ""
    var inputHint = ""
    var buttonText: String = "Ok"
    var showSearchBar = false
    var resetValuesOnShow = true
    var dismissKeyboardOnSelection = true
    var dismissOnSelection = false
    var data: ArrayList<Item> = arrayListOf()
        set(value) {
            field = value
            if (resetValuesOnShow) {
                field.forEach { it.isSelected = false }
            }
            dataFiltered.clear()
            dataFiltered.addAll(field)
            if (::dataAdapter.isInitialized) {
                dataAdapter.updateItems(dataFiltered)
            }
        }

    @AnimRes
    var itemsLayoutAnimator: Int? = null

    var dialogHeight: Int = ViewGroup.LayoutParams.MATCH_PARENT
        set(value) {
            field = if (value < 0) ViewGroup.LayoutParams.MATCH_PARENT else value
        }

    var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            field = value
            field?.lifecycle?.addObserver(this@RecyclerPickerDialogFragment)
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY && isAdded) {
            dismissAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataAdapter = ItemsAdapter(
            (arguments?.getSerializable(EXTRA_FOR_SELECTION_TYPE) ?: SelectionType.SINGLE) as SelectionType,
            (arguments?.getSerializable(EXTRA_FOR_SELECTOR_TYPE) ?: SelectorType.CHECK_BOX) as SelectorType,
            onDataUpdate = { old, new, isSingle ->
                if (isSingle) {
                    data.find { it.id == new.id }?.isSelected = if (old == new) new.isSelected.not() else true
                    old?.let { data.find { it.id == old.id && it.id != new.id }?.isSelected = false }
                    /** note: [dataFiltered] will automatically replicate [data] changes, this is because they are a ref copy.**/
                    dataAdapter.notifyItemChanged(dataFiltered.indexOf(new))
                    dataAdapter.notifyItemChanged(dataFiltered.indexOf(old))
                } else {
                    data.find { it.id == new.id }?.isSelected = !new.isSelected
                    dataAdapter.notifyItemChanged(dataFiltered.indexOf(new))
                }

                if (dismissOnSelection) {
                    binding.btnOk.performClick()
                } else {
                    stealFocusFromInput()
                }
            }
        )
        setStyle(STYLE_NO_TITLE, arguments?.getInt(EXTRA_FOR_THEME, R.style.RecyclerPickerDialogTheme) ?: R.style.RecyclerPickerDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentRecyclerPickerDialogBinding>(inflater, R.layout.fragment_recycler_picker_dialog, null, false)
            .also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = title
        binding.searchLayout.visibility = if (showSearchBar) View.VISIBLE else View.GONE
        binding.search.hint = inputHint
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filterText = s?.toString()
                dataFiltered.clear()
                if (filterText.isNullOrEmpty()) {
                    dataFiltered.addAll(data)
                } else {
                    dataFiltered.addAll(data.filter { it.text.contains(filterText, true) })
                }
                if (::dataAdapter.isInitialized) {
                    dataAdapter.updateItems(dataFiltered)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        if (resetValuesOnShow) {
            data.forEach { it.isSelected = false }
            dataFiltered.clear()
            dataFiltered.addAll(data)
        }

        with(binding.recyclerView) {
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            itemsLayoutAnimator?.let { layoutAnimation = AnimationUtils.loadLayoutAnimation(context, it) }
            adapter = dataAdapter.apply { updateItems(dataFiltered) }
        }

        binding.btnOk.text = buttonText
        binding.btnOk.setOnClickListener {
            onDismiss.invoke(data.filter { it.isSelected })
            this.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight)
        binding.search.setText("")
        binding.recyclerView.scrollToPosition(0)
    }

    private fun stealFocusFromInput() {
        if (dismissKeyboardOnSelection && showSearchBar) {
            binding.title.requestFocus()
            val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.search.windowToken, 0)
        }
    }

    companion object {
        fun newInstance(
            type: SelectionType = SelectionType.SINGLE,
            selector: SelectorType = SelectorType.CHECK_BOX,
            @StyleRes theme: Int = R.style.RecyclerPickerDialogTheme,
            onDismiss: (List<Item>) -> Unit
        ) = RecyclerPickerDialogFragment(onDismiss).apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_FOR_SELECTION_TYPE, type)
                putSerializable(EXTRA_FOR_SELECTOR_TYPE, selector)
                putInt(EXTRA_FOR_THEME, theme)
            }
        }

        private const val EXTRA_FOR_SELECTION_TYPE = "EXTRA_FOR_SELECTION_TYPE"
        private const val EXTRA_FOR_SELECTOR_TYPE = "EXTRA_FOR_SELECTOR_TYPE"
        private const val EXTRA_FOR_THEME = "EXTRA_FOR_THEME"
    }
}

enum class SelectionType { SINGLE, MULTIPLE }

enum class SelectorType { CHECK_BOX, RADIO_BUTTON, SWITCH }

data class Item(val text: String) : Serializable {
    val id = UUID.randomUUID().toString()
    var isSelected: Boolean = false
}