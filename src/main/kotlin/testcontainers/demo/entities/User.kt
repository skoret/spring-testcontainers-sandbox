package testcontainers.demo.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * According to [Spring Boot tutorial for Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/#_persistence_with_jpa):
 * > Here we don’t use `data` classes with `val` properties because JPA is not designed to work with
 * immutable classes or the methods generated automatically by `data` classes. If you are using
 * other Spring Data flavor, most of them are designed to support such constructs so you
 * should use classes like `data class User(val login: String, …)` when using Spring Data MongoDB, Spring Data JDBC, etc.
 */
@Entity
class User(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var surname: String,
    @Column(nullable = false)
    var email: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
) {
    override fun toString() = "User(#$id | $name $surname | $email)"
}
