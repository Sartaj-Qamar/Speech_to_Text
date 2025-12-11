package com.codetech.speechtotext.Adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.DBViewModel
import com.codetech.speechtotext.data_source.AppDatabase
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.databinding.ItemTranslationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class TranslationAdapter(
    private val translationList: MutableList<Translation>,
    private val context: Context,
    private val isFavouriteFragment: Boolean = false,
    private val onEmptyList: (() -> Unit)? = null,
//    private val onFavoriteChanged: ((Translation) -> Unit)? = null
) : RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {

    private lateinit var textToSpeech: TextToSpeech
    private val database = AppDatabase.getDatabase(context)
    private val translationDao = database.translationDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.getDefault()
            }
        }
    }

    inner class TranslationViewHolder(private val binding: ItemTranslationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(translation: Translation) {
            binding.apply {
                sourceLangText.text = translation.sourceLang
                targetLangText.text = translation.targetLang
                inputText.text = translation.inputText
                resultText.text = translation.resultText

                updateFavoriteIcon(translation.isFavorite)

                deleteText.visibility = if (isFavouriteFragment) View.GONE else View.VISIBLE

                favoriteText.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val updatedTranslation = translation.copy(isFavorite = !translation.isFavorite)

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // Update in database
                                // translationDao.updateTranslation(position,true)
                                translationDao.updateTranslationNew(updatedTranslation.timestamp, updatedTranslation.isFavorite)
                                // DBViewModel(context).updateFavourite(translation)
                                // Update UI on main thread
                                withContext(Dispatchers.Main) {
                                    translationList[position] = updatedTranslation
                                    updateFavoriteIcon(updatedTranslation.isFavorite)

                                    if (isFavouriteFragment && !updatedTranslation.isFavorite) {
                                        translationList.removeAt(position)
                                        notifyItemRemoved(position)
                                        if (translationList.isEmpty()) {
                                            onEmptyList?.invoke()
                                        }
                                    } else {
                                        notifyItemChanged(position)
                                    }

                                    val message = if (updatedTranslation.isFavorite)
                                        "Added to favorites"
                                    else
                                        "Removed from favorites"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Error updating favorite status",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }

                volume.setOnClickListener { readTextAloud(translation.inputText) }
                volume2.setOnClickListener { readTextAloud(translation.resultText) }

                deleteText.setOnClickListener {
                    textToSpeech.stop()
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        coroutineScope.launch {
                            translationDao.deleteTranslation(translation)
                        }
                        translationList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, translationList.size)
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()

                        if (translationList.isEmpty()) {
                            onEmptyList?.invoke()
                        }
                    }
                }

                copyText.setOnClickListener {
                    copyToClipboard(translation.resultText)
                }
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.favoriteText.setImageResource(
                if (isFavorite) R.drawable.fav_icon
                else R.drawable.ic_unfav
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val binding = ItemTranslationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TranslationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        holder.bind(translationList[position])
    }

    override fun getItemCount(): Int = translationList.size

    fun addTranslation(translation: Translation) {
        coroutineScope.launch {
            translationDao.insertTranslation(translation)
        }
        translationList.add(translation)
        notifyItemInserted(translationList.size - 1)
    }

    fun clearTranslations() {
        coroutineScope.launch {
            translationDao.deleteAllTranslations()
        }
        translationList.clear()
        notifyDataSetChanged()
    }

    fun updateTranslations(newTranslations: List<Translation>) {
        translationList.clear()
        translationList.addAll(newTranslations)
        notifyDataSetChanged()
    }

    private fun readTextAloud(text: String) {
        if (text.isNotEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun copyToClipboard(text: String) {
        if (text.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Translate Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No text to copy", Toast.LENGTH_SHORT).show()
        }
    }
}
