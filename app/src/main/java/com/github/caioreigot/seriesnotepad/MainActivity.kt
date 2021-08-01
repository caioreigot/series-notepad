package com.github.caioreigot.seriesnotepad

import android.app.Dialog
import android.graphics.Canvas
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.caioreigot.seriesnotepad.model.MainInformation
import com.github.caioreigot.seriesnotepad.model.SharedPrefs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var addItemFab: FloatingActionButton
    lateinit var mainRecyclerView: RecyclerView
    lateinit var sharedPrefs: SharedPrefs

    var mainAdapter: MainAdapter? = null
    var savedList: MutableList<MainInformation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addItemFab = findViewById(R.id.add_item_fab)
        mainRecyclerView = findViewById(R.id.main_recycler_view)

        sharedPrefs = SharedPrefs(this)

        savedList =
            if (sharedPrefs.getList()?.isNotEmpty() == true)
                sharedPrefs.getList() as MutableList<MainInformation>
            else null

        addItemFab.setOnClickListener {

            Dialog(this).apply {
                setContentView(R.layout.main_add_dialog)

                val addButton: Button = findViewById(R.id.add_button)

                addButton.setOnClickListener {
                    val nameET = findViewById<EditText>(R.id.name_edit_text)
                    val episodeET = findViewById<EditText>(R.id.episode_edit_text)

                    if (TextUtils.isEmpty(nameET.text.toString()) ||
                        TextUtils.isEmpty(episodeET.text.toString()))
                    {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.fill_all_fields_message,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        mainAdapter?.addItem(nameET.text.toString(), episodeET.text.toString())
                        dismiss()
                    }
                }

                show()
            }
        }

        mainAdapter = MainAdapter(
            savedList ?: mutableListOf(),
            ::scrollRecyclerViewTo,
            ::notifyListChanged
        )

        mainRecyclerView.adapter = mainAdapter
        mainRecyclerView.layoutManager = LinearLayoutManager(this)

        val helper = ItemTouchHelper(
            CustomItemTouchHelper(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )
        )

        helper.attachToRecyclerView(mainRecyclerView)
    }

    private fun notifyListChanged() {
        mainAdapter?.let { adapter ->
            sharedPrefs.setList(SharedPrefs.STORE_FILE_NAME_KEY, adapter.informationData)
        }
    }

    inner class CustomItemTouchHelper(
        dragDirs: Int, swipeDirs: Int
    ) : ItemTouchHelper.SimpleCallback(
        dragDirs, swipeDirs
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean
        {
            val from: Int = viewHolder.adapterPosition
            val to: Int = target.adapterPosition

            mainAdapter?.itemMoved(from, to)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            mainAdapter?.let { adapter ->
                val position = viewHolder.adapterPosition

                adapter.itemSwiped(position)
                showUndoSnackbar()
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean)
        {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE ||
                actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                if (isCurrentlyActive) {
                    val bottomRow = viewHolder.itemView.findViewById<View>(R.id.bottom_row)
                    bottomRow.alpha = 0f
                } else {
                    val bottomRow = viewHolder.itemView.findViewById<View>(R.id.bottom_row)

                    bottomRow.animate().apply {
                        interpolator = LinearInterpolator()
                        duration = 100
                        alpha(1f)
                        start()
                    }
                }
            }
        }
    }

    private fun scrollRecyclerViewTo(position: Int) =
        mainRecyclerView.scrollToPosition(position)

    private fun showUndoSnackbar() {
        val view: View = findViewById(R.id.root_view)

        Snackbar.make(
            view, R.string.snack_bar_deleted_text,
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.snack_bar_undo) { mainAdapter?.undoDelete() }
            setActionTextColor(resources.getColor(android.R.color.holo_red_light))
            show()
        }
    }
}