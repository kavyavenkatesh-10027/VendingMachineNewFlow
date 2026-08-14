package controller

import model.Electronics
import model.Food
import model.Product
import model.Purchase
import model.Slot
import model.VendingMachine
import service.CurrencyService
import service.ElectronicsService
import service.FoodService
import service.PurchaseService
import service.SlotService
import service.VendingMachineService
import model.enum.ElectronicsType
import model.enum.FoodType
import model.enum.IndianCurrency
import model.enum.Location
import model.enum.ProductCategory
import model.enum.VegNonVeg
import service.BaseProductService
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2

//The purpose of AdminController is to direct requests from an admin coming from the AdminUI to the correct service class and its methods, and it does this by class.
object AdminController : BaseController() {

    /***
     *All the methods in AdminController class -
    Why? For input validation and clean flow, for the safety of not letting the UI layer access Service layer.
    ***/
    fun createVendingMachine(
        location: Location,
        establishedOn: LocalDate,
        firstSlotProductItems: Map<String, Int>,
        category: ProductCategory
    ): VendingMachine {
        require(firstSlotProductItems.isNotEmpty()) { "First slot must have at least one product item." }
        return VendingMachineService.createVendingMachine(location, establishedOn, firstSlotProductItems, category)
    }

    fun addSlotToVendingMachine(vendingMachineId: String, productItems: Map<String, Int>): Slot {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }
        require(productItems.isNotEmpty()) { "Slot must have at least one product item." }
        return VendingMachineService.addSlotToVendingMachine(vendingMachineId, productItems)
    }

    fun addNewProductTypeToSlot(vmId: String, slotId: String, productId: String, quantity: Int, category: ProductCategory) {
        require(slotId.isNotBlank()) { "Slot ID cannot be empty." }
        require(productId.isNotBlank()) { "Product ID cannot be empty." }
        require(quantity > 0) { "Quantity must be greater than zero." }

        SlotService.addNewProductTypeToSlot(vmId, slotId, productId, quantity, category)
    }

    fun refillProductInSlot(vmId: String, slotId: String, productId: String, quantity: Int) {
        require(slotId.isNotBlank()) { "Slot ID cannot be empty." }
        require(productId.isNotBlank()) { "Product ID cannot be empty." }
        require(quantity > 0) { "Quantity must be greater than zero." }

        SlotService.refillProductInSlot(vmId, slotId, productId, quantity)
    }

    fun removeVendingMachine(vendingMachineId: String) {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }

        VendingMachineService.removeVendingMachine(vendingMachineId)
    }

    fun getAllSlots(vendingMachineId: String): Set<Slot> = VendingMachineService.getAllSlotsInVendingMachine(vendingMachineId)

//    fun removeSlot(slotId: String) {
//        require(slotId.isNotBlank()) { "Slot ID cannot be empty." }
//
//        SlotService.removeSlot(slotId)
//    }

    fun getProductById(productId: String): Product {
        require(productId.isNotBlank()) { "Product ID cannot be empty." }

        return BaseProductService.getProductById(productId)
    }

    fun getProductCountForMachine(vendingMachineId: String): Map<String, Int> =
        viewAvailableQuantityForAllProducts(vendingMachineId)

    fun addCashToDrawer(vendingMachineId: String, denominations: Map<IndianCurrency, Int>) {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }
        require(denominations.isNotEmpty()) { "Denomination map cannot be empty." }

        val vm = VendingMachineService.getVendingMachineById(vendingMachineId)

        for ((denom, count) in denominations) {
            CurrencyService.addToDrawer(vm.drawer, denom, count)
        }
    }

    fun getDenominationBreakdown(vendingMachineId: String): Map<IndianCurrency, Int> {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }

        return VendingMachineService.getVendingMachineById(vendingMachineId)
            .drawer
            .getDenominations()
    }

    fun getTotalCashInMachine(vendingMachineId: String): BigDecimal {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty." }

        return VendingMachineService.getVendingMachineById(vendingMachineId)
            .drawer
            .totalCash()
    }

    fun getAllPurchases(): Set<Purchase> = PurchaseService.getAllPurchases()

    // Registration (kept per-category )

    fun registerFood(
        productName: String,
        brand: String,
        description: String,
        warning: String?,
        price: BigDecimal,
        manufacturingLocation: Location,
        manufacturingDate: LocalDate,
        vegOrNonVeg: VegNonVeg,
        ingredients: List<String>,
        expiryDate: LocalDate,
        foodType: FoodType
    ): Food {
        require(productName.isNotBlank()) { "Food name cannot be empty." }
        require(brand.isNotBlank()) { "Brand cannot be empty." }
        require(description.isNotBlank()) { "Description cannot be empty." }
        require(price > BigDecimal.ZERO) { "Price cannot be zero or negative." }
        require(manufacturingDate <= LocalDate.now()) { "Manufacturing date cannot be in the future." }
        require(ingredients.isNotEmpty()) { "At least one ingredient must be provided." }

        return FoodService.registerFood(
            productName,
            brand,
            description,
            warning,
            price,
            manufacturingLocation,
            manufacturingDate,
            vegOrNonVeg,
            ingredients,
            expiryDate,
            foodType
        )
    }

    fun registerElectronics(
        productName: String,
        brand: String,
        description: String,
        warning: String?,
        price: BigDecimal,
        manufacturingLocation: Location,
        manufacturingDate: LocalDate,
        warrantyMonths: Int,
        batteryPowered: Boolean,
        electronicsType: ElectronicsType
    ): Electronics {
        require(productName.isNotBlank()) { "Electronics name cannot be empty." }
        require(brand.isNotBlank()) { "Brand cannot be empty." }
        require(description.isNotBlank()) { "Description cannot be empty." }
        require(price > BigDecimal.ZERO) { "Price cannot be zero or negative." }
        require(manufacturingDate <= LocalDate.now()) { "Manufacturing date cannot be in the future." }
        require(warrantyMonths >= 0) { "Warranty months cannot be negative." }

        return ElectronicsService.registerElectronics(
            productName,
            brand,
            description,
            warning,
            price,
            manufacturingLocation,
            manufacturingDate,
            warrantyMonths,
            batteryPowered,
            electronicsType
        )
    }

    // Category-generic product operations (edit / remove / view)

//    fun editProductDescription(category: ProductCategory, productId: String, newDescription: String) {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        require(newDescription.isNotBlank()) { "New description cannot be empty." }
//        when (category) {
//            ProductCategory.FOOD -> FoodService.editDescription(productId, newDescription)
//            ProductCategory.ELECTRONIC -> ElectronicsService.editDescription(productId, newDescription)
//        }
//    }
//
//    fun editProductName(category: ProductCategory, productId: String, newName: String) {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        require(newName.isNotBlank()) { "New name cannot be empty." }
//        when (category) {
//            ProductCategory.FOOD -> FoodService.editName(productId, newName)
//            ProductCategory.ELECTRONIC -> ElectronicsService.editName(productId, newName)
//        }
//    }
//
//    fun editProductPrice(category: ProductCategory, productId: String, newPrice: BigDecimal) {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        require(newPrice > BigDecimal.ZERO) { "Price cannot be zero or negative." }
//        when (category) {
//            ProductCategory.FOOD -> FoodService.editPrice(productId, newPrice)
//            ProductCategory.ELECTRONIC -> ElectronicsService.editPrice(productId, newPrice)
//        }
//    }
//
//    fun editProductBrand(category: ProductCategory, productId: String, newBrand: String) {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        require(newBrand.isNotBlank()) { "New brand cannot be empty." }
//        when (category) {
//            ProductCategory.FOOD -> FoodService.editBrand(productId, newBrand)
//            ProductCategory.ELECTRONIC -> ElectronicsService.editBrand(productId, newBrand)
//        }
//    }
//
//    fun editProductWarning(category: ProductCategory, productId: String, newWarning: String?) {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        // Warning can be blank
//        when (category) {
//            ProductCategory.FOOD -> FoodService.editWarning(productId, newWarning)
//            ProductCategory.ELECTRONIC -> ElectronicsService.editWarning(productId, newWarning)
//        }
//    }

//    fun removeProduct(category: ProductCategory, productId: String) {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        when (category) {
//            ProductCategory.FOOD -> FoodService.removeFood(productId)
//            ProductCategory.ELECTRONIC -> ElectronicsService.removeElectronics(productId)
//        }
//    }

    //Why? A single lookup that works across every product category. This `when` is exhaustive over
//    //This is an overloaded method and is used only when the category is known by the caller(for future need)
//    fun getProductById(category: ProductCategory, productId: String): Product {
//        require(productId.isNotBlank()) { "Product ID cannot be empty." }
//        return when (category) {
//            ProductCategory.FOOD -> FoodService.getById(productId)
//            ProductCategory.ELECTRONIC -> ElectronicsService.getById(productId)
//        }
//    }
    fun getCategoryByVendingMachineId(vendingMachineId: String) : ProductCategory{
        return VendingMachineService.getVendingMachineById(vendingMachineId).productTypeInside
    }
//
//    fun getCategoryBySlotId(slotId: String) : ProductCategory{
//        val slot = getSlotBySlotId(slotId)
//        return VendingMachineService.getVendingMachineById(slot.vendingMachineId).productTypeInside
//    }

    fun getSlotBySlotId(vendingMachineId: String, slotId: String) : Slot {
        return VendingMachineService.getSlotByIdInVendingMachine( slotId, vendingMachineId)
    }

    fun getProductsInSlot(vendingMachineId: String, slotId: String) : List<Product>{
        val productSet = getSlotBySlotId(vendingMachineId ,slotId).getProductItemsInSlot()
        val productsInSlot = productSet.map { (productId, _) -> BaseProductService.getProductById(productId) }
        return productsInSlot
    }

    //Why? Backs the "view all products" / "pick a product" UI flows for any category, generically.
    fun getAllProductsOfCategory(category: ProductCategory): Set<Product> = when (category) {
        ProductCategory.FOOD -> FoodService.getAllProductsInThisCategory()
        ProductCategory.ELECTRONIC -> ElectronicsService.getAllProductsInThisCategory()
    }
}
