package model.enum

//The purpose of IndianCurrency is to provide a fixed set of denominations, and it does this by using enum(stateless).
enum class IndianCurrency(val value: Int) {
    ONE(1),
    TWO(2),
    FIVE(5),
    TEN(10),
    TWENTY(20),
    FIFTY(50),
    HUNDRED(100),
    TWO_HUNDRED(200),
    FIVE_HUNDRED(500)
}