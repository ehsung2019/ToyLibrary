package com.toylibrary.app

data class Toy(
    val name: String,
    val age: String,
    val imageRes: Int? = null,     // for built-in toys
    val imageUri: String? = null,  // for user uploaded toys
    val available: Boolean = true,
    val isNew: Boolean = false,
    val description: String,
    var borrowedAt: Long? = null
)

val toyList = listOf(
    Toy("LEGO Set", "Ages 6+", R.drawable.lego, description = "A creative LEGO building set that enhances problem-solving and imagination."),
    Toy("Teddy Bear", "Ages 3+", R.drawable.teddy, description = "A soft and cuddly teddy bear perfect for comforting children and encouraging imaginative play. Made with child-safe materials and suitable for all ages."),
    Toy("Red Fire Truck", "Ages 4+", R.drawable.fire_truck, description = "A bright and durable fire truck toy that inspires imaginative rescue play. Features rolling wheels and realistic details to spark creativity in young children."),
    Toy("Robot Toy", "Ages 8+", R.drawable.robot, isNew = true, description = "A fun and interactive robot toy that encourages hands-on play and curiosity. Designed to spark interest in technology and engineering."),
    Toy("Doll House", "Ages 3+", R.drawable.doll_house, description = "A charming doll house that inspires imaginative play and storytelling. Designed to help children create everyday life scenarios and social interactions."),
    Toy("Xylophone", "Ages 2+", R.drawable.xylophone, description = "A colorful xylophone toy that introduces children to music through playful sound exploration. Helps develop rhythm and hand-eye coordination.")
)

data class Loan(val name: String, val due: String)

val categories = listOf("STEM", "Board", "Outdoor", "Education")

val sampleLoans = listOf(
    Loan("Fire Truck", "Jan 12"),
    Loan("Doll House", "Jan 14")
)