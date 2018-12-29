package wonky.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import wonky.json.JacksonUtil
import wonky.model.Organization

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.*

class ApiControllerSpec extends Specification {
  @Shared
  @AutoCleanup
  EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

  @Shared
  @AutoCleanup
  RxStreamingHttpClient client = embeddedServer.applicationContext.createBean(RxStreamingHttpClient, embeddedServer.getURL())

  def "Getting the organization info"() {
    when:
      HttpRequest request = GET('/v1/organizations/_self').header("Host", "localhost")
      GET('/v1/organizations/_self').header("Host", "localhost")

      HttpResponse<Organization> rsp = client.toBlocking().exchange(request, Argument.of(Organization))

    then: 'the endpoint can be accessed'
      rsp.status == OK
      rsp.body()

    when:
      def body = rsp.body()

    then:
      body.team.name
  }

  def "Fail when Getting the organization info"() {
    when:
      HttpRequest notFound = GET('/v1/organizations/notfound')

      client.toBlocking().exchange(notFound, Argument.of(Map))

    then: 'the endpoint can be accessed'
      HttpClientResponseException notFoundException = thrown(HttpClientResponseException)
      NOT_FOUND == notFoundException.response.status
      def body = notFoundException.response.body()
      body.id
      body.entity
      body.message

    when:
      HttpRequest badToken = GET('/v1/organizations/badtoken')

      client.toBlocking().exchange(badToken, Argument.of(Map))

    then: 'the endpoint can be accessed'
      HttpClientResponseException badTokenException = thrown(HttpClientResponseException)
      INTERNAL_SERVER_ERROR == badTokenException.response.status
      def body1 = badTokenException.response.body()
      body1.error
      body1.message
      body1.slackResponse
  }

  def "invite"() {
    when:
      def prefix = System.getenv("WONKY_TEST_EMAIL_PREFIX")
      assert prefix
      def alphanumeric = RandomStringUtils.randomAlphanumeric(10)
      Invite invite = new Invite(email: "${ prefix }+${ alphanumeric }@gmail.com")

      JacksonUtil jacksonUtil = new JacksonUtil(new ObjectMapper())
      def jsonInvite = jacksonUtil.toJson(invite)

      HttpRequest notFound = POST('/v1/invites', jsonInvite)

      def exchange = client.toBlocking().exchange(notFound, Argument.of(Map))

    then: 'the endpoint can be accessed'

      OK == exchange.status()

  }


}
