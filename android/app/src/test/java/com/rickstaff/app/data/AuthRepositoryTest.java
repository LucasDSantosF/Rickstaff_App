package com.rickstaff.app.data;

import com.rickstaff.app.data.model.LoginResponse;
import com.rickstaff.app.data.remote.AuthRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AuthRepositoryTest {

    private MockWebServer server;
    private AuthRepository repository;

    private static final long TIMEOUT_SECONDS = 3;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        OkHttpClient testClient = new OkHttpClient.Builder().build();
        String baseUrl = server.url("/").toString().replaceAll("/$", "");

        repository = new AuthRepository(testClient, baseUrl);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    private MockResponse successResponse() {
        String body = "{\"success\":true,\"token\":\"abc123\",\"message\":null}";
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private MockResponse errorResponse(int code, String message) {
        String body = "{\"success\":false,\"token\":null,\"message\":\"" + message + "\"}";
        return new MockResponse()
                .setResponseCode(code)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private MockResponse errorResponseSemMensagem(int code) {
        String body = "{\"success\":false,\"token\":null}";
        return new MockResponse()
                .setResponseCode(code)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Test
    public void login_resposta_sucesso_chama_OnSuccess() throws InterruptedException {
        server.enqueue(successResponse());

        CountDownLatch latch = new CountDownLatch(1);
        LoginResponse[] result = {null};

        repository.login("user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse response) {
                result[0] = response;
                latch.countDown();
            }
            @Override public void onError(String message) { latch.countDown(); }
        });

        assertTrue("Timeout", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(result[0]);
        assertTrue(result[0].isSuccess());
    }

    @Test
    public void login_resposta_sucesso_envia_dados_corretos() throws Exception {
        server.enqueue(successResponse());
        CountDownLatch latch = new CountDownLatch(1);

        repository.login("user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().endsWith("/api/auth/login"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"email\""));
        assertTrue(body.contains("\"senha\""));
        assertTrue(body.contains("user@email.com"));
    }

    @Test
    public void login_resposta_erro_401_chama_OnError_com_mensagem() throws InterruptedException {
        server.enqueue(errorResponse(401, "Credenciais inválidas"));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.login("user@email.com", "errada", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Credenciais inválidas", errorMsg[0]);
    }

    @Test
    public void login_resposta_erro_sem_mensagem_usa_mensagem_padrao() throws InterruptedException {
        server.enqueue(errorResponseSemMensagem(401));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.login("user@email.com", "errada", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Credenciais inválidas", errorMsg[0]);
    }

    @Test
    public void login_http_200_mas_success_false_chama_OnError() throws InterruptedException {
        String body = "{\"success\":false,\"token\":null,\"message\":\"Conta desativada\"}";
        server.enqueue(new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json").setBody(body));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.login("user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Conta desativada", errorMsg[0]);
    }

    @Test
    public void login_falha_de_conexao_chama_OnError_com_prefixo() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.login("user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(errorMsg[0]);
        assertTrue(errorMsg[0].startsWith("Erro de conexão:"));
    }

    @Test
    public void register_resposta_sucesso_chama_OnSuccess() throws InterruptedException {
        server.enqueue(successResponse());

        CountDownLatch latch = new CountDownLatch(1);
        LoginResponse[] result = {null};

        repository.register("Nome", "user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse response) {
                result[0] = response;
                latch.countDown();
            }
            @Override public void onError(String message) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(result[0]);
        assertTrue(result[0].isSuccess());
    }

    @Test
    public void register_resposta_sucesso_envia_dados_corretos() throws Exception {
        server.enqueue(successResponse());
        CountDownLatch latch = new CountDownLatch(1);

        repository.register("Nome", "user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().endsWith("/api/auth/register"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"nome\""));
        assertTrue(body.contains("\"email\""));
        assertTrue(body.contains("\"senha\""));
        assertTrue(body.contains("Nome"));
        assertTrue(body.contains("user@email.com"));
    }

    @Test
    public void register_resposta_erro_409_chama_OnError_com_mensagem() throws InterruptedException {
        server.enqueue(errorResponse(409, "E-mail já cadastrado"));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.register("Nome", "user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("E-mail já cadastrado", errorMsg[0]);
    }

    @Test
    public void register_resposta_erro_sem_mensagem_usa_mensagem_padrao() throws InterruptedException {
        server.enqueue(errorResponseSemMensagem(500));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.register("Nome", "user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro ao criar usuário.", errorMsg[0]);
    }

    @Test
    public void register_falha_de_conexao_chama_OnError_com_prefixo() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.register("Nome", "user@email.com", "senha123", new AuthRepository.AuthCallback() {
            @Override public void onSuccess(LoginResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(errorMsg[0]);
        assertTrue(errorMsg[0].startsWith("Erro de conexão:"));
    }
}