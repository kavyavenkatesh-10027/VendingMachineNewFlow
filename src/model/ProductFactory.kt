package model

import model.enum.*
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of ProductFactory is to centralize product construction, and it does this by object (Singleton class in Java)
object ProductFactory {

    fun createFood(
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
    ): Food = Food(
        productName = productName,
        brand = brand,
        description = description,
        price = price,
        manufacturingLocation = manufacturingLocation,
        manufacturingDate = manufacturingDate,
        vegOrNonVeg = vegOrNonVeg,
        ingredients = ingredients.toMutableList(),
        expiryDate = expiryDate,
        foodType = foodType,
        warning = warning
    )

    fun createElectronics(
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
    ): Electronics = Electronics(
        productName = productName,
        brand = brand,
        description = description,
        price = price,
        manufacturingLocation = manufacturingLocation,
        manufacturingDate = manufacturingDate,
        warrantyMonths = warrantyMonths,
        batteryPowered = batteryPowered,
        electronicsType = electronicsType,
        warning = warning
    )

    // For future enhancements, can add createStationery(...), etc. here as new categories.
}
