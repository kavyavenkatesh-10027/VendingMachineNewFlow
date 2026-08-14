package model

import model.enum.Gender
import java.time.LocalDate

//The purpose of User is to provide a template of mandatory data for existing as well as future users (for eg. Admin, Consumer ), and it does this by class(open).
abstract class User(
    val name: String,
    val dob: LocalDate,
    val gender: Gender
) {

    init {
        require(!name.isBlank()) { "Name cannot be empty" }
        require(!dob.isAfter(LocalDate.now())) {"Date of Birth must be on or before the current date" }
    }

    override fun toString(): String =
        "Name : $name\n" +
                "Date of Birth : $dob\n" +
                "Gender : $gender"
}