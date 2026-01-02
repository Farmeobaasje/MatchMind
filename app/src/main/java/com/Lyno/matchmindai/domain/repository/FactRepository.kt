package com.Lyno.matchmindai.domain.repository

import com.Lyno.matchmindai.domain.model.Fact
import com.Lyno.matchmindai.domain.model.FactCategory
import com.Lyno.matchmindai.domain.model.FactCollection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository for managing and retrieving facts for the loading screen.
 * Provides a stream of rotating facts with configurable timing.
 */
interface FactRepository {
    /**
     * Get a stream of rotating facts.
     * @param intervalMs Time between fact changes in milliseconds (default: 3500ms)
     * @param includeDynamic Whether to include facts with dynamic values
     */
    fun getFactStream(intervalMs: Long = 3500, includeDynamic: Boolean = true): Flow<Fact>
    
    /**
     * Get facts by specific category.
     */
    suspend fun getFactsByCategory(category: FactCategory): List<Fact>
    
    /**
     * Get a single random fact.
     */
    suspend fun getRandomFact(includeDynamic: Boolean = true): Fact
}

/**
 * Implementation of FactRepository using the static FactCollection.
 */
class FactRepositoryImpl @Inject constructor() : FactRepository {
    
    override fun getFactStream(intervalMs: Long, includeDynamic: Boolean): Flow<Fact> = flow {
        var previousFact: Fact? = null
        
        while (true) {
            val fact = if (includeDynamic && (0..3).random() == 0) {
                // 25% chance to get a dynamic fact
                FactCollection.getRandomDynamicFact()
            } else {
                // Get random fact, avoid repeating the same fact immediately
                var newFact = FactCollection.getRandomFact()
                while (newFact.id == previousFact?.id) {
                    newFact = FactCollection.getRandomFact()
                }
                newFact
            }
            
            previousFact = fact
            emit(fact)
            delay(intervalMs)
        }
    }
    
    override suspend fun getFactsByCategory(category: FactCategory): List<Fact> {
        return FactCollection.getFactsByCategory(category)
    }
    
    override suspend fun getRandomFact(includeDynamic: Boolean): Fact {
        return if (includeDynamic && (0..1).random() == 0) {
            FactCollection.getRandomDynamicFact()
        } else {
            FactCollection.getRandomFact()
        }
    }
}

/**
 * Factory for creating FactRepository instances.
 */
object FactRepositoryFactory {
    fun create(): FactRepository {
        return FactRepositoryImpl()
    }
}
