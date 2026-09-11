package com.mulheres

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FolderAdapter(
    private val onClick: (StorageItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.VH>() {

    private var list: List<StorageItem> = emptyList()

    sealed class StorageItem {

        data class Local(
            val file: File
        ) : StorageItem()

        data class Document(
            val document: DocumentFile
        ) : StorageItem()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        val icon: ImageView =
            view.findViewById(R.id.fileIcon)

        val name: TextView =
            view.findViewById(R.id.folderName)

        val date: TextView =
            view.findViewById(R.id.folderDate)

        val count: TextView =
            view.findViewById(R.id.folderCount)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_folder,
                    parent,
                    false
                )

        return VH(view)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {

        val item =
            list[position]

        val fonte =
            try {
                Typeface.createFromAsset(
                    holder.itemView.context.assets,
                    "font.ttf"
                )
            } catch (e: Exception) {
                Typeface.DEFAULT
            }

        holder.name.typeface = fonte
        holder.date.typeface = fonte
        holder.count.typeface = fonte

        when (item) {

            is StorageItem.Local -> {

                val file =
                    item.file

                holder.name.text =
                    file.name.ifEmpty {
                        "Sem nome"
                    }

                if (file.lastModified() > 0L) {

                    holder.date.text =
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(
                            Date(
                                file.lastModified()
                            )
                        )

                } else {

                    holder.date.text =
                        "Não há informações de data"
                }

                if (file.isDirectory) {

                    holder.icon.setImageResource(
                        R.drawable.ic_folder
                    )

                    val quantidade =
                        try {
                            file.listFiles()
                                ?.size ?: 0
                        } catch (e: Exception) {
                            0
                        }

                    holder.count.text =
                        when (quantidade) {
                            1 -> "1 item"
                            else -> "$quantidade itens"
                        }

                } else {

                    holder.icon.setImageResource(
                        R.drawable.ic_document
                    )

                    holder.count.text =
                        "Arquivo"
                }
            }

            is StorageItem.Document -> {

                val document =
                    item.document

                holder.name.text =
                    document.name
                        ?.ifEmpty {
                            "Sem nome"
                        }
                        ?: "Sem nome"

                val data =
                    document.lastModified()

                if (data > 0L) {

                    holder.date.text =
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(
                            Date(data)
                        )

                } else {

                    holder.date.text =
                        "Não há informações de data"
                }

                if (document.isDirectory) {

                    holder.icon.setImageResource(
                        R.drawable.ic_folder
                    )

                    val quantidade =
                        try {
                            document.listFiles().size
                        } catch (e: Exception) {
                            0
                        }

                    holder.count.text =
                        when (quantidade) {
                            1 -> "1 item"
                            else -> "$quantidade itens"
                        }

                } else {

                    holder.icon.setImageResource(
                        R.drawable.ic_document
                    )

                    holder.count.text =
                        "Arquivo"
                }
            }
        }

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun update(
        newList: List<StorageItem>
    ) {

        list = newList

        notifyDataSetChanged()
    }
}