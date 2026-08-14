package model

import exception.SlotVendingMachineMismatchException
import exception.UnknownEntityException
import generator.IDGenerator
import model.enum.*
import java.time.LocalDate

//The purpose of VendingMachine is to represent a real-world vending machine and has methods to refactor itself, and it does this by using class.
class VendingMachine(
    val vendingMachineLocation: Location,
    val establishedOn: LocalDate,
    val productTypeInside: ProductCategory,
    firstSlot: Slot,
    private val slotsInVendingMachine: MutableSet<Slot> = mutableSetOf()
    //val numberOfslots
    //val quantity

) {
    val vendingMachineId = IDGenerator.generateVendingMachineId()
    val drawer = Drawer()

    //Why? For encapsulating and restricting modification of the collection (Slot is mutable)
    fun getAllSlotsInVendingMachine(): Set<Slot> {
        return slotsInVendingMachine.toSet()
    }

    //Why? To avoid duplication by fetching data from repository once
    fun getOneSlotByIdInVendingMachine(slotId: String): Slot? {
        var slot: Slot? = null
        slotsInVendingMachine.forEach {
            if(it.slotId == slotId){
                slot = it
            }
        }
        return slot
    }

    init {
        require(establishedOn <= LocalDate.now()) {"Established date must be on or before the current date"}
        addSlotToVendingMachine(firstSlot)
        slotsInVendingMachine.forEach { slot ->
            if (slot.vendingMachineId != vendingMachineId) {
                throw SlotVendingMachineMismatchException(slot.slotId)
            }
        }
        //Runs along with primary const
    }

    fun addSlotToVendingMachine(slot: Slot) {
        if (slot.vendingMachineId != vendingMachineId) {
            throw SlotVendingMachineMismatchException(slot.slotId)
        }
        slotsInVendingMachine.add(slot)
    }

//    fun removeSlotFromVendingMachine(slot: Slot) {
//        if (slot.vendingMachineId != vendingMachineId || !slotsInVendingMachine.contains(slot)) {
//            throw UnregisteredEntityException("Slot", slot.slotId,"Vending Machine", vendingMachineId)
//        }
//        slotsInVendingMachine.remove(slot)
//    }

    override fun toString(): String =
        """
    Vending Machine ID      : $vendingMachineId
    Vending Machine Type    : $productTypeInside
    Location                : $vendingMachineLocation
    Established On          : $establishedOn
    Number of Slots         : ${slotsInVendingMachine.size}
    Cash Available          : ₹${drawer.totalCash()}
    """.trimIndent()
}