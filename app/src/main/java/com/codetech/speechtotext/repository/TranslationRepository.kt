import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.data_source.TranslationDao

class TranslationRepository(private val translationDao: TranslationDao) {
    suspend fun getAllTranslations(): List<Translation> = translationDao.getAllTranslations()

    suspend fun getFavorites(): List<Translation> = translationDao.getFavorites()

    suspend fun insertTranslation(translation: Translation) = translationDao.insertTranslation(translation)

    suspend fun updateTranslation(translation: Translation) = translationDao.updateTranslation(translation)

    suspend fun deleteTranslation(translation: Translation) = translationDao.deleteTranslation(translation)

    suspend fun clearAllTranslations() = translationDao.deleteAllTranslations()
} 