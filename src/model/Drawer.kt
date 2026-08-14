package model

import exception.AvailabilityRequirementException
import model.enum.IndianCurrency
import java.math.BigDecimal
import java.util.EnumMap

//The purpose of Drawer is to behave as a drawer for individual vending machines, and it does this by class.
class Drawer {

    private val denominations = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)

    init {
        for (denomination in IndianCurrency.entries) {
            denominations[denomination] = 0
        }
        //Runs along with primary const
    }

    //Why? To reduce duplication of checking the nos of denomination
    fun getCount(denomination: IndianCurrency): Int {
        return denominations[denomination] ?: 0
    }

    //Why? To safely refill the denomination in drawer
    fun add(denomination: IndianCurrency, count: Int) {
        require (count > 0) { "Count must be greater than zero." }

        denominations[denomination] = getCount(denomination) + count
    }

    //Why? To reduce duplication, and ensure that the demand is not more than supply
    fun deduct(denomination: IndianCurrency, count: Int) {
        val current = getCount(denomination)

        require( count > 0) { "Entered value must be greater than zero." }
        if (count > current) {
            throw AvailabilityRequirementException("Insufficient denomination to deduct.")
        }

        denominations[denomination] = current - count
    }

    //Why? For encapsulating and restricting modification to the collection. Read-only.
    fun getDenominations(): Map<IndianCurrency, Int> {
        return denominations.toMap()
    }

    //Why? To reduce duplication
    fun totalCash(): BigDecimal {
        var total = BigDecimal.ZERO

        for ((denomination, count) in denominations) {
            val denominationValue = BigDecimal.valueOf(denomination.value.toLong())
            total = total.add(
                denominationValue.multiply(BigDecimal.valueOf(count.toLong()))
            )
        }

        return total
    }

    override fun toString(): String =
        """
    Drawer
    ------
    ${
            denominations.entries.joinToString("\n") {
                "${it.key} : ${it.value}"
            }
        }
    Total Cash : ₹${totalCash()}
    """.trimIndent()
}