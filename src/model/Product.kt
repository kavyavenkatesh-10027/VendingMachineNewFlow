package model

import generator.IDGenerator
import model.enum.*
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of Product is to represent any product, and it does this by class(sealed).
//Open class because food extends it
sealed class Product(
    var productName: String,
    var brand: String,
    var description: String,
    var price: BigDecimal,
    val manufacturingLocation: Location,
    val manufacturingDate: LocalDate,
    val productCategory: ProductCategory,
    var warning: String? = null
) {
    val productId = IDGenerator.generateProductId()

    init {
        require(productName.isNotBlank()) { "Product must have a name" }
        require(brand.isNotBlank()) { "Product must have a brand" }
        require(description.isNotBlank()) { "Product must have a description" }
        require(price > BigDecimal.ZERO) { "Price must be positive" }
        require(manufacturingDate <= LocalDate.now()) {
            "Manufacturing date must be on or before today"
        }
        //Runs along with primary const
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        return productId == other.productId
    }

    override fun hashCode(): Int = productId.hashCode()

    override fun toString(): String =
        """
    Product ID              : $productId
    Name                    : $productName
    Category                : $productCategory
    Brand                   : $brand
    Description             : $description
    Price                   : ₹$price
    Manufactured At         : $manufacturingLocation
    Manufacturing Date      : $manufacturingDate
    Warning                 : ${warning ?: "None"}
    """.trimIndent()
}