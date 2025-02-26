package com.protect.jikigo_pay.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.protect.jikigo_pay.PayListener
import com.protect.jikigo_pay.applyNumberFormat
import com.protect.jikigo_pay.databinding.ItemPayItemBinding
import com.protect.Pay


class HomeAdapter(
    private val listener: PayListener
) : RecyclerView.Adapter<HomeViewHolder>() {
    private val items = mutableListOf<Pay>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder.from(parent, listener)
    }

    // 리스트 업데이트 함수 추가
    fun updateItems(newItems: List<Pay>) {
        items.clear() // 기존 데이터 초기화
        items.addAll(newItems) // 새 데이터 추가
        notifyDataSetChanged() // 데이터 변경 알림
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(items[position])
    }

}

class HomeViewHolder(
    private val binding: ItemPayItemBinding,
    private val listener: PayListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Pay) {
        itemView.setOnClickListener {
            listener.onClickPay(item)
        }
        with(binding) {
            tvPayName.text = item.payName
            tvPayPrice.applyNumberFormat(item.payPrice)
        }
    }


    companion object {
        fun from(parent: ViewGroup, listener: PayListener): HomeViewHolder {
            return HomeViewHolder(
                ItemPayItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), listener
            )
        }
    }
}