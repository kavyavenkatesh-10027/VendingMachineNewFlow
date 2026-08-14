package repository

import model.Purchase

//The purpose of PurchaseRepository is to return purchaseId, and it does this by object(Singleton classes in Java) and inherits the BaseRepository for purchase data handling.
object PurchaseRepository : BaseRepository<Purchase>() {
    //Why? To avoid duplication
    override fun getId(entity: Purchase) = entity.purchaseId
}