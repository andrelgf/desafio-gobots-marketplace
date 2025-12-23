package com.gobots.marketplace_service.domain.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_items", schema = "marketplace")
@Access(AccessType.FIELD)
class OrderItem protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    lateinit var order: Order

    @Column(name = "product_name", nullable = false, length = 100)
    lateinit var productName: String

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    lateinit var unitPrice: BigDecimal

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    lateinit var totalAmount: BigDecimal

    constructor(
        order: Order,
        productName: String,
        unitPrice: BigDecimal,
        quantity: Int
    ) : this() {
        require(productName.isNotBlank()) { "productName must not be blank" }
        require(quantity > 0) { "quantity must be > 0" }
        require(unitPrice >= BigDecimal.ZERO) { "unitPrice must be >= 0" }

        this.order = order
        this.productName = productName
        this.unitPrice = unitPrice
        this.quantity = quantity
        this.totalAmount = unitPrice.multiply(quantity.toBigDecimal())
    }

    constructor(
        productName: String,
        quantity: Int,
        unitPrice: BigDecimal,
    ) : this() {
        this.productName = productName
        this.unitPrice = unitPrice
        this.quantity = quantity
        this.totalAmount = unitPrice.multiply(quantity.toBigDecimal())
    }
}
