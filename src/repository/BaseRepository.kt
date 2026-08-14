package repository

import exception.ExistsAlreadyException
import exception.UnknownEntityException

//The purpose of BaseRepository is to perform common data handling, and it does this by Abstact Generics.
abstract class BaseRepository<T : Any> {//Now T can be anything other than null

    protected val store = mutableMapOf<String, T>()

    //Why? Entity recognition through id is very important to perform any actions and must be fetched from the respective models
    protected abstract fun getId(entity: T): String

    //Why? To manage data redundancy
    open fun add(entity: T) {
        val id = getId(entity)
        if (existsById(id)) {
            throw ExistsAlreadyException("Entity already exists in the system")
        }
        store[id] = entity
    }

    //Why? To avoid duplication
    fun findById(id: String): T {
        return store[id] ?: throw UnknownEntityException(id, "Entity")
    }

    //Why? To avoid data redundancy
    fun findAll(): Set<T> = store.values.toSet()

    //Why? To remove after check
    open fun removeById(id: String) {
        if (!existsById(id)) {
            throw UnknownEntityException(id, "Entity")
        }
        store.remove(id)
    }

    //Why? To avoid duplication
    fun existsById(id: String): Boolean = store.containsKey(id)
}
