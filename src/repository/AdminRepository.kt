package repository

import model.Admin

//The purpose of AdminRepository is to return adminId, and it does this by object(Singleton classes in Java) and inherits the BaseRepository for admin data handling.
object AdminRepository : BaseRepository<Admin>() {
    //Why? To avoid duplication
    override fun getId(entity: Admin) = entity.adminId
}