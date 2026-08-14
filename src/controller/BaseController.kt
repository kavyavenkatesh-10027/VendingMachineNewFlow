package controller

import model.*
import service.*

//The purpose of BaseController is to hold the common functionalities in AdminController and ConsumerController, and it does this by abstract class.
abstract class BaseController {

    /***
     *All the methods in this class -
    Why? For input validation and clean flow, for the safety of not letting the UI layer access Service layer.
     ***/
    fun viewVendingMachine(vendingMachineId: String): VendingMachine {
        require(vendingMachineId.isNotBlank()) {"Vending machine ID cannot be empty."}
        return VendingMachineService.getVendingMachineById(vendingMachineId)
    }

    fun viewAllVendingMachines(): Set<VendingMachine> = VendingMachineService.getAllVendingMachines()

    fun viewAvailableProducts(vendingMachineId: String): Set<Product> {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }
        return VendingMachineService.viewAvailableProducts(vendingMachineId)
    }

    fun viewAvailableQuantityForAllProducts(vendingMachineId: String): Map<String, Int> {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }
        return VendingMachineService.viewAvailableQuantityForAllProducts(vendingMachineId)
    }

    fun getAvailableQuantityForOneProduct(
        vendingMachineId: String,
        productId: String
    ): Int {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }
        require(productId.isNotBlank()) { "Product ID cannot be empty." }

        return VendingMachineService.getAvailableQuantityForOneProduct(
            vendingMachineId,
            productId
        )
    }
}
//todo - cancel 4,5- done