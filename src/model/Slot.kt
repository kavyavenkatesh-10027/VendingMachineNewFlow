package model

import exception.AvailabilityRequirementException
import exception.UnregisteredEntityException
import generator.IDGenerator

//The purpose of Slot is to represent individual racks of a vending machine and do the necessary actions, and it does this by class.
class Slot(
    val vendingMachineId: String,
    productItemsInSlot: MutableMap<String, Int>
) {
    val slotId = IDGenerator.generateSlotId()

    // Defensive copy so external mutation of the caller's map can't corrupt slot state.
    private val productItemsInSlot: MutableMap<String, Int> = productItemsInSlot.toMutableMap()

    //Why? For encapsulating and restricting modification of the collection
    fun getProductItemsInSlot(): Map<String, Int> {
        return productItemsInSlot.toMap()
    }

    init {
        require(!vendingMachineId.isBlank()) {"Vending machine cannot be blank"}
        require(this.productItemsInSlot.isNotEmpty()) {"Slot must have at least one product item"}
        //Runs along with primary const
    }

    //Why? For validating and to safely add a new product type to the Slot
    fun addNewProductTypeToSlot(productId: String, quantity: Int) {
        require(productId.isNotBlank()) {"Product ID must not be left blank"}
        require(quantity > 0) {"Quantity must be greater than zero" }
        productItemsInSlot[productId] = quantity
    }

    //Why? To validate before refilling
    fun addMoreOfProductItemToSlot(productId: String, quantity: Int) {
        require(quantity > 0) {"Quantity must be greater than zero"}
        val current = productItemsInSlot[productId]
            ?: throw UnregisteredEntityException("Product", productId,  "Slot", slotId, "User 'Add New Product Type' instead")
        productItemsInSlot[productId] = current + quantity
    }

    //Why? To validate before removing a product item from slot
    fun removeProductItemFromSlot(productId: String, quantity: Int) {
        require(quantity > 0) {"Quantity must be greater than zero"}
        val current = productItemsInSlot[productId]
            ?: throw UnregisteredEntityException("product", productId,  "Slot", slotId)
        if (quantity > current) {
            throw AvailabilityRequirementException("Cannot remove $quantity of product $productId; only $current present in slot $slotId")
        }
        productItemsInSlot[productId] = current - quantity
    }

//    //Why? To validate the product type before collectively removing product items
//    fun removeProductFromSlot(productId: String) {
//        if (!productItemsInSlot.contains(productId)) throw UnregisteredEntityException("Product", productId,  "Slot", slotId)
//        productItemsInSlot.remove(productId)
//    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Slot) return false
        return slotId == other.slotId
    }

    override fun hashCode(): Int = slotId.hashCode()

    override fun toString(): String =
        """
    Slot ID                : $slotId
    Vending Machine ID     : $vendingMachineId
    Product Items:
    ${
            productItemsInSlot.entries.joinToString("\n") {
                "  ${it.key} -> ${it.value}"
            }
        }
    """.trimIndent()
}