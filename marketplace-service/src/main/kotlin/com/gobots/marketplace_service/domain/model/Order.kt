package com.gobots.marketplace_service.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders", schema = "marketplace")
@Access(AccessType.FIELD)
class Order protected constructor() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "store_code", nullable = false, length = 50)
    lateinit var storeCode: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: OrderStatus = OrderStatus.CREATED

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val itemsInternal: MutableList<OrderItem> = mutableListOf()

    val items: List<OrderItem>
        get() = itemsInternal

    constructor(storeCode: String) : this() {
        require(storeCode.isNotBlank()) { "storeCode must not be blank" }
        this.storeCode = storeCode
        this.status = OrderStatus.CREATED
        this.totalAmount = BigDecimal.ZERO
    }

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    fun addItem(productName: String, unitPrice: BigDecimal, quantity: Int) {
        require(productName.isNotBlank()) { "productName must not be blank" }
        require(quantity > 0) { "quantity must be > 0" }
        require(unitPrice >= BigDecimal.ZERO) { "unitPrice must be >= 0" }

        val item = OrderItem(
            order = this,
            productName = productName,
            unitPrice = unitPrice,
            quantity = quantity
        )

        itemsInternal.add(item)
        totalAmount = totalAmount.add(item.totalAmount)
    }

    fun removeItemById(itemId: Long) {
        val item = itemsInternal.firstOrNull { it.id == itemId } ?: return
        itemsInternal.remove(item)
        totalAmount = totalAmount.subtract(item.totalAmount)
    }

    fun removeItem(item: OrderItem) {
        val removed = itemsInternal.remove(item)
        if (!removed) return
        totalAmount = totalAmount.subtract(item.totalAmount)
    }
}

