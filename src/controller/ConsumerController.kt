package controller

import model.Purchase
import service.PurchaseService
import service.VendingMachineService
import model.enum.IndianCurrency
import java.math.BigDecimal

//The purpose of ConsumerController is to direct requests from a consumer, interacting through the ConsumerUI, to the correct service and its methods , and it does this by class and inheriting abstract class BaseController.
object ConsumerController : BaseController() {

    /***
     *All the methods in ConsumerController class -
    Why? For input validation and clean flow, for the safety of not letting the UI layer access Service layer.
     ***/
    fun buyProducts(
        vendingMachineId: String,
        cart: Map<String, Int>,
        inserted: Map<IndianCurrency, Int>
    ): Purchase {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be null or empty." }
        require(cart.isNotEmpty()) { "Cart is empty. Please select at least one product." }
        require(inserted.isNotEmpty()) { "No money inserted. Please insert payment." }

        val vm = VendingMachineService.getVendingMachineById(vendingMachineId)
        return PurchaseService.processPurchase(vm, cart, inserted)
    }

    fun getCartTotal(cart: Map<String, Int>): BigDecimal {
        require(cart.isNotEmpty()) { "Cart is empty." }
        return PurchaseService.getCartTotal(cart)
    }

    fun getAvailableStock(vendingMachineId: String, productId: String): Int =
        getAvailableQuantityForOneProduct(vendingMachineId, productId)
}