package com.github.guilhe.recyclerpickerdialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.guilhe.recyclerpickerdialog.databinding.ViewRowForItemBinding
import java.util.*

class ItemsAdapter(
    selection: SelectionType,
    val selectorType: SelectorType,
    private val onDataUpdate: (old: Item?, new: Item, isSingle: Boolean) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private val data: ArrayList<Item> = arrayListOf()
    private val isSingle = selection == SelectionType.SINGLE
    private var lastItemSelected: Item? = null

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewRowForItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = data[position]
        holder.binding.selector = selectorType
        holder.binding.percent = if (selectorType == SelectorType.SWITCH) 0.15f else 0.12f
        holder.binding.executePendingBindings() //because selector visibility

        holder.binding.root.setOnClickListener {
            when (selectorType) {
                SelectorType.CHECK_BOX -> holder.binding.checkButton.performClick()
                SelectorType.RADIO_BUTTON -> holder.binding.radioButton.performClick()
                SelectorType.SWITCH -> holder.binding.switchButton.performClick()
            }
        }
        holder.binding.radioButton.setOnClickListener { onUpdate(holder) }
        holder.binding.checkButton.setOnClickListener { onUpdate(holder) }
        holder.binding.switchButton.setOnClickListener { onUpdate(holder) }
    }

    private fun onUpdate(holder: ViewHolder) {
        onDataUpdate.invoke(lastItemSelected, data[holder.adapterPosition], isSingle)
        lastItemSelected = data[holder.adapterPosition]
    }

    fun updateItems(items: ArrayList<Item>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ViewRowForItemBinding) : RecyclerView.ViewHolder(binding.root)
}

object ViewRowItemBindingAdapter {
    @JvmStatic
    @BindingAdapter(value = ["itemGuidelinePercent"])
    fun setItemGuidelinePercent(view: Guideline, percent: Float = 0f) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.guidePercent = percent
        view.layoutParams = params
    }
}