package ui

import model.enum.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

//The purpose of Interactable is to provide methods for the UI components to easily interact with the user, and it does this by using interface.
interface Interactable {

    //Why? To reduce duplication
    fun prompt(label: String): String {
        print(label)
        return readln().trim()
    }

    //Why? To reduce duplication, and reduce NFE.
    fun readInt(prompt: String): Int {
        while (true) {
            print("$prompt : ")
            try {
                val value = readln().trim().toInt()
                if (value > 0) {
                    return value
                }
                println("Please enter a number greater than zero.")
            } catch (_: NumberFormatException) {
                println("Invalid number. Please enter a whole number greater than zero.")
            }
        }
    }

    //Why? To reduce duplication of code for action: validating product on input.
    fun readProductItemsMap(category: ProductCategory, context: String): MutableMap<String, Int> {
        val productItems = mutableMapOf<String, Int>()

        println("Enter $category items for the $context (blank product ID to stop):")

        while (true) {
            print("  $category ID: ")
            val productId = readln().trim()

            if (productId.isBlank()) {
                if (productItems.isEmpty()) {
                    println("  At least one ${category.toString().lowercase()} item is required. Try again.")
                    continue
                }
                break
            }

            val qty = readInt("Quantity")
            productItems[productId] = (productItems[productId] ?: 0) + qty
        }

        return productItems
    }

    //Why? To reduce duplication and validate date input.
    fun readDate(prompt: String): LocalDate {
        while (true) {
            print(prompt)
            try {
                return LocalDate.parse(readln().trim())
            } catch (_: DateTimeParseException) {
                println("Invalid date. Please use the format yyyy-MM-dd.")
            }
        }
    }

    //Why? To reduce duplication, and validate decimal input
    fun readBigDecimal(prompt: String): BigDecimal {
        while (true) {
            print(prompt)
            try {
                val value = BigDecimal(readln().trim())

                if (value <= BigDecimal.ZERO) {
                    println("Please enter a number greater than zero.")
                    continue
                }

                return value
            } catch (_: NumberFormatException) {
                println("Invalid number. Please enter a number greater than zero.")
            }
        }
    }

    //Why? To reduce duplication, and for better UserX
    fun <T : Enum<T>> readEnum(clazz: Class<T>, label: String): T {
        val constants = clazz.enumConstants

        println("$label options:")
        constants.forEachIndexed { index, value ->
            println("  ${index + 1}. $value")
        }

        while (true) {
            print("Choose (1-${constants.size}): ")

            try {
                val choice = readln().trim().toInt()
                if (choice in 1..constants.size) {
                    return constants[choice - 1]
                }
            } catch (_: NumberFormatException) {
                println("Invalid choice.")
            }

        }
    }
}