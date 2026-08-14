package service

import exception.CorruptedDataException
import exception.MismatchingProductTypeAndVendingMachine
import exception.UnknownEntityException
import exception.VendingMachineException
import generator.IDGenerator
import model.Product
import model.Slot
import model.VendingMachine
import repository.VendingMachineRepository
import model.enum.*
import repository.ProductRepository
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

//The purpose of VendingMachineService is to manage vending machine, and it does this by object (Singleton class in Java).
object VendingMachineService {

    //Why? For maintaining the right order in the process of creating a vending machine.
    fun createVendingMachine(
        location: Location,
        establishedOn: LocalDate,
        firstSlotProductItems: Map<String, Int>,
        category: ProductCategory
    ): VendingMachine {
        require(establishedOn <= LocalDate.now()) {"Established date must be on or before the current date"}

        val firstSlotAnalysed = buildSlotForMachine(IDGenerator.getNextVendingMachineId(), firstSlotProductItems, category)
        val vm = VendingMachine(location, establishedOn, category, firstSlotAnalysed)

        VendingMachineRepository.add(vm)
//        SlotRepository.add(firstSlotAnalysed)
        return vm
    }

    //Why? To avoid duplication by fetching data from repository once
    fun getSlotByIdInVendingMachine(slotId: String, vendingMachineId: String): Slot = getVendingMachineById(vendingMachineId).getOneSlotByIdInVendingMachine(slotId)?: throw UnknownEntityException(slotId, "Slot")

    //Why? For consistency and maintaining Controller->Service->Repository flow
    fun getAllSlotsInVendingMachine(vendingMachineId: String) : Set<Slot> = getVendingMachineById(vendingMachineId).getAllSlotsInVendingMachine()

    //Why? For coupling data update in vending machine list as well as slot repo.
    fun addSlotToVendingMachine(vendingMachineId: String, productItems: Map<String, Int>): Slot {
        val vm = getVendingMachineById(vendingMachineId)
        val slot = buildSlotForMachine(vendingMachineId, productItems, vm.productTypeInside)
        vm.addSlotToVendingMachine(slot)
//        SlotRepository.add(slot)
        return slot
    }

    //Why? Internal method to verify product items, and then creating the Slot
    private fun buildSlotForMachine(vendingMachineId: String, productItems: Map<String, Int>, allowedProductCategory: ProductCategory): Slot {
        validateProductItems(allowedProductCategory, productItems)
        return Slot(vendingMachineId, productItems.toMutableMap())
    }

    //Why? To avoid duplication by fetching data from repository once
    fun getVendingMachineById(vendingMachineId: String): VendingMachine =
        VendingMachineRepository.findById(vendingMachineId)

    //Why? To filter out unavailable products and display unique available products.
    fun viewAvailableProducts(vendingMachineId: String): Set<Product> {
        val vendingMachine = getVendingMachineById(vendingMachineId)

        return vendingMachine.getAllSlotsInVendingMachine()
            .flatMap { slot ->
                slot.getProductItemsInSlot().entries
            }//for destructuring
            .filter { (_, quantity) ->
                quantity > 0
            }
            .map { (productId, _) ->
                try {
                    ProductRepository.findById(productId)
                } catch (_: VendingMachineException) {
                    throw CorruptedDataException("Vending machine, ID : $vendingMachineId contains unregistered product item, ID : $productId ")
                }
            }
            .toSet()
    }

    //Why? For machine specific all stock-quantity review.
    fun viewAvailableQuantityForAllProducts(vendingMachineId: String): Map<String, Int> {
        val vm = getVendingMachineById(vendingMachineId)
        val result = mutableMapOf<String, Int>()
        for (slot in vm.getAllSlotsInVendingMachine()) {
            for ((productId, qty) in slot.getProductItemsInSlot()) {
                if (qty > 0) result[productId] = (result[productId] ?: 0) + qty
            }
        }
        return result
    }

    //Why? For machine specific single product-quantity review.
    fun getAvailableQuantityForOneProduct(vendingMachineId: String, productId: String): Int {
        if (!ProductRepository.existsById(productId)) {
            throw UnknownEntityException(productId, "Product","Cannot check quantity for a product that does not exist")
        }
        val vm = getVendingMachineById(vendingMachineId)
        return vm.getAllSlotsInVendingMachine().sumOf { slot -> slot.getProductItemsInSlot()[productId] ?: 0 }
    }

    //Why? For validating before removing. Ensuring cascading deletion of slots and product items within, then and finally removing vending machine from Repo.
    fun removeVendingMachine(vendingMachineId: String) {
        if (!VendingMachineRepository.existsById(vendingMachineId)) {
            throw UnknownEntityException(vendingMachineId,"Vending machine")
        }
//        SlotRepository.findByVendingMachineId(vendingMachineId).forEach {
//            SlotRepository.removeById(it.slotId)
//        }
        VendingMachineRepository.removeById(vendingMachineId)
    }

    //Why? For consistency and maintaining Controller->Service->Repository flow
    fun getAllVendingMachines(): Set<VendingMachine> = VendingMachineRepository.findAll()

    //Why? Internal method for validating product items. Assists buildSlotForMachine()
    private fun validateProductItems(allowedProductCategory: ProductCategory, productItems: Map<String, Int>) {
        require(!productItems.isEmpty()){"A slot must contain at least one product item."}
        for ((productId, qty) in productItems) {
            if (ProductRepository.getCategory(productId) != allowedProductCategory) throw MismatchingProductTypeAndVendingMachine(allowedProductCategory, ProductRepository.getCategory(productId))
            require(productId.isNotBlank()) {"Product ID in slot cannot be empty."}
            require(qty > 0) { "Quantity for product '$productId' must be greater than zero."}
            if (!ProductRepository.existsById(productId)) throw UnknownEntityException(productId, "Product")
        }
    }
}