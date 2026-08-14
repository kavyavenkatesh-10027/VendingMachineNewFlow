package repository

import model.Product

object ProductRepository : BaseRepository<Product>() {

    override fun getId(entity: Product) = entity.productId

    //Why? To fetch the products, especially important because vending machine has only one category of products
    fun getCategory(productId: String) = findById(productId).productCategory

}