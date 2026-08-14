package service

import model.Drawer
import exception.IllegalNegativeValueException
import model.enum.IndianCurrency
import exception.InsufficientDenominationForChangeException
import java.math.BigDecimal
import java.util.EnumMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.minus
import kotlin.plus

//The purpose of CurrencyService is to handle drawer operations, and it does this by object (Singleton class in Java).
object CurrencyService {

    //Why? For maintaining drawer data consistency on accepting payment
    fun acceptPayment(drawer: Drawer, inserted: Map<IndianCurrency, Int>): BigDecimal {var total = BigDecimal.ZERO
        for ((denomination, count) in inserted) {  // destructuring map entries with for ((k, v) in $%map)
            addToDrawer(drawer, denomination, count)
            total += BigDecimal.valueOf(denomination.value.toLong()) * BigDecimal.valueOf(count.toLong())
        }
        return total
    }

    //Why? Greedy algorithm implementation.
    fun makeChange(drawer: Drawer, changeAmount: BigDecimal): Map<IndianCurrency, Int> {
        if (changeAmount < BigDecimal.ZERO) throw IllegalNegativeValueException("Change amount")
        if (changeAmount.compareTo(BigDecimal.ZERO) == 0) return emptyMap()

        val change = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)//enum requires a java class
        var remaining = changeAmount
        val denominations = IndianCurrency.entries

        for (i in denominations.indices.reversed()) {  // loop backwards
            if (remaining == BigDecimal.ZERO) break
            val denom = denominations[i]
            val denomValue = BigDecimal.valueOf(denom.value.toLong())
            val available = drawer.getCount(denom)
            val canUse = remaining.divideToIntegralValue(denomValue).toInt()
            val use = minOf(canUse, available)
            if (use > 0) {
                change[denom] = use
                remaining -= denomValue * BigDecimal.valueOf(use.toLong())
            }
        }

        if (remaining != BigDecimal.ZERO) {
            throw InsufficientDenominationForChangeException(changeAmount)
        }

        removeFromDrawer(drawer, change)
        return change
    }

    //Why? To ensure that an invalid purchase returns entered money to customer
    fun refund(drawer: Drawer, inserted: Map<IndianCurrency, Int>) {
        if (inserted.isEmpty()) return
        removeFromDrawer(drawer, inserted)
    }

    //Why? To validate count before refilling drawer with denominations
    fun addToDrawer(drawer: Drawer, denomination: IndianCurrency, count: Int) {
        require (count > 0) {"Count cannot be zero or negative." }
        drawer.add(denomination, count)
    }

    //Why? To avoid duplication and to validate count before removing denominations from the drawer
    fun removeFromDrawer(drawer: Drawer, denomCountRelation: Map<IndianCurrency, Int>) {
        for ((denom, count) in denomCountRelation) {
            require (count > 0) {"Count cannot be zero or negative." }
            drawer.deduct(denom, count)
        }
    }
}