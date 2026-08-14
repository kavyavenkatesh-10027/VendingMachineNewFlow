package service

import model.Food
import model.ProductFactory
import repository.FoodRepository
import model.enum.FoodType
import model.enum.Location
import model.enum.VegNonVeg
import repository.ProductRepository
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of FoodService is to do Crud for all Foods, and it does this by object (Singleton class in Java).
object FoodService : BaseProductService<Food>() {
    fun registerFood(
        productName: String, brand: String, description: String,
        warning: String?, price: BigDecimal, manufacturingLocation: Location,
        manufacturingDate: LocalDate, vegOrNonVeg: VegNonVeg,
        ingredients: List<String>, expiryDate: LocalDate, foodType: FoodType
    ): Food {
        val food = ProductFactory.createFood(
            productName = productName,
            brand = brand,
            description = description,
            warning = warning,
            price = price,
            manufacturingLocation = manufacturingLocation,
            manufacturingDate = manufacturingDate,
            vegOrNonVeg = vegOrNonVeg,
            ingredients = ingredients,
            expiryDate = expiryDate,
            foodType = foodType
        )

        ProductRepository.add(food)
        FoodRepository.add(food)
        return food
    }

    //Why? To avoid duplication by fetching data from repository once
    override fun getById(productId: String): Food = FoodRepository.findById(productId)

    //Why? To get all the food specific product, very important for category wise enu showcase
    override fun getAllProducts(): Set<Food> = FoodRepository.findAll()

    //Why? For verification and completion of the chain without inconsistency. (Slot contains food, to maintain Slot-Food relationship)
//    fun removeFood(foodId: String) {
//        getById(foodId)  // verify existence
//        SlotService.removeProductTypeFromSlot(foodId)
//        FoodRepository.removeById(foodId)
//        ProductRepository.removeById(foodId)
//    }
}