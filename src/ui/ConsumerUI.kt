package ui

import controller.AdminController
import controller.ConsumerController
import model.Purchase
import model.enum.IndianCurrency
import exception.VendingMachineException
import java.math.BigDecimal
import java.util.EnumMap
import kotlin.collections.set
import kotlin.plus

//The purpose of AdminUI is to get input from the admin and display the fetched data and teh status of the request in a human friendly manner, and it does this by using class that implements the model.enum(Interface Interactable).
class ConsumerUI() : Interactable {

    //Why? For looping the options till exit request
    fun show() {
        var running = true
        while (running) {
            println("\n=====================================")
            println("  CUSTOMER MENU")
            println("=====================================")
            println("  1. Buy products")
            println("  0. Exit")
            println("=====================================")

            try {
                when (prompt("Please enter your choice ")) {
                    "1"  -> buyProducts()
                    "0"  -> running = false
                    else -> println("  Invalid choice, please enter a choice from 0–3.")
                }
            } catch (e: VendingMachineException) {
                println("\n  [!SYSTEM ERROR!] ${e.message}")
            } catch (e: IllegalArgumentException) {
                println("[Input Error] ${e.message}")
            }
        }
    }

    //Why? UI and presentation, along with forward calls, along with forward calls
    private fun viewAllMachines() {
        val machines = ConsumerController.viewAllVendingMachines()
        println("\n=====================================")
        println("  VENDING MACHINES")
        println("=====================================")
        if (machines.isEmpty()) { println("  No machines available yet."); return }
        for (vm in machines) {
            println("  ID       : ${vm.vendingMachineId}")
            println("  Location : ${vm.vendingMachineLocation}")
            println("  Since    : ${vm.establishedOn}")
            println("  Slots    : ${vm.getAllSlotsInVendingMachine().size}")
            println("─────────────────────────────────────")
        }
    }

    //Why? UI and presentation, along with forward calls, along with forward calls
    private fun printAvailableProducts(vmId: String) {//todo unique properties
        val products = ConsumerController.viewAvailableProducts(vmId)
        val category = AdminController.getCategoryByVendingMachineId(vmId)
        println("\n=====================================")
        println("  AVAILABLE $category PRODUCTS")
        println("=====================================")
        if (products.isEmpty()) { println("  No products in stock at this machine."); return }
        println("  %-14s %-22s %8s  %6s".format("Product ID", "Name", "Price", "Stock"))
        println("─────────────────────────────────────")
        for (product in products) {
            val qty = ConsumerController.getAvailableStock(vmId, product.productId)
            println("  %-14s %-22s Rs.%-5s  %6d".format(
                product.productId, product.productName, product.price, qty
            ))
        }
    }

    //Why? To have a proper flow for purchasing a product, along with UI and presentation, along with forward calls
    private fun buyProducts() {
        viewAllMachines()
        val vmId = prompt("Vending machine ID : ")

        printAvailableProducts(vmId)

        val cart = buildCart(vmId)
        if (cart.isEmpty()) { println("\n  Nothing added to cart. Returning to menu."); return }

        val total = ConsumerController.getCartTotal(cart)
        println("\n  Cart total : Rs.$total")

        val payment = collectPayment(total)
        if (payment.isEmpty()) { println("  Purchase cancelled — no payment received."); return }

        val purchase = ConsumerController.buyProducts(vmId, cart, payment)
        printReceipt(purchase)
    }

    //Why? Soft replication of a real-world cart
    private fun buildCart(vmId: String): Map<String, Int> {
        val cart = mutableMapOf<String, Int>()
        println("\n  Add items to cart (leave Product ID blank when done):")
        while (true) {
            val productId = prompt("    Product ID : ")
            if (productId.isBlank()) break

            val available = try {
                ConsumerController.getAvailableStock(vmId, productId)
            } catch (e: VendingMachineException) {
                println("    [!] ${e.message}"); continue
            }

            if (available == 0) { println("    [!] That item is out of stock."); continue }

            val qty = readInt("    Quantity (available: $available)")
            if (qty > available) { println("    [!] Only $available units available."); continue }

            cart[productId] = (cart[productId] ?: 0) + qty
            println("    Added $qty × $productId")
        }
        return cart
    }

    //Why? For one at a time money exchange. Attempting to replicate the vending machine style of coin by coin entry.
    private fun collectPayment(totalRequired: BigDecimal): Map<IndianCurrency, Int> {
        val payment = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)
        var paid = BigDecimal.ZERO

        println("\n  Accepted denominations:")
        IndianCurrency.entries.forEach { print("  ${it.name}(Rs.${it.value})") }
        println()
        println("  Type a denomination name to insert it, or DONE to cancel.\n")

        while (paid < totalRequired) {
            val remaining = totalRequired - paid
            println("  Paid: Rs.%-8s  Still needed: Rs.%s".format(paid, remaining))
            val input = prompt("  Insert: ").uppercase()

            if (input == "DONE") {
                println("  Payment cancelled.")
                return EnumMap(IndianCurrency::class.java)
            }

            try {
                val coin = IndianCurrency.valueOf(input)
                payment[coin] = (payment[coin] ?: 0) + 1
                paid += BigDecimal.valueOf(coin.value.toLong())
                println("  Accepted Rs.${coin.value}  |  Total inserted so far: Rs.$paid")
            } catch (_: IllegalArgumentException) {
                println("  [!] Unknown denomination. Try again.")
            }
        }

        return payment
    }

    //Why? UI and presentation, along with forward calls
    private fun printReceipt(purchase: Purchase) {
        println("\n=====================================")
        println("              RECEIPT")
        println("=====================================")
        println("  Purchase ID : ${purchase.purchaseId}")
        println("  Time        : ${purchase.purchaseTime}")
        println("  Items       : ${purchase.getQuantityOfProductsPurchased()}}")//todo, custom print, no {}
        println("  Total       : Rs.${purchase.totalAmount}")
        println("  Paid        : Rs.${purchase.moneyPaidByCustomer}")
        println("  Change      : Rs.${purchase.moneyToBeReturnedByVendingMachine}")
        println("=====================================")
        if (purchase.moneyToBeReturnedByVendingMachine > BigDecimal.ZERO) {
            println("  Please collect your change: Rs.${purchase.moneyToBeReturnedByVendingMachine}")
        }
        println("  Thank you for your purchase!")
        println("=====================================\n")
    }
}