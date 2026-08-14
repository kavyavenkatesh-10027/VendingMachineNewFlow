package generator

//The purpose of IDGenerator is to generate values for the Ids automatically on instantiation (called internally), and it does this by using enum(stateless).
object IDGenerator {

    private var nextVendingMachineId = 1L
    private var nextSlotId = 1L
    private var nextAdminId = 1L
    private var nextProductId = 1L
    private var nextPurchaseId = 1L

    /***
    Why (following 5 methods)? To automatically generate unique id for the respective entities
     ***/
    fun getNextVendingMachineId(): String{
        return "vendingMachine-$nextVendingMachineId"
    }

    fun generateVendingMachineId(): String =
        "vendingMachine-${nextVendingMachineId++}"

    fun generateSlotId(): String =
        "slot-${nextSlotId++}"

    fun generateAdminId(): String =
        "admin-${nextAdminId++}"

    fun generateProductId(): String =
        "product-${nextProductId++}"

    fun generatePurchaseId(): String =
        "purchase-${nextPurchaseId++}"
}