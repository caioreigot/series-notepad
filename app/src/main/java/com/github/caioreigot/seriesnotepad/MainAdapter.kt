package com.github.caioreigot.seriesnotepad

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.github.caioreigot.seriesnotepad.model.ArrowButtons
import com.github.caioreigot.seriesnotepad.model.MainInformation
import java.util.*

class MainAdapter(
    val informationData: MutableList<MainInformation>,
    private val scrollToPosition: (position: Int) -> Unit,
    private val notifyChanges: () -> Unit
) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private var mRecentlyDeletedItem: MainInformation? = null
    private var mRecentlyDeletedItemPosition = -1

    inner class MainViewHolder(
        itemView: View,
        private val context: Context): RecyclerView.ViewHolder(itemView)
    {

        private var header: TextView = itemView.findViewById(R.id.item_header)

        private var episodeViewGroup: ViewGroup = itemView.findViewById(R.id.episode_view_group)
        var episode: TextView = itemView.findViewById(R.id.item_episode)

        private var decreaseBtn: Button = itemView.findViewById(R.id.decrease_button)
        private var increaseBtn: Button = itemView.findViewById(R.id.increase_button)

        fun bind(info: MainInformation) {
            header.text = info.header
            episode.text = info.episode

            header.setOnClickListener {
                Dialog(context).apply {
                    setContentView(R.layout.main_edit_dialog)

                    // Header of Edit Field Dialog
                    findViewById<TextView>(R.id.edit_header).apply {
                        text = context.resources.getString(R.string.name)
                    }

                    // Edit Text for user input
                    val editField = findViewById<EditText>(R.id.edit_field_et).apply {
                        setText(header.text)
                        setSelection(header.text.length)
                    }

                    // Save Button
                    findViewById<Button>(R.id.save_button).apply {
                        setOnClickListener {
                            val input = editField.text.toString()

                            if (TextUtils.isEmpty(input)) {
                                Toast.makeText(
                                    context,
                                    R.string.fill_all_fields_message,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                header.text = input
                                informationData[adapterPosition].header = input
                                notifyChanges()
                                dismiss()
                            }
                        }

                        show()
                    }
                }
            }

            episodeViewGroup.setOnClickListener {
                Dialog(context).apply {
                    setContentView(R.layout.main_edit_dialog)

                    // Header of Edit Field Dialog
                    findViewById<TextView>(R.id.edit_header).apply {
                        text = context.resources.getString(R.string.episode)
                    }

                    // Edit Text for user input
                    val editField = findViewById<EditText>(R.id.edit_field_et).apply {
                        setText(episode.text)
                        setSelection(episode.text.length)
                    }

                    // Save Button
                    findViewById<Button>(R.id.save_button).apply {
                        setOnClickListener {
                            val input = editField.text.toString()

                            if (TextUtils.isEmpty(input)) {
                                Toast.makeText(
                                    context,
                                    R.string.fill_all_fields_message,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                episode.text = input
                                informationData[adapterPosition].episode = input
                                notifyChanges()
                                dismiss()
                            }
                        }

                        show()
                    }
                }
            }

            decreaseBtn.setOnClickListener(ArrowButtonsClickListener(
                ArrowButtons.DECREASE,
                this
            ))

            increaseBtn.setOnClickListener(ArrowButtonsClickListener(
                ArrowButtons.INCREASE,
                this
            ))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val layoutToInflate = R.layout.main_item
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(layoutToInflate, parent, false)

        return MainViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(informationData[position])
    }

    override fun getItemCount() = informationData.size

    fun addItem(header: String, episode: String) {
        informationData.add(
            MainInformation(
            header = header,
            episode = episode
        ))

        notifyItemInserted(informationData.size - 1)

        notifyChanges()
    }

    fun itemMoved(from: Int, to: Int) {
        Collections.swap(informationData, from, to)
        notifyItemMoved(from, to)

        notifyChanges()
    }

    fun itemSwiped(position: Int) {
        mRecentlyDeletedItem = informationData[position]
        mRecentlyDeletedItemPosition = position

        informationData.removeAt(position)
        notifyItemRemoved(position)

        notifyChanges()
    }

    fun undoDelete() {
        mRecentlyDeletedItem?.let { recentlyDeletedItem ->
            informationData.add(
                mRecentlyDeletedItemPosition,
                recentlyDeletedItem
            )

            notifyItemInserted(mRecentlyDeletedItemPosition)

            notifyChanges()

            if (mRecentlyDeletedItemPosition == 0 ||
                mRecentlyDeletedItemPosition == informationData.size - 1)
            {
                scrollToPosition(mRecentlyDeletedItemPosition)
            }
        }
    }

    inner class ArrowButtonsClickListener(
        private val whichButton: ArrowButtons,
        private val viewHolder: MainViewHolder
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            val currentInt = findFirstIntInString(viewHolder.episode.text) ?: return

            if (whichButton == ArrowButtons.DECREASE && currentInt <= 0)
                return

            val targetInt =
                if (whichButton == ArrowButtons.DECREASE) currentInt - 1
                else currentInt + 1

            val newText = replaceIntOnString(
                viewHolder.episode.text.toString(),
                currentInt,
                targetInt
            )

            viewHolder.episode.text = newText
            informationData[viewHolder.adapterPosition].episode = newText

            notifyChanges()
        }
    }

    fun findFirstIntInString(cs: CharSequence): Int? {
        var text = ""

        for (i in cs.indices) {
            if (cs[i].digitToIntOrNull() != null) {
                text += cs[i]

                if (i != cs.length - 1 && cs[i + 1].digitToIntOrNull() == null)
                    break
            }
        }

        return text.toIntOrNull()
    }

    fun replaceIntOnString(text: String, current: Int, target: Int): String =
        text.replaceFirst(current.toString(), target.toString())
}