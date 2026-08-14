package service

import model.Product
import repository.ProductRepository
import java.math.BigDecimal

abstract class BaseProductService<T : Product> {

    abstract fun getById(productId : String) : T

    abstract fun getAllProductsInThisCategory(): Set<T>

    companion object{//similar to static in java. When the method belongs to the class instead of the instance
        fun getProductById(productId: String) : Product{
            return ProductRepository.findById(productId)
        }
    }
    
    //Why? For completing the chain
    fun editDescription(productId: String, newDescription: String) {
        getProductById(productId).description = newDescription
    }

    //Why? For completing the chain
    fun editName(productId: String, newName: String) {
        getProductById(productId).productName = newName
    }

    //Why? For completing the chain
    fun editBrand(productId: String, newBrand: String) {
        getProductById(productId).brand = newBrand
    }

    //Why? For completing the chain
    fun editPrice(productId: String, newPrice: BigDecimal) {
        getProductById(productId).price = newPrice
    }

    //Why? For completing the chain
    fun editWarning(productId: String, newWarning: String?) {
        getProductById(productId).warning = newWarning
    }
}