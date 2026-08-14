package service

import model.Electronics
import model.ProductFactory
import repository.ElectronicsRepository
import model.enum.ElectronicsType
import model.enum.Location
import repository.ProductRepository
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of ElectronicsService is to do Crud for all Electronics items, and it does this by object (Singleton class in Java).
object ElectronicsService : BaseProductService<Electronics>() {

    fun registerElectronics(
        productName: String, brand: String, description: String,
        warning: String?, price: BigDecimal, manufacturingLocation: Location,
        manufacturingDate: LocalDate, warrantyMonths: Int,
        batteryPowered: Boolean, electronicsType: ElectronicsType
    ): Electronics {
        val electronics = ProductFactory.createElectronics(
            productName = productName,
            brand = brand,
            description = description,
            warning = warning,
            price = price,
            manufacturingLocation = manufacturingLocation,
            manufacturingDate = manufacturingDate,
            warrantyMonths = warrantyMonths,
            batteryPowered = batteryPowered,
            electronicsType = electronicsType
        )

        ProductRepository.add(electronics)
        ElectronicsRepository.add(electronics)
        return electronics
    }

    //Why? To avoid duplication by fetching data from repository once
    override fun getById(productId: String): Electronics = ElectronicsRepository.findById(productId)

    //Why? For consistency and maintaining Controller->Service->Repository flow
    override fun getAllProductsInThisCategory(): Set<Electronics> = ElectronicsRepository.findAll()

        //Why? For verification and completion of the chain without inconsistency.
//    fun removeElectronics(electronicsId: String) {
//        getById(electronicsId)  // verify existence
//        ElectronicsRepository.removeById(electronicsId)
//        ProductRepository.removeById(electronicsId)
//    }
}
