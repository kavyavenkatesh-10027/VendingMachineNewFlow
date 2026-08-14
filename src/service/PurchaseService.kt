package service

import exception.InsufficientPaymentException
import exception.AvailabilityRequirementException
import exception.VendingMachineException
import model.Purchase
import model.VendingMachine
import repository.PurchaseRepository
import model.enum.*
import repository.ProductRepository
import java.math.BigDecimal

//The purpose of PurchaseService is to handle purchases, and it does this by object (Singleton class in Java).
object PurchaseService {

    //Why? For preventing invalid purchases and managing stock data on a clear purchase.
    // For handling demand-supply problem. Insufficient payment, change making, and scarcity of denomination leads to refund .
    // Updating product stock on purchase.
    fun processPurchase(
        vm: VendingMachine,
        cart: Map<String, Int>,
        inserted: Map<IndianCurrency, Int>
    ): Purchase {
        // Validate cart items and stock
        for ((productId, requestedQty) in cart) {
            require(productId.isNotBlank()) { "Product ID in cart cannot be empty."}
            require(requestedQty > 0) {"Quantity for product $productId must be greater than zero."}
            val product = ProductRepository.findById(productId)
            val stock = getStockInMachine(vm, productId)
            if (stock < requestedQty) {
                throw AvailabilityRequirementException("Insufficient stock for '${product.productName}'. Available: $stock")
            }
        }

        val total = getCartTotal(cart)
        val amountPaid = CurrencyService.acceptPayment(vm.drawer, inserted)

        if (amountPaid < total) {
            CurrencyService.refund(vm.drawer, inserted)
            throw InsufficientPaymentException( total, amountPaid )
        }

        val changeAmount = amountPaid - total
        try {
            CurrencyService.makeChange(vm.drawer, changeAmount)
        } catch (e: VendingMachineException) {
            CurrencyService.refund(vm.drawer, inserted)
            throw e
        }

        deductStockFromSlots(vm, cart)

        val purchase = Purchase(cart, total, amountPaid, changeAmount)
        PurchaseRepository.add(purchase)
        return purchase
    }

    //Why? To avoid duplication send cartTotal to the initial level (ie Controller, UI layers)
    fun getCartTotal(cart: Map<String, Int>): BigDecimal {
        var total = BigDecimal.ZERO

        for ((productId, quantity) in cart) {
            val product = ProductRepository.findById(productId)
            total += product.price * BigDecimal.valueOf(quantity.toLong())
        }

        return total
    }

    //Why? To get a consolidated value per product, instead of slot-wise
    fun getStockInMachine(vm: VendingMachine, productId: String): Int =
        vm.getAllSlotsInVendingMachine().sumOf { it.getProductItemsInSlot()[productId] ?: 0 }

    //Why? For collecting product items scattered across various slots
    private fun deductStockFromSlots(vm: VendingMachine, cart: Map<String, Int>) {
        for ((productId, requestedQty) in cart) {
            var remaining = requestedQty
            for (slot in vm.getAllSlotsInVendingMachine()) {
                if (remaining <= 0) break
                val inSlot = slot.getProductItemsInSlot()[productId] ?: 0
                if (inSlot > 0) {
                    val deduct = minOf(inSlot, remaining)
                    slot.removeProductItemFromSlot(productId, deduct)
                    remaining -= deduct
                }
            }
        }
    }

    //Why? For consistency and maintaining Controller->Service->Repository flow
    fun getAllPurchases(): Set<Purchase> = PurchaseRepository.findAll()
}