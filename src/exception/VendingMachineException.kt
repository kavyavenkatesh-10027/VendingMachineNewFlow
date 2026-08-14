package exception

import model.enum.ProductCategory
import java.math.BigDecimal

//The purpose of VendingMachineException is to clearly communicate the cause for the breakdown is , and it does this by inheriting Runtime.//done
//Why? To have a common type for all the exception for better readability and handling
abstract class VendingMachineException(message: String) : RuntimeException(message)


class AvailabilityRequirementException(message: String)  : VendingMachineException(message)

class InsufficientPaymentException(total : BigDecimal, amountPaid : BigDecimal)  : VendingMachineException(  
    "Insufficient payment. Total: Rs.$total, Paid: Rs.$amountPaid\nCollect refund from the inserting plate"
)

class InsufficientDenominationForChangeException(changeAmount: BigDecimal) : VendingMachineException(
    "Machine cannot make exact change of Rs.$changeAmount."
)

class UnknownEntityException(entityDetail : String, entity: String = "Entity", suggestion : String = "") : VendingMachineException(
    "$entity : $entityDetail does not exist. $suggestion"
)

class MismatchingProductTypeAndVendingMachine(vendingMachineType: ProductCategory, productCategory: ProductCategory) : VendingMachineException(
    "Cannot add $productCategory product to $vendingMachineType vending machine"
)

class UnregisteredEntityException(item : String, itemId : String, container : String, containerId : String, suggestion : String = "")  : VendingMachineException(
    "$item : $itemId is not present in $container $containerId. $suggestion"
)

class EmptyMenuException(menu: String) : VendingMachineException(
    "Currently, no ${menu}s have been registered under the ${menu}'s menu."
)

class IllegalNegativeValueException(valueName: String)  : IllegalArgumentException(
    "$valueName cannot be negative."
)

class ExistsAlreadyException(message: String)  : VendingMachineException(message)

class SlotVendingMachineMismatchException(slotId : String) : VendingMachineException(
    "Slot $slotId belongs to a different vending machine"
)

class CorruptedDataException(corruptedDataDetails: String)  : VendingMachineException(
    "Vending machine data has been corrupted. $corruptedDataDetails"
)

/**
package repository

import model.Slot

//The purpose of SlotRepository is to return slotId and override the parent method for unique implementation, and it does this by object(Singleton classes in Java) and inherits the BaseRepository for common, non-specific Slot data handling.
object SlotRepository : BaseRepository<Slot>() {

    private val slotsInEveryMachine = mutableMapOf<String, MutableList<Slot>>()

    //Why? To avoid duplication
    override fun getId(entity: Slot) = entity.slotId

    //Why? To maintain slot-machine relation
    override fun add(entity: Slot) {
        super.add(entity)
        slotsInEveryMachine.getOrPut(entity.vendingMachineId) { mutableListOf() }.add(entity)
    }

    //Why? To avoid inconsistency in slot-machine data on delete
    override fun removeById(id: String) {
        val slotToRemove = findById(id)
        slotsInEveryMachine[slotToRemove.vendingMachineId]?.remove(slotToRemove)
        super.removeById(id)
    }

    //Why? To filter slot by their container, which is unique to slot entity only
    fun findByVendingMachineId(vendingMachineId: String): List<Slot> {
        return slotsInEveryMachine[vendingMachineId]?.toList() ?: emptyList()
    }
}

 **/
//The above is my slot repository