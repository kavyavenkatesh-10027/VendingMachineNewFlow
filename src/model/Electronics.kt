package model

import model.enum.*
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of Electronics class is to represent an electronics item sold in the vending machine, and it does this by using class.
class Electronics(
    productName: String,
    brand: String,
    description: String,
    price: BigDecimal,
    manufacturingLocation: Location,
    manufacturingDate: LocalDate,
    val warrantyMonths: Int,
    val batteryPowered: Boolean,
    val electronicsType: ElectronicsType,
    warning: String? = null
) : Product(
    productName = productName,
    brand = brand,
    description = description,
    price = price,
    manufacturingLocation = manufacturingLocation,
    manufacturingDate = manufacturingDate,
    warning = warning,
    productCategory = ProductCategory.ELECTRONIC
) {

    init {
        require(warrantyMonths >= 0) { "Warranty months cannot be negative." }
        //Runs along with primary const
    }

    override fun toString(): String =
        super.toString() + "\n" +
                """
    Electronics Type        : $electronicsType
    Warranty                : $warrantyMonths month(s)
    Battery Powered         : $batteryPowered
    """.trimIndent()
}
