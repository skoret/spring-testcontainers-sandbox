package testcontainers.demo

import mu.KotlinLogging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import testcontainers.demo.entities.User
import testcontainers.demo.repositories.UsersRepository
import testcontainers.demo.services.EmailService
import java.time.Duration

@Testcontainers
@SpringBootTest
class EmailServiceTest @Autowired constructor(
    private val mailer: EmailService,
    private val repository: UsersRepository,
) {
    companion object {
        val log = logger {}

        private const val PORT_SMTP = 1025
        private const val PORT_HTTP = 8025
        private lateinit var SMTP_URL: String
        private const val CHECK_WEB_UI = false
        private val CHECK_WEB_DURATION = Duration.ofSeconds(20)
        private val WAIT_STARTUP_TIMEOUT = Duration.ofSeconds(5)

        class KGenericContainer(image: DockerImageName): GenericContainer<KGenericContainer>(image)

        /**
         * Pull image if needed, configure and run container with mailhog smtp server before application start
         *
         * [github.com/mailhog/MailHog](https://github.com/mailhog/MailHog)
         */
        @Container
        val mailhog = KGenericContainer(DockerImageName.parse("mailhog/mailhog:v1.0.1")).apply {
            log.warn { "------------------------" }
            log.warn { "setup mailhog container" }
            withExposedPorts(PORT_SMTP, PORT_HTTP)
            waitingFor(Wait.forHttp("/").withStartupTimeout(WAIT_STARTUP_TIMEOUT))
        }.also { log.warn { "------------------------" } }

        /**
         * Update mail host and port properties after smtp server is set up in container
         *
         * [docs.spring.io](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-testing-annotation-dynamicpropertysource)
         */
        @JvmStatic
        @DynamicPropertySource
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            log.warn { "------------------------" }
            log.warn { "setup host and port for smtp server" }

            val host = mailhog.host
            val smtp = mailhog.getMappedPort(PORT_SMTP)
            val http = mailhog.getMappedPort(PORT_HTTP)
            registry.add("spring.mail.host") { host }
            registry.add("spring.mail.port") { smtp }
            SMTP_URL = "http://$host:$http"

            log.warn { "\tspring.mail.host = $host" }
            log.warn { "\tspring.mail.port = $smtp" }
            log.warn { "\tmailhog ui: $SMTP_URL" }
            log.warn { "------------------------" }
        }
    }

    @AfterEach
    fun cleanup() {
        if (CHECK_WEB_UI) {
            log.warn { "Go to web ui and see mails, you have only $CHECK_WEB_DURATION" }
            log.warn { "\tmailhog ui: $SMTP_URL" }
            Thread.sleep(CHECK_WEB_DURATION.toMillis())
        }
    }

    @Test
    fun send() {
        log.warn { "------------------------" }
        log.warn { "run test | mailhog ui: $SMTP_URL" }
        val (sender, recipient) = users()
        val (subject, message) = "Test Subject" to "Hello, Testcontainers!"
        mailer.send(sender.id, recipient.id, subject, message)

        val (count, mails) = mails("from", sender.email)

        assertThat(count).isGreaterThan(0)
        assertThat(mails)
            .anySatisfy { (content, from, to) ->
                assertThat(content.Body).isEqualTo(message)
                assertThat(content.Headers).extractingByKey("Subject").asList().contains(subject)
                assertThat("${from.Mailbox}@${from.Domain}").isEqualTo(sender.email)
                assertThat(to).anySatisfy { (domain, mailbox) ->
                    assertThat("${mailbox}@${domain}").isEqualTo(recipient.email)
                }
            }
    }

    private fun users(): Pair<User, User> {
        if (repository.count() < 2)
            fail("Not enough users entries in db, check your setup")
        val users = repository.findAll().toList()
        return users[0] to users[1]
    }

    private fun mails(kind: String, query: String): Mails {
        log.warn { "retrieve mails from mailhog container" }
        val template = RestTemplate()
        return template.getForObject("$SMTP_URL/api/v2/search?kind=$kind&query=$query")
    }

    private data class Mails(
        val count: Int,
        val items: List<Mail>,
    )

    private data class Mail(
        val Content: Content,
        val From: Participant,
        val To: List<Participant>,
    )

    private data class Content(
        val Body: String,
        val Headers: Map<String, List<String>>,
    )

    private data class Participant(
        val Domain: String,
        val Mailbox: String,
    )
}