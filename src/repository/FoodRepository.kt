package repository

import model.Food

//The purpose of FoodRepository is to return foodId, and it does this by object(Singleton classes in Java) and inherits the BaseRepository for food data handling.
object FoodRepository : BaseRepository<Food>() {
    //Why? To avoid duplication
    override fun getId(entity: Food) = entity.productId
}