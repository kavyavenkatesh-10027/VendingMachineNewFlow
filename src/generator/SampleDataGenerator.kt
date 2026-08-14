import generator.IDGenerator
import model.*
import repository.*
import model.enum.*
import java.math.BigDecimal
import java.time.LocalDate

//The purpose of SampleDataGenerator is to seed the repositories with sample data so the console app has something to show right after startup,
// and it does this calling SampleDataGenerator.load() at the top of main()
object SampleDataGenerator {

    //Why? For instance creation
fun load() {
    // ---------- Admin ----------
    val admin = Admin(
            name = "Priya Raman",
            dob = LocalDate.of(1990, 4, 12),
            gender = Gender.FEMALE
    )
    AdminRepository.add(admin)

    // ---------- Food products ----------
    val lays = Food(
        productName = "Lay's Classic Salted",
        brand = "Lay's",
        description = "Crispy potato chips, classic salted flavor",
        price = BigDecimal("20"),
        manufacturingLocation = Location.GUINDY,
        manufacturingDate = LocalDate.now().minusMonths(1),
        vegOrNonVeg = VegNonVeg.VEG,
        ingredients = mutableListOf("Potato", "Vegetable Oil", "Salt"),
        expiryDate = LocalDate.now().plusMonths(5),
        foodType = FoodType.CHIP,
        category = ProductCategory.FOOD
    )

    val bisleri = Food(
            productName = "Bisleri Mineral Water",
            brand = "Bisleri",
            description = "Packaged drinking water, 500ml",
            price = BigDecimal("15"),
            manufacturingLocation = Location.PORUR,
            manufacturingDate = LocalDate.now().minusMonths(2),
            vegOrNonVeg = VegNonVeg.VEGAN,
            ingredients = mutableListOf("Purified Water"),
            expiryDate = LocalDate.now().plusYears(1),
            foodType = FoodType.WATER_BOTTLE,
            category = ProductCategory.FOOD
    )

    val cocaCola = Food(
            productName = "Coca-Cola",
            brand = "Coca-Cola",
            description = "Chilled carbonated soft drink, 300ml can",
            price = BigDecimal("40"),
            manufacturingLocation = Location.TAMBARAM,
            manufacturingDate = LocalDate.now().minusMonths(1),
            vegOrNonVeg = VegNonVeg.VEGAN,
            ingredients = mutableListOf("Carbonated Water", "Sugar", "Caramel Color", "Caffeine"),
            expiryDate = LocalDate.now().plusMonths(8),
            foodType = FoodType.COLD_DRINK,
            warning = "Contains caffeine",
            category = ProductCategory.FOOD
    )

    val dairyMilk = Food(
            productName = "Cadbury Dairy Milk",
            brand = "Cadbury",
            description = "Milk chocolate bar, 40g",
            price = BigDecimal("50"),
            manufacturingLocation = Location.ANNA_NAGAR,
            manufacturingDate = LocalDate.now().minusDays(20),
            vegOrNonVeg = VegNonVeg.VEG,
            ingredients = mutableListOf("Milk Solids", "Sugar", "Cocoa Butter", "Cocoa Solids"),
            expiryDate = LocalDate.now().plusMonths(9),
            foodType = FoodType.CHOCOLATE_BAR,
            warning = "Contains milk",
            category = ProductCategory.FOOD
    )

    val monsterEnergy = Food(
            productName = "Monster Energy",
            brand = "Monster",
            description = "Energy drink, 350ml can",
            price = BigDecimal("110"),
            manufacturingLocation = Location.VELACHERY,
            manufacturingDate = LocalDate.now().minusMonths(3),
            vegOrNonVeg = VegNonVeg.VEGAN,
            ingredients = mutableListOf("Carbonated Water", "Sugar", "Taurine", "Caffeine", "B-Vitamins"),
            expiryDate = LocalDate.now().plusMonths(6),
            foodType = FoodType.ENERGY_DRINK,
            warning = "High caffeine content; not recommended for children",
            category = ProductCategory.FOOD
    )

    listOf(lays, bisleri, cocaCola, dairyMilk, monsterEnergy).forEach {
        FoodRepository.add(it)
        ProductRepository.add(it)
    }

    // ---------- Slots ----------
    val slot1 = Slot(
            vendingMachineId = IDGenerator.getNextVendingMachineId(),
            productItemsInSlot = mutableMapOf(lays.productId to 15)
    )
    val slot2 = Slot(
            vendingMachineId = IDGenerator.getNextVendingMachineId(),
            productItemsInSlot = mutableMapOf(bisleri.productId to 20, cocaCola.productId to 12)
    )
    val slot3 = Slot(
            vendingMachineId = IDGenerator.getNextVendingMachineId(),
            productItemsInSlot = mutableMapOf(monsterEnergy.productId to 8)
    )

        // ---------- Vending Machine ----------
        // Note: VendingMachine.init requires establishedOn to be strictly
        // AFTER today's date, so we use a near-future date here.
        val machine = VendingMachine(
            vendingMachineLocation = Location.OMR,
            establishedOn = LocalDate.now(),
            ProductCategory.FOOD,
            slot3
        )

        machine.drawer.add(IndianCurrency.FIVE_HUNDRED, 10)
        machine.drawer.add(IndianCurrency.HUNDRED, 20)
        machine.drawer.add(IndianCurrency.FIFTY, 20)
        machine.drawer.add(IndianCurrency.TWENTY, 30)
        machine.drawer.add(IndianCurrency.TEN, 50)
        machine.drawer.add(IndianCurrency.FIVE, 50)
        machine.drawer.add(IndianCurrency.TWO, 50)
        machine.drawer.add(IndianCurrency.ONE, 100)


        listOf(slot1, slot2).forEach { slot ->
            machine.addSlotToVendingMachine(slot)
        }

    VendingMachineRepository.add(machine)

    // ---------- Sample purchase ----------
    val purchase = Purchase(
            quantityOfProductsPurchased = mapOf(lays.productId to 2, bisleri.productId to 1),
            totalAmount = BigDecimal("55"),
            moneyPaidByCustomer = BigDecimal("100"),
            moneyToBeReturnedByVendingMachine = BigDecimal("45")
    )
    PurchaseRepository.add(purchase)

    println("Sample data loaded: 1 admin, 1 vending machine (${IDGenerator.getNextVendingMachineId()}), " +
            "5 food products, 3 slots, 1 purchase record.")
}
}