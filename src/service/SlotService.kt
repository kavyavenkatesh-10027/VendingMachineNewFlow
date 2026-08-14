package service

import exception.ExistsAlreadyException
import exception.MismatchingProductTypeAndVendingMachine
import exception.UnregisteredEntityException
import model.Slot
import model.enum.ProductCategory
import repository.ProductRepository

//The purpose of SlotService is to do Crud in slots , and it does this by object (Singleton class in Java).
object SlotService {

    //Why? To avoid duplication by fetching data from repository once
//    fun getSlotById(slotId: String): Slot = VendingMachineService.getSlotByIdInVendingMachine()

    //Why? For consistency and maintaining Controller->Service->Repository flow
//    fun getAllSlots() : Set<Slot> = SlotRepository.findAll()

    //Why? To validating and to safely add a new product type to the Slot
    fun addNewProductTypeToSlot(vendingMachineId: String, slotId: String, productId: String, quantity: Int, category: ProductCategory) {
        val slot = VendingMachineService.getSlotByIdInVendingMachine(vendingMachineId, slotId)
        val product = ProductRepository.findById(productId)
        if(product.productCategory != category){
            throw MismatchingProductTypeAndVendingMachine(category, product.productCategory)
        }
        if (slot.getProductItemsInSlot().containsKey(productId)) {
            throw ExistsAlreadyException("Product : $productId already exists. Use refillProductInSlot instead.")
        }
        slot.addNewProductTypeToSlot(productId, quantity)
    }

//    //Why? To validate the product type before collectively removing product items
//    fun removeProductTypeFromSlot(productId: String) {
//        SlotRepository.findAll().forEach { slot ->
//            if (slot.getProductItemsInSlot().containsKey(productId)) {
//                slot.removeProductFromSlot(productId)
//            }
//        }
//    }

    //Why? To ensure that the product actually exists in slot
    fun refillProductInSlot(vendingMachineId: String, slotId: String, productId: String, quantity: Int) {
        val slot = VendingMachineService.getSlotByIdInVendingMachine(slotId, vendingMachineId)
        if (!slot.getProductItemsInSlot().containsKey(productId)) {
            throw UnregisteredEntityException("Product", productId, "Slot", slotId,"Use 'Add New Product Type To Slot' instead.")
        }
        slot.addMoreOfProductItemToSlot(productId, quantity)//Validation in model class
    }

//    //Why? To maintain the flow and ensure removal from both the repos.
//    fun removeSlot(slotId: String) {
//        val slot = getSlotById(slotId)
//        VendingMachineRepository.findById(slot.vendingMachineId).removeSlotFromVendingMachine(slot)
//        SlotRepository.removeById(slotId)
//    }
}