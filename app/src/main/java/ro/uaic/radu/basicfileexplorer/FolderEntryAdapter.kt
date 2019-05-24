package ro.uaic.radu.basicfileexplorer

import android.content.Context
import android.content.Intent
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*

class FolderEntryAdapter(data: List<FolderEntry>, activity: MainActivity) :
    ArrayAdapter<FolderEntry>(activity, R.layout.sample_folder_entry_view, data), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    val tag = "FOLDER_ENTRY_ADAPTER"
    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        val popup = PopupMenu(parentActivity, view)
        popup.menuInflater.inflate(R.menu.entry_popup, popup.menu)
        popup.setOnMenuItemClickListener {
                item: MenuItem? ->
            if (item != null) {
                var updateList = false
                when(item.itemId){
                    R.id.delete_action -> {
                        updateList = parentActivity.deleteEntry(dataSet[position])
                    }
                    R.id.rename_action -> {
                        parentActivity.renameEntry(dataSet[position])
                    }
                    R.id.move_action -> parentActivity.moveEntry(dataSet[position])
                    R.id.copy_action -> parentActivity.copyEntry(dataSet[position])
                }
                if(updateList){
                    notifyDataSetChanged()
                }
            }
            true
        }
        popup.show()
        return true
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(dataSet[position].isDirectory()){
            val directoryChanged = parentActivity.cd(dataSet[position])
            if(directoryChanged)
                notifyDataSetChanged()
        } else if(dataSet[position].isFile()){
            parentActivity.editFile(dataSet[position])
        }
    }

    private val dataSet = data

    private val parentActivity: MainActivity = activity

    private class ViewHolder(view: View){
        val txtName = view.findViewById<TextView>(R.id.entry_name)
        val iconView = view.findViewById<ImageView>(R.id.entry_image)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val entry = dataSet[position]
        val freshView: View
        val viewHolder: ViewHolder
        if(convertView == null){
            val inflater = LayoutInflater.from(context)
            freshView = inflater.inflate(R.layout.sample_folder_entry_view, parent, false)
            viewHolder = ViewHolder(freshView)
            freshView.tag = viewHolder
        } else {
            freshView = convertView
            viewHolder = freshView.tag as ViewHolder
        }

        if (position != 0  || NavigatorManager.isOnRoot){
            viewHolder.txtName.text = entry.name
            if (entry.isDirectory()){
                viewHolder.iconView.setImageDrawable(freshView.resources.getDrawable(R.drawable.folder_icon))
            } else {
                viewHolder.iconView.setImageDrawable(freshView.resources.getDrawable(R.drawable.file_icon))
            }

        } else {
            viewHolder.txtName.text = "..."
            viewHolder.iconView.setImageDrawable(freshView.resources.getDrawable(R.drawable.parent_folder_foreground))
        }




        return freshView


    }

}