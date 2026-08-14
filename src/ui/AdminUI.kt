package ui

import controller.AdminController
import exception.EmptyMenuException
import exception.VendingMachineException
import model.Product
import model.VendingMachine
import model.enum.*
import java.util.EnumMap
import kotlin.collections.sortedBy

//The purpose of AdminUI is to get input from the admin and display the fetched data and status of the system in a human friendly manner, and it does this by using class that implements the model.enum(Interface Interactable).
class AdminUI() : Interactable {

    //Why? For looping the options till exit request
    fun show() {
        var running = true

        while (running) {
            println("\n========== ADMIN MENU ==========")
            println("1. Create vending machine")
            println("2. View vending machine")
            println("3. Remove vending machine")
            println("4. View all products")
            println("5. Add product to slot")
            println("6. View cash drawer")
            println("7. Add cash to drawer")
            println("8. View purchase history")
            println("0. Exit")
            println("=================================")

            try {
                when (prompt("Please enter your choice : ")) {
                    "1" -> createVendingMachine()
                    "2" -> viewVendingMachine()
                    "3" -> removeVendingMachine()
                    "4" -> viewAllProducts()
                    "5" -> addProductToSlot()
                    "6" -> viewCashDrawer()
                    "7" -> addCashToDrawer()
                    "8" -> viewPurchaseHistory()
                    "0" -> running = false
                    else -> println("Invalid choice. Please try again.")
                }
            } catch (e: VendingMachineException) {
                println("[Error] ${e.message}")
            } catch (e: IllegalArgumentException) {
                println("[Input Error] ${e.message}")
            }
        }
    }

    //Why? Vending machine creation flow, UI and presentation, along with forward calls
    private fun createVendingMachine() {
        println("\n--- Create Vending Machine ---")
        val allowedProductCategory = readEnum(ProductCategory::class.java, "Product Type")
        val location = readEnum(Location::class.java, "Location")
        val establishedOn = readDate("Established on (yyyy-MM-dd): ")
        displayProductMenuCategoryWise(allowedProductCategory)
        val firstSlotProductItems = readProductItemsMap(allowedProductCategory, "first slot")
        val vm = AdminController.createVendingMachine(location, establishedOn, firstSlotProductItems, allowedProductCategory)
        addCashToDrawer(vm.vendingMachineId)
        println("\nVending machine created successfully!")
        println(vm)
    }

    // ===================== 2. View Vending Machine (ALL or ONE) =====================

    //Why? UI and presentation, along with forward calls. Branches into "view all" or "view one" per the new flow.
    private fun viewVendingMachine() {
        println("\n--- View Vending Machine ---")
        println("  1. All")
        println("  2. One")
        when (prompt("Choose (1-2): ")) {
            "1" -> viewAllVendingMachines()
            "2" -> viewOneVendingMachine()
            else -> println("Invalid choice.")
        }
    }

    //Why? UI and presentation.
    private fun viewAllVendingMachines() {
        val machines = AdminController.viewAllVendingMachines()
        if (machines.isEmpty()) { println("No vending machines registered yet."); return }
        println("\n===== All Vending Machines =====")
        machines.sortedBy { it.vendingMachineId.substringAfterLast("-").toIntOrNull() ?: 0 }.forEach { println("$it\n--------------------------------") }
    }

    //Why? UI and presentation, along with forward calls.
    private fun viewOneVendingMachine() {
        displayVendingMachineMenu()
        val vmId = prompt("Vending machine ID to view: ")
        val vm = AdminController.viewVendingMachine(vmId)
        println("\n$vm")
    }

    //Why? UI and presentation, along with forward calls.
    private fun removeVendingMachine() {
        println("\n--- Remove Vending Machine ---")
        displayVendingMachineMenu()
        val vmId = prompt("Vending machine ID to remove: ")
        AdminController.removeVendingMachine(vmId)
        println("Vending machine $vmId and all its slots have been removed.")
    }

    //Why? UI and presentation. One flow for any category instead of one per category.
    private fun viewAllProducts() {
        val category = readEnum(ProductCategory::class.java, "Product category")
        val products = AdminController.getAllProductsOfCategory(category)
        if (products.isEmpty()) { println("No $category items registered yet."); return }
        println("\n===== All $category Items =====")
        products.sortedBy { it.productId.substringAfterLast("-").toIntOrNull() ?: 0 }.forEach { println("$it\n-------------------------") }
    }

    // ===================== 5. Add product to slot (auto type: new vs old) =====================

    //Why? Single entry point for stocking a slot. Figures out the machine/slot first, then
    //auto-detects whether the product being added is brand-new (needs registration) or already
    //registered (just needs to be placed/refilled). Internally this still drives every one of the
    //original stocking methods (add slot, register, add new type, refill, view count) - it just
    //no longer exposes them as separate top level menu items.
    private fun addProductToSlot() {
        println("\n--- Add Product to Slot ---")
        displayVendingMachineMenu()
        val vmId = prompt("Vending machine ID: ")
        val category = AdminController.getCategoryByVendingMachineId(vmId)

        val slotId = resolveSlotForVendingMachine(vmId, category)

        var productId: String? = null
        var knowsStatus = false

        while (!knowsStatus) {
            println("\nIs this product NEW (not registered yet) or OLD (already registered)?")
            println("  1. New")
            println("  2. Old")
            println("  3. Not sure - view product count at this machine first")
            when (prompt("Choose (1-3): ")) {
                "1" -> {
                    productId = registerProduct(category)
                    knowsStatus = true
                }
                "2" -> {
                    productId = pickExistingProduct(category)
                    knowsStatus = true
                }
                "3" -> viewProductCount(vmId)
                else -> println("Invalid choice.")
            }
        }

        val id = productId ?: return
        val quantity = readInt("Quantity")

        //Why? "check" step from the flow - is this product already sitting in the chosen slot?
        val alreadyInSlot = AdminController.getProductsInSlot(vmId, slotId).any { it.productId == id }
        if (alreadyInSlot) {
            AdminController.refillProductInSlot(vmId, slotId, id, quantity)
            println("Slot refilled successfully.")
        } else {
            AdminController.addNewProductTypeToSlot(vmId,slotId, id, quantity, category)
            println("Product added to slot successfully.")
        }
    }

    //Why? Lets the admin either reuse an existing slot on the machine or create a brand new one
    //(replaces the old standalone "Add slot to vending machine" menu item - still calls the same
    //AdminController.addSlotToVendingMachine internally).
    private fun resolveSlotForVendingMachine(vmId: String, category: ProductCategory): String {
        val slotsOnMachine = AdminController.getAllSlots(vmId).filter { it.vendingMachineId == vmId }

        println("\nUse an existing slot or create a new one?")
        println("  1. Existing slot")
        println("  2. New slot")
        return when (prompt("Choose (1-2): ")) {
            "2" -> createNewSlot(vmId, category)
            else -> {
                if (slotsOnMachine.isEmpty()) {
                    println("No existing slots on this machine - creating a new one instead.")
                    createNewSlot(vmId, category)
                } else {
                    println("\n@Slots on $vmId....")
                    slotsOnMachine.sortedBy { it.slotId.substringAfterLast("-").toIntOrNull() ?: 0 }.forEach { println("${it.slotId} | ${it.vendingMachineId}") }
                    prompt("Slot ID: ")
                }
            }
        }
    }

    //Why? Reuses AdminController.addSlotToVendingMachine (the old "4. Add slot to vending machine")
    private fun createNewSlot(vmId: String, category: ProductCategory): String {
        displayProductMenuCategoryWise(category)
        val productItems = readProductItemsMap(category, "new slot")
        val slot = AdminController.addSlotToVendingMachine(vmId, productItems)
        println("\nSlot added successfully!")
        println(slot)
        return slot.slotId
    }

    //Why? "New" branch - registers the product (per-category fields genuinely differ) and hands
    //back its generated ID so it can immediately be placed in the slot.
    private fun registerProduct(category: ProductCategory): String {
        val product = when (category) {
            ProductCategory.FOOD -> registerFood()
            ProductCategory.ELECTRONIC -> registerElectronics()
        }
        return product.productId
    }

    //Why? Validation and forward calling along with UI
    private fun registerFood(): Product {
        println("\n--- Register Food Item ---")
        val productName = prompt("Product name: ")
        val brand = prompt("Brand: ")
        val description = prompt("Description: ")
        var warning: String = prompt("Warning (press Enter to skip): ")
        if (warning.isEmpty()) warning = "- nil -"

        val price = readBigDecimal("Price: ")
        val manufacturingLocation = readEnum(Location::class.java, "Manufacturing location")
        val manufacturingDate = readDate("Manufacturing date (yyyy-MM-dd): ")
        val expiryDate = readDate("Expiry date (yyyy-MM-dd): ")
        val vegOrNonVeg = readEnum(VegNonVeg::class.java, "Veg / Non-veg")

        val ingredients = prompt("Ingredients (comma-separated): ").trim().split(",")

        val foodType = readEnum(FoodType::class.java, "Food type")

        val food = AdminController.registerFood(productName, brand, description, warning,
            price, manufacturingLocation, manufacturingDate, vegOrNonVeg,
            ingredients, expiryDate, foodType
        )
        println("\nFood registered successfully!")
        println(food)
        return food
    }

    //Why? Validation and forward calling along with UI
    private fun registerElectronics(): Product {
        println("\n--- Register Electronics Item ---")
        val productName = prompt("Product name: ")
        val brand = prompt("Brand: ")
        val description = prompt("Description: ")
        var warning: String = prompt("Warning (press Enter to skip): ")
        if (warning.isEmpty()) warning = "- nil -"

        val price = readBigDecimal("Price: ")
        val manufacturingLocation = readEnum(Location::class.java, "Manufacturing location")
        val manufacturingDate = readDate("Manufacturing date (yyyy-MM-dd): ")
        val warrantyMonths = readInt("Warranty (months)")
        val batteryPowered = prompt("Battery powered? (y/n): ").equals("y", ignoreCase = true)
        val electronicsType = readEnum(ElectronicsType::class.java, "Electronics type")

        val electronics = AdminController.registerElectronics(
            productName, brand, description, warning,
            price, manufacturingLocation, manufacturingDate,
            warrantyMonths, batteryPowered, electronicsType
        )
        println("\nElectronics item registered successfully!")
        println(electronics)
        return electronics
    }

    //Why? "Old" branch - display, select. The "check" (already-in-slot vs new-to-slot) happens
    //back in addProductToSlot() once we know both the slotId and the productId.
    private fun pickExistingProduct(category: ProductCategory): String {
        displayProductMenuCategoryWise(category)
        return prompt("Product ID: ")
    }

    //Why? UI and presentation. Reachable from step 5 when the admin isn't sure whether a product
    //is new or old yet - still calls the exact same lookups as the old standalone menu item.
    private fun viewProductCount(vmId: String) {
        val stockMap = AdminController.getProductCountForMachine(vmId)
        if (stockMap.isEmpty()) { println("No products currently stocked."); return }
        println("\n  %-14s %-22s %8s  %6s".format("Product ID", "Name", "Price", "Stock"))
        println("  ──────────────────────────────────────────────────")
        for ((productId, qty) in stockMap) {
            val product = AdminController.getProductById(productId)
            println("  %-14s %-22s Rs.%-5s  %6d".format(product.productId, product.productName, product.price, qty))
        }
        val total = stockMap.values.sum()
        println("  Total units : $total")
    }

    // ===================== 6/7/8 - unchanged from before, just renumbered =====================

    //Why? UI and presentation.
    private fun viewCashDrawer() {
        println("\n--- View Cash Drawer ---")
        displayVendingMachineMenu()
        val vmId = prompt("Vending machine ID: ")
        println("\n===== Cash Drawer — $vmId =====")
        AdminController.getDenominationBreakdown(vmId).forEach { (denom, count) ->
            println("  Rs.%-4d  x  %d".format(denom.value, count))
        }
        println("  Total : Rs.${AdminController.getTotalCashInMachine(vmId)}")
    }

    //Why? Easy cash refilling (UX).
    private fun addCashToDrawer(vmId: String = "") {
        println("\n--- Add Cash to Drawer ---")

        var vendingMachineId = vmId
        val denominations = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)

        if(vendingMachineId.isBlank()) {
            displayVendingMachineMenu()
            vendingMachineId = prompt("Vending machine ID: ")
        }

        println("Enter how many of each denomination to add (Enter to skip):")
        for (denom in IndianCurrency.entries) {
            val input = prompt("  Rs.${denom.value}: ")
            if (input.isEmpty()) continue
            try {
                val count = input.toInt()
                if (count > 0) denominations[denom] = count
                else println("  Skipped — must be greater than zero.")
            } catch (_: NumberFormatException) {
                println("  Invalid input, skipping Rs.${denom.value}")
            }
        }

        if (denominations.isEmpty()) { println("Nothing added."); return }

        AdminController.addCashToDrawer(vendingMachineId, denominations)
        println("\nCash added. Current drawer for $vendingMachineId:")
        AdminController.getDenominationBreakdown(vendingMachineId).forEach { (denom, count) ->
            println("  Rs.%-4d  x  %d".format(denom.value, count))
        }
        println("  Total : Rs.${AdminController.getTotalCashInMachine(vendingMachineId)}")
    }

    //Why? UI and presentation.
    private fun viewPurchaseHistory() {
        val purchases = AdminController.getAllPurchases()
        if (purchases.isEmpty()) { println("No purchases recorded yet."); return }
        println("\n===== Purchase History =====")
        for (p in purchases) {
            println("  ID     : ${p.purchaseId}")
            println("  Time   : ${p.purchaseTime}")
            println("  Items  : ${p.getQuantityOfProductsPurchased()}")
            println("  Total  : Rs.${p.totalAmount}")
            println("  Paid   : Rs.${p.moneyPaidByCustomer}")
            println("  Change : Rs.${p.moneyToBeReturnedByVendingMachine}")
            println("  ────────────────────────────")
        }
    }

    // ===================== display helpers =====================

    //Why? For UX, a short menu display for all vending machine, for easy selection for the user
    private fun displayVendingMachineMenu() {
        val allVendingMachine: Set<VendingMachine> = AdminController.viewAllVendingMachines()

        if(allVendingMachine.isEmpty()){
            throw EmptyMenuException("vending machine")
        }
        println("""

            @Vending Machine Menu....

        """.trimIndent())
        allVendingMachine.sortedBy { it.vendingMachineId.substringAfterLast("-").toIntOrNull() ?: 0 }.forEach {
            println("${it.vendingMachineId} | ${it.establishedOn} ")
        }
        println()
    }

    //Why? For UX, a shortlisting of products in one category, for easy selection.
    private fun displayProductMenuCategoryWise(category: ProductCategory) {
        val products: Set<Product> = AdminController.getAllProductsOfCategory(category)
        val sortedProducts: Set<Product> =
            products.toSortedSet(compareBy { it.productId.substringAfterLast("-").toIntOrNull() ?: 0 })

        if(sortedProducts.isEmpty()){
            throw EmptyMenuException(category.toString().lowercase())
        }
        println("""

            @$category Menu....

        """.trimIndent())
        sortedProducts.forEach {
            println("${it.productId} | ${it.productName} | ${it.brand} | ${it.price}")
        }
        println()
    }
}
//package ui
//
//import controller.AdminController
//import exception.VendingMachineException
//import model.Product
//import model.Slot
//import model.VendingMachine
//import model.enum.*
//import java.util.EnumMap
//
////The purpose of AdminUI is to get input from the admin and display the fetched data and status of the system in a human friendly manner, and it does this by using class that implements the model.enum(Interface Interactable).
//class AdminUI() : Interactable {
//
//    //Why? For looping the options till exit request
//    fun show() {
//        var running = true
//
//        while (running) {
//            println("\n========== ADMIN MENU ==========")
//            println("1.  Create vending machine")
//            println("2.  View vending machine")
//            println("3.  Remove vending machine")
//            println("4.  Add slot to vending machine")
//            println("5.  Remove slot")
//            println("6.  Register new product")
//            println("7.  Remove product")
//            println("8.  Edit product")
//            println("9.  Add new product type to slot")
//            println("10. Refill product in slot")
//            println("11. View all vending machines")
//            println("12. View all products")
//            println("13. View product count at a machine")
//            println("14. View cash drawer")
//            println("15. Add cash to drawer")
//            println("16. View purchase history")
//            println("0.  Exit")
//            println("=================================")
//
//            try {
//                when (prompt("Please enter your choice : ")) {
//                    "1"  -> createVendingMachine()
//                    "2"  -> viewVendingMachine()//todo all or one?
//                    "3"  -> removeVendingMachine()
//                    "4"  -> addSlotToVendingMachine()//X no
//                    "5"  -> removeSlot()//X no
//                    "6"  -> registerProduct()//X no
//                    "7"  -> removeProduct()//X no
//                    "8"  -> editProduct()//X no
//                    "9"  -> addNewProductTypeToSlot()//X no
//                    "10" -> refillProductInSlot()//todo showcase viewProductCount, new in slot: (registered or new) or existing in slot?
//                    "11" -> viewAllVendingMachines()//X no
//                    "12" -> viewAllProducts()
//                    "13" -> viewProductCount()//X no
//                    "14" -> viewCashDrawer()
//                    "15" -> addCashToDrawer()
//                    "16" -> viewPurchaseHistory()
//                    "0"  -> running = false
//                    else -> println("Invalid choice. Please try again.")
//                }
//            } catch (e: VendingMachineException) {
//                println("[Error] ${e.message}")
//            } catch (e: IllegalArgumentException) {
//                println("[Input Error] ${e.message}")
//            }
//        }
//    }
//
//    //Why? Vending machine creation flow, UI and presentation, along with forward calls
//    private fun createVendingMachine() {
//        println("\n--- Create Vending Machine ---")
//        val allowedProductCategory = readEnum(ProductCategory::class.java, "Product Type")
//        val location = readEnum(Location::class.java, "Location")
//        val establishedOn = readDate("Established on (yyyy-MM-dd): ")
//        displayProductMenuCategoryWise(allowedProductCategory)
//        val firstSlotProductItems = readProductItemsMap(allowedProductCategory,"first slot")
//        val vm = AdminController.createVendingMachine(location, establishedOn, firstSlotProductItems, allowedProductCategory)
//        println("\nVending machine created successfully!")
//        println(vm)
//    }
//
//    //Why? UI and presentation, along with forward calls.
//    private fun viewVendingMachine() {
//        println("\n--- View Vending Machine ---")
//        displayVendingMachineMenu()
//        val vmId = prompt("Vending machine ID to view: ")
//        val vm = AdminController.viewVendingMachine(vmId)
//        println("\n$vm")
//    }
//
//    //Why? UI and presentation, along with forward calls.
//    private fun removeVendingMachine() {
//        println("\n--- Remove Vending Machine ---")
//        displayVendingMachineMenu()
//        val vmId = prompt("Vending machine ID to remove: ")
//        AdminController.removeVendingMachine(vmId)
//        println("Vending machine $vmId and all its slots have been removed.")
//    }
//
//    //Why? To maintain slot, UI and presentation, along with forward calls
//    private fun addSlotToVendingMachine() {
//        println("\n--- Add Slot to Vending Machine ---")
//        displayVendingMachineMenu()
//        val vendingMachineId = prompt("Vending machine ID: ")
//        val category = AdminController.getCategoryByVendingMachineId(vendingMachineId)
//        displayProductMenuCategoryWise(category)
//        val productItems = readProductItemsMap(category,"new slot")
//        val slot = AdminController.addSlotToVendingMachine(vendingMachineId, productItems)
//        println("\nSlot added successfully!")
//        println(slot)
//    }
//
//    //Why? UI and presentation, along with forward calls
//    private fun removeSlot() {
//        println("\n--- Remove Slot ---")
//        displaySlotMenu()
//        val slotId = prompt("Slot ID to remove: ")
//        AdminController.removeSlot(slotId)
//        println("Slot $slotId removed.")
//    }
//
//    // ===================== Registration (kept per-category - fields genuinely differ) =====================
//
//    private fun registerProduct(){
//        val productCategory = readEnum(ProductCategory::class.java, "Product Type")
//        when(productCategory){
//            ProductCategory.FOOD -> registerFood()
//            ProductCategory.ELECTRONIC -> registerElectronics()
//        }
//    }
//
//    //Why? Validation and forward calling along with UI
//    private fun registerFood() {
//        println("\n--- Register Food Item ---")
//        val productName = prompt("Product name: ")
//        val brand = prompt("Brand: ")
//        val description = prompt("Description: ")
//        var warning: String = prompt("Warning (press Enter to skip): ")
//        if (warning.isEmpty()) warning = "- nil -"
//
//        val price = readBigDecimal("Price: ")
//        val manufacturingLocation = readEnum(Location::class.java, "Manufacturing location")
//        val manufacturingDate = readDate("Manufacturing date (yyyy-MM-dd): ")
//        val expiryDate = readDate("Expiry date (yyyy-MM-dd): ")
//        val vegOrNonVeg = readEnum(VegNonVeg::class.java, "Veg / Non-veg")
//
//        val ingredients = prompt("Ingredients (comma-separated): ").trim().split(",")
//
//        val foodType = readEnum(FoodType::class.java, "Food type")
//
//        val food = AdminController.registerFood(productName, brand, description, warning,
//            price, manufacturingLocation, manufacturingDate, vegOrNonVeg,
//            ingredients, expiryDate, foodType
//        )
//        println("\nFood registered successfully!")
//        println(food)
//    }
//
//    //Why? Validation and forward calling along with UI
//    private fun registerElectronics() {
//        println("\n--- Register Electronics Item ---")
//        val productName = prompt("Product name: ")
//        val brand = prompt("Brand: ")
//        val description = prompt("Description: ")
//        var warning: String = prompt("Warning (press Enter to skip): ")
//        if (warning.isEmpty()) warning = "- nil -"
//
//        val price = readBigDecimal("Price: ")
//        val manufacturingLocation = readEnum(Location::class.java, "Manufacturing location")
//        val manufacturingDate = readDate("Manufacturing date (yyyy-MM-dd): ")
//        val warrantyMonths = readInt("Warranty (months)")
//        val batteryPowered = prompt("Battery powered? (y/n): ").equals("y", ignoreCase = true)
//        val electronicsType = readEnum(ElectronicsType::class.java, "Electronics type")
//
//        val electronics = AdminController.registerElectronics(
//            productName, brand, description, warning,
//            price, manufacturingLocation, manufacturingDate,
//            warrantyMonths, batteryPowered, electronicsType
//        )
//        println("\nElectronics item registered successfully!")
//        println(electronics)
//    }
//
//    // ===================== Category-generic product operations (edit / remove / view) =====================
//
//    //Why? UI and presentation, along with forward calls. One flow for any category instead of one per category.
//    private fun removeProduct() {
//        println("\n--- Remove Product ---")
//        val category = readEnum(ProductCategory::class.java, "Product category")
//        displayProductMenuCategoryWise(category)
//        val productId = prompt("Product ID to remove: ")
//        AdminController.removeProduct(category, productId)
//        println("Product $productId removed from registry.")
//    }
//
//    //Why? UI and presentation, along with forward calls. Asks which field once, instead of one menu item per field x category.
//    private fun editProduct() {
//        println("\n--- Edit Product ---")
//        val category = readEnum(ProductCategory::class.java, "Product category")
//        displayProductMenuCategoryWise(category)
//        val productId = prompt("Product ID: ")
//
//        println(
//            """
//            Which field do you want to edit?
//              1. Description
//              2. Name
//              3. Price
//              4. Brand
//              5. Warning
//            """.trimIndent()
//        )
//        when (prompt("Choose (1-5): ")) {
//            "1" -> {
//                val newDescription = prompt("New description: ")
//                AdminController.editProductDescription(category, productId, newDescription)
//                println("Description updated.")
//            }
//            "2" -> {
//                val newName = prompt("New name: ")
//                AdminController.editProductName(category, productId, newName)
//                println("Name updated.")
//            }
//            "3" -> {
//                val newPrice = readBigDecimal("New price: ")
//                AdminController.editProductPrice(category, productId, newPrice)
//                println("Price updated.")
//            }
//            "4" -> {
//                val newBrand = prompt("New brand: ")
//                AdminController.editProductBrand(category, productId, newBrand)
//                println("Brand updated.")
//            }
//            "5" -> {
//                var newWarning: String? = prompt("New warning (press Enter to clear): ")
//                if (newWarning!!.isEmpty()) newWarning = null
//                AdminController.editProductWarning(category, productId, newWarning)
//                println("Warning updated.")
//            }
//            else -> println("Invalid choice.")
//        }
//    }
//
//
//    //Why? UI and presentation, along with forward calls.
//    private fun addNewProductTypeToSlot() {
//        println("\n--- Add New Product Type to Slot ---")
//        displaySlotMenu()
//        val slotId = prompt("Slot ID: ")
//
//        val category = AdminController.getCategoryBySlotId(slotId)
//        displayProductMenuCategoryWise(category)
//
//        val productId = prompt("Product ID: ")
//        val quantity = readInt("Quantity")
//        AdminController.addNewProductTypeToSlot(slotId, productId, quantity, category)
//        println("Product added to slot successfully.")
//    }
//
//    //Why? UI and presentation, along with forward calls.
//    private fun refillProductInSlot() {
//        println("\n--- Refill Product in Slot ---")
//        displaySlotMenu()
//        val slotId = prompt("Slot ID: ")
//        val category = AdminController.getCategoryBySlotId(slotId)
//        val productsAlreadyExistingInSlot = AdminController.getProductsInSlot(slotId)
//        displayProductMenuCustomised(productsAlreadyExistingInSlot, category)
//
//        val productId = prompt("Product ID: ")
//        val quantity = readInt("Quantity to add")
//        AdminController.refillProductInSlot(slotId, productId, quantity)
//        println("Slot refilled successfully.")
//    }
//
//    //Why? UI and presentation.
//    private fun viewAllVendingMachines() {
//        val machines = AdminController.viewAllVendingMachines()
//        if (machines.isEmpty()) { println("No vending machines registered yet."); return }
//        println("\n===== All Vending Machines =====")
//        machines.forEach { println("$it\n--------------------------------") }
//    }
//
//    //Why? UI and presentation. One flow for any category instead of one per category.
//    private fun viewAllProducts() {
//        val category = readEnum(ProductCategory::class.java, "Product category")
//        val products = AdminController.getAllProductsOfCategory(category)
//        if (products.isEmpty()) { println("No $category items registered yet."); return }
//        println("\n===== All $category Items =====")
//        products.forEach { println("$it\n-------------------------") }
//    }
//
//    //Why? UI and presentation.
//    private fun viewProductCount() {
//        println("\n--- Product Count at Machine ---")
//        displayVendingMachineMenu()
//        val vmId = prompt("Vending machine ID: ")
//        val stockMap = AdminController.getProductCountForMachine(vmId)
//        if (stockMap.isEmpty()) { println("No products currently stocked."); return }
//        println("\n  %-14s %-22s %8s  %6s".format("Product ID", "Name", "Price", "Stock"))
//        println("  ──────────────────────────────────────────────────")
//        for ((productId, qty) in stockMap) {
//            val product = AdminController.getProductById(productId)
//            println("  %-14s %-22s Rs.%-5s  %6d".format(product.productId, product.productName, product.price, qty))
//        }
//        val total = stockMap.values.sum()
//        println("  Total units : $total")
//    }
//
//    //Why? UI and presentation.
//    private fun viewCashDrawer() {
//        println("\n--- View Cash Drawer ---")
//        displayVendingMachineMenu()
//        val vmId = prompt("Vending machine ID: ")
//        println("\n===== Cash Drawer — $vmId =====")
//        AdminController.getDenominationBreakdown(vmId).forEach { (denom, count) ->
//            println("  Rs.%-4d  x  %d".format(denom.value, count))
//        }
//        println("  Total : Rs.${AdminController.getTotalCashInMachine(vmId)}")
//    }
//
//    //Why? Easy cash refilling (UX).
//    private fun addCashToDrawer() {
//        println("\n--- Add Cash to Drawer ---")
//        displayVendingMachineMenu()
//        val vmId = prompt("Vending machine ID: ")
//        val denominations = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)
//
//        println("Enter how many of each denomination to add (Enter to skip):")
//        for (denom in IndianCurrency.entries) {
//            val input = prompt("  Rs.${denom.value}: ")
//            if (input.isEmpty()) continue
//            try {
//                val count = input.toInt()
//                if (count > 0) denominations[denom] = count
//                else println("  Skipped — must be greater than zero.")
//            } catch (_: NumberFormatException) {
//                println("  Invalid input, skipping Rs.${denom.value}")
//            }
//        }
//
//        if (denominations.isEmpty()) { println("Nothing added."); return }
//
//        AdminController.addCashToDrawer(vmId, denominations)
//        println("\nCash added. Current drawer for $vmId:")
//        AdminController.getDenominationBreakdown(vmId).forEach { (denom, count) ->
//            println("  Rs.%-4d  x  %d".format(denom.value, count))
//        }
//        println("  Total : Rs.${AdminController.getTotalCashInMachine(vmId)}")
//    }
//
//    //Why? UI and presentation.
//    private fun viewPurchaseHistory() {
//        val purchases = AdminController.getAllPurchases()
//        if (purchases.isEmpty()) { println("No purchases recorded yet."); return }
//        println("\n===== Purchase History =====")
//        for (p in purchases) {
//            println("  ID     : ${p.purchaseId}")
//            println("  Time   : ${p.purchaseTime}")
//            println("  Items  : ${p.getQuantityOfProductsPurchased()}")
//            println("  Total  : Rs.${p.totalAmount}")
//            println("  Paid   : Rs.${p.moneyPaidByCustomer}")
//            println("  Change : Rs.${p.moneyToBeReturnedByVendingMachine}")
//            println("  ────────────────────────────")
//        }
//    }
//
//    //Why? For UX, a short menu display for all vending machine, for easy selection for the user
//    private fun displayVendingMachineMenu(){
//        val allVendingMachine: Set<VendingMachine> = AdminController.viewAllVendingMachines()
//        println("""
//
//            @Vending Machine Menu....
//
//        """.trimIndent())
//        allVendingMachine.forEach {
//            println("${it.vendingMachineId} | ${it.establishedOn} ")
//        }
//        println()
//    }
//
//    //Why? For UX, a shortlisting of products in one category, for easy selection. Replaces the old
//    //displayProductMenu()/displayElectronicsMenu() pair - one generic helper works for any ProductCategory
//    //since it only prints fields common to every Product (id/name/brand/price).
//    private fun displayProductMenuCategoryWise(category: ProductCategory){
//        val products: Set<Product> = AdminController.getAllProductsOfCategory(category)//todo terminate one layer up
//        println("""
//
//            @$category Menu....
//
//        """.trimIndent())
//        products.forEach {
//            println("${it.productId} | ${it.productName} | ${it.brand} | ${it.price}")
//        }
//        println()
//    }
//
//    //Why? For UX, a shortlisting of products according to passed Set
//    private fun displayProductMenuCustomised(productsInSlot: List<Product>, category: ProductCategory){
//        println("""
//
//            @$category Menu....
//
//        """.trimIndent())
//        productsInSlot.forEach {
//            println("${it.productId} | ${it.productName} | ${it.brand} | ${it.price}")
//        }
//        println()
//    }
//
//    //Why? For UX, a short menu display for all vending machine, for easy selection for the user
//    private fun displaySlotMenu(){
//        val allSlots: Set<Slot> = AdminController.getAllSlots()
//        println("""
//
//            @Slot Menu....
//
//        """.trimIndent())
//        allSlots.forEach {
//            println("${it.slotId} | ${it.vendingMachineId} ")
//        }
//        println()
//    }
//}
