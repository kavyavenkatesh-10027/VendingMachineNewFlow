import ui.*

/**
 * This program follows the flow of layer UI -> Controller -> Service -> Repository
 **/

//The purpose of main() method is to decide which UI class should be operated, and it does this by using top-level functions.
fun main() {

    val adminCaller = AdminUI()

    val consumerCaller = ConsumerUI()

    SampleDataGenerator.load()


    var running = true

    println("""
        --------------------------
        Welcome to Vending Machine
        --------------------------
        
    """.trimIndent())

    while (running) {
        println(
            """
        (1) Enter as an Admin
        (2) Enter as a Customer
        (0) Exit
        
    """.trimIndent()
        )

        val option = readln().trim()
        when(option){
            "1" -> {
                if (validateAdmin()){
                    adminCaller.show()
                }else{
                    println("Invalid passcode, try again!")
                    continue
                }
            }
            "2" -> consumerCaller.show()
            "0" -> {running = false}
            else -> println("Invalid choice")
        }
    }
}

//The purpose of validAdmin() method is to have a check for the admin so not anyone can enter, and it does this by using a top-level function that assists main().
fun validateAdmin(): Boolean{
    println("Enter the passcode")
    val codeInput: String = readln()
    return codeInput == "Aloha"
}
