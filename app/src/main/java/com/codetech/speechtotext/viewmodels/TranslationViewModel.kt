import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codetech.speechtotext.data_source.Translation
import kotlinx.coroutines.launch

class TranslationViewModel(private val repository: TranslationRepository) : ViewModel() {
    private val _translations = MutableLiveData<List<Translation>>()
    val translations: LiveData<List<Translation>> = _translations

    private val _favorites = MutableLiveData<List<Translation>>()
    val favorites: LiveData<List<Translation>> = _favorites

    fun loadTranslations() {
        viewModelScope.launch {
            _translations.value = repository.getAllTranslations()
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = repository.getFavorites()
        }
    }

    fun toggleFavorite(translation: Translation) {
        viewModelScope.launch {
            val updatedTranslation = translation.copy(isFavorite = !translation.isFavorite)
            repository.updateTranslation(updatedTranslation)

//            // Refresh the lists to maintain consistency
//            loadTranslations()
//            loadFavorites()
        }
    }

    fun deleteTranslation(translation: Translation) {
        viewModelScope.launch {
            repository.deleteTranslation(translation)
//            loadTranslations()
//            loadFavorites()
        }
    }

    fun addTranslation(translation: Translation) {
        viewModelScope.launch {
            repository.insertTranslation(translation)
//            loadTranslations()
        }
    }
} 