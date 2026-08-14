package model

import model.enum.*
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of Food class is to represent the food item //todo and it should contain list of real life foods, and it does this by using class. maybe use SEALED Class
class Food(
    productName: String,
    brand: String,
    description: String,
    price: BigDecimal,
    manufacturingLocation: Location,
    manufacturingDate: LocalDate,
    val vegOrNonVeg: VegNonVeg,
    private val ingredients: MutableList<String>,
    val expiryDate: LocalDate,
    val foodType: FoodType,
    category: ProductCategory,
    warning: String? = null
) : Product(
    productName = productName,
    brand = brand,
    description = description,
    price = price,
    manufacturingLocation = manufacturingLocation,
    manufacturingDate = manufacturingDate,
    productCategory = category,
    warning = warning
) {

    //Why? For encapsulating and restricting modification of the collection. Read-only
    fun getIngredients(): List<String> {
        return ingredients.toList()
    }//todo must be able to print ingredients

    init {
        require(ingredients.isNotEmpty()) { "Ingredients must be provided"}
        require(expiryDate.isAfter(LocalDate.now())) {"Cannot register an already-expired food item." }
        require(expiryDate.isAfter(manufacturingDate)) {"Expiry date must be after the manufacturing date."}
        //Runs along with primary const
    }

    /***
    //Why? Safe-adding
//    fun addIngredient(ingredient: String) {
//        require(ingredient.isNotBlank()) { "A product cannot have a blank ingredient"}
//        ingredients.add(ingredient)
//    }

    //Why? Safe-removing
//    fun removeIngredient(ingredient: String) {
//        if (ingredient !in ingredients) {
//            throw UnknownEntityException(ingredient, "Ingredient" )
//        }
//        ingredients.remove(ingredient)
//    }
    **/

    override fun toString(): String =
        super.toString() + "\n" +
                """
    Food Type               : $foodType
    Category                : $vegOrNonVeg
    Ingredients             : ${ingredients.joinToString(", ")}
    Expiry Date             : $expiryDate
    """.trimIndent()
}