package me.proton.core.plan.test.robot

public data class BillingCycle(
    val value: String,
    val monthlyPrice: Double
) {
    public companion object {
        // Predefined payment period values
        public const val PAY_ANNUALLY: String = "Pay annually"
        public const val PAY_MONTHLY: String = "Pay monthly"
    }
}

public data class Plan(
    val id: String,
    val name: String,
    val billingCycle: BillingCycle
) {

    override fun toString(): String {
        return "$name, ${billingCycle.value}" // Shows only Plan name in parametrized test
    }

    public companion object {
        // Predefined Billing Cycles
        public val Free: Plan =
            Plan(
                "Free", "Free",
                BillingCycle(
                    BillingCycle.PAY_MONTHLY,
                    0.0
                )
            )
    }
}
