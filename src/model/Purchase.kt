package model

import generator.IDGenerator
import java.math.BigDecimal
import java.time.LocalDateTime

//The purpose of Purchase is to purely hold record for all the successful purchases and needs only read and create, no editing or deletion, and it does this by using data class.
data class Purchase(
    private val quantityOfProductsPurchased: Map<String, Int>,
    val totalAmount: BigDecimal,
    val moneyPaidByCustomer: BigDecimal,
    val moneyToBeReturnedByVendingMachine: BigDecimal,
    val purchaseTime: LocalDateTime = LocalDateTime.now()
) {

    val purchaseId: String = IDGenerator.generatePurchaseId()

    //Why? For encapsulating and restricting modification of the collection
    fun getQuantityOfProductsPurchased(): Map<String, Int> {
        return quantityOfProductsPurchased.toMap()
    }

    init {
        require(quantityOfProductsPurchased.isNotEmpty()) { "Purchase cannot be made with an empty cart." }
        require(totalAmount > BigDecimal.ZERO) { "Total amount must be greater than zero." }
        require(moneyPaidByCustomer > BigDecimal.ZERO) { "Cash paid must be greater than zero." }
        require(moneyToBeReturnedByVendingMachine >= BigDecimal.ZERO) { "Change cannot be negative." }
        //Runs along with primary const
    }

    override fun toString(): String =
        """
    Purchase ID             : $purchaseId
    Purchase Time           : $purchaseTime
    Products                : $quantityOfProductsPurchased
    Total Amount            : ₹$totalAmount
    Amount Paid             : ₹$moneyPaidByCustomer
    Change Returned         : ₹$moneyToBeReturnedByVendingMachine
    """.trimIndent()
}