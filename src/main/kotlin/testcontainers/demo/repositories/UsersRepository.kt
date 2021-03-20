package testcontainers.demo.repositories

import org.springframework.data.repository.CrudRepository
import testcontainers.demo.entities.User

interface UsersRepository: CrudRepository<User, Long>
