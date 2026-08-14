package repository

import model.Electronics

//The purpose of ElectronicsRepository is to return electronicsId, and it does this by object(Singleton classes in Java) and inherits the BaseRepository for electronics data handling.
object ElectronicsRepository : BaseRepository<Electronics>() {
    //Why? To avoid duplication
    override fun getId(entity: Electronics) = entity.productId
}
