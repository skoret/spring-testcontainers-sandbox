package testcontainers.demo.services

import mu.KotlinLogging.logger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import testcontainers.demo.entities.User
import testcontainers.demo.repositories.UsersRepository

@Service
class EmailService(
    private val mailer: JavaMailSender,
    private val repository: UsersRepository,
) {
    private val log = logger {}

    fun send(from: Long, to: Long, subject: String, text: String) {
        val sender = repository.findByIdOrNull(from)
            ?: throw NoSuchElementException("User not found, id=$from")
        val recipient = repository.findByIdOrNull(to)
            ?: throw NoSuchElementException("User not found, id=$to")
        send(sender, recipient, subject, text)
    }

    private fun send(from: User, to: User, subject: String, text: String) {
        val message = mailer.createMimeMessage()
        MimeMessageHelper(message, Charsets.UTF_8.name()).apply {
            setTo(to.email)
            setFrom(from.email, "${from.name} ${from.surname}")
            setText(text)
            setSubject(subject)
        }
        mailer.send(message)

        log.warn { "------------------------" }
        log.warn { "Letter sent successfully" }
        log.warn { "\t   from: $from" }
        log.warn { "\t     to: $to" }
        log.warn { "\tsubject: '$subject'" }
        log.warn { "------------------------" }
    }
}
