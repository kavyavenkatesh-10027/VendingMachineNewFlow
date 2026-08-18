package util

import java.time.LocalDate

fun calculateExpiryDate(manufacturingDate: LocalDate, shelfLifeInMonths: Int) : Boolean{
    val expiryDate = manufacturingDate.plusMonths(shelfLifeInMonths.toLong())
    return LocalDate.now().isAfter(expiryDate)
}