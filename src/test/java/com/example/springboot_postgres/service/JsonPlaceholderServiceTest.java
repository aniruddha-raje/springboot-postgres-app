package com.example.springboot_postgres.service;

import com.example.springboot_postgres.client.JsonPlaceholderFeignClient;
import com.example.springboot_postgres.config.AppProperties;
import com.example.springboot_postgres.dto.Post;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Unit tests for {@link JsonPlaceholderService}. Each of its three HTTP
 * clients is stubbed so the tests are fully offline and deterministic:
 * <ul>
 *   <li>{@link RestClient} and {@link RestTemplate} are each bound to a
 *       {@link MockRestServiceServer}, which intercepts requests and returns
 *       canned responses.</li>
 *   <li>The Feign client is just an interface, so it is a Mockito mock.</li>
 * </ul>
 */
class JsonPlaceholderServiceTest {

    private static final String BASE = "https://jsonplaceholder.typicode.com/posts";

    private MockRestServiceServer server;          // RestClient calls
    private MockRestServiceServer templateServer;  // RestTemplate call
    private JsonPlaceholderFeignClient feignClient;
    private JsonPlaceholderService service;

    @BeforeEach
    void setUp() {
        // Mirror the production factory so PATCH is exercised the same way.
        RestClient.Builder builder = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory())
                .baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        RestTemplate restTemplate = new RestTemplate(new JdkClientHttpRequestFactory());
        templateServer = MockRestServiceServer.createServer(restTemplate);

        feignClient = mock(JsonPlaceholderFeignClient.class);

        AppProperties properties = new AppProperties();
        properties.setJsonPlaceholderUrl(BASE);
        service = new JsonPlaceholderService(restClient, restTemplate, feignClient, properties);
    }

    // ------------------------------------------------------ OpenFeign call

    @Test
    void getPostsByUser_delegatesToFeignClient() {
        when(feignClient.getPostsByUser(1L)).thenReturn(List.of(new Post(1L, 1L, "t1", "b1")));

        List<Post> posts = service.getPostsByUser(1L);

        assertEquals(1, posts.size());
        assertEquals(1L, posts.get(0).userId());
    }

    @Test
    void getPostsByUser_whenUpstream500_throwsBadGateway() {
        when(feignClient.getPostsByUser(1L)).thenThrow(
                new FeignException.InternalServerError("boom", feignRequest(), null, Map.of()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getPostsByUser(1L));
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    @Test
    void getPostsByUser_whenNoResponse_throwsBadGateway() {
        // Connection failures surface as status -1, which is not a valid HTTP status.
        when(feignClient.getPostsByUser(1L)).thenThrow(
                new RetryableException(-1, "timeout", Request.HttpMethod.GET, (Long) null, feignRequest()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getPostsByUser(1L));
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

    private static Request feignRequest() {
        return Request.create(Request.HttpMethod.GET, BASE, Map.of(), null, StandardCharsets.UTF_8, null);
    }

    // --------------------------------------------------- RestTemplate call

    @Test
    void getPost_whenFound_returnsPost() {
        templateServer.expect(requestTo(BASE + "/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"id\":1,\"title\":\"t1\",\"body\":\"b1\"}",
                        MediaType.APPLICATION_JSON));

        Post post = service.getPost(1L);

        assertNotNull(post);
        assertEquals(1L, post.id());
        templateServer.verify();
    }

    @Test
    void getPost_whenUpstream404_throwsNotFound() {
        templateServer.expect(requestTo(BASE + "/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getPost(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        templateServer.verify();
    }

    // ----------------------------------------------------- RestClient calls

    @Test
    void getAllPosts_parsesArray() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"userId\":1,\"id\":1,\"title\":\"t1\",\"body\":\"b1\"}," +
                                "{\"userId\":1,\"id\":2,\"title\":\"t2\",\"body\":\"b2\"}]",
                        MediaType.APPLICATION_JSON));

        List<Post> posts = service.getAllPosts();

        assertEquals(2, posts.size());
        assertEquals("t1", posts.get(0).title());
        server.verify();
    }

    @Test
    void getAllPosts_whenUpstream500_throwsBadGateway() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getAllPosts());
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
        server.verify();
    }

    @Test
    void createPost_sendsBodyAndReturnsCreated() {
        server.expect(requestTo(BASE))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.title").value("new title"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body("{\"userId\":1,\"id\":101,\"title\":\"new title\",\"body\":\"new body\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        Post created = service.createPost(new Post(1L, null, "new title", "new body"));

        assertEquals(101L, created.id());
        server.verify();
    }

    @Test
    void replacePost_issuesPut() {
        server.expect(requestTo(BASE + "/1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.title").value("replaced"))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"id\":1,\"title\":\"replaced\",\"body\":\"b\"}",
                        MediaType.APPLICATION_JSON));

        Post result = service.replacePost(1L, new Post(1L, 1L, "replaced", "b"));

        assertEquals("replaced", result.title());
        server.verify();
    }

    @Test
    void patchPost_issuesPatchWithPartialBody() {
        server.expect(requestTo(BASE + "/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(jsonPath("$.title").value("patched"))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"id\":1,\"title\":\"patched\",\"body\":\"b1\"}",
                        MediaType.APPLICATION_JSON));

        Post result = service.patchPost(1L, Map.of("title", "patched"));

        assertEquals("patched", result.title());
        assertEquals("b1", result.body());
        server.verify();
    }

    @Test
    void deletePost_issuesDelete() {
        server.expect(requestTo(BASE + "/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        service.deletePost(1L);

        server.verify();
    }
}
