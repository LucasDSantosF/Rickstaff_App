package com.rickstaff.app.data;

import com.rickstaff.app.data.model.CharacterResponse;
import com.rickstaff.app.data.remote.CharacterRepository;

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

public class CharacterRepositoryTest {

    private MockWebServer server;
    private CharacterRepository repository;

    private static final long TIMEOUT_SECONDS = 3;

    private static final String RESPONSE_COM_RESULTADOS = "{"
            + "\"info\":{\"count\":2,\"pages\":1,\"next\":null,\"prev\":null},"
            + "\"results\":["
            + "  {\"id\":1,\"name\":\"Rick Sanchez\",\"status\":\"Alive\",\"species\":\"Human\",\"gender\":\"Male\"},"
            + "  {\"id\":2,\"name\":\"Morty Smith\",\"status\":\"Alive\",\"species\":\"Human\",\"gender\":\"Male\"}"
            + "]}";

    private static final String RESPONSE_VAZIA = "{"
            + "\"info\":{\"count\":0,\"pages\":0,\"next\":null,\"prev\":null},"
            + "\"results\":[]}";

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        OkHttpClient testClient = new OkHttpClient.Builder().build();
        String baseUrl = server.url("/api/character").toString();

        repository = new CharacterRepository(testClient, baseUrl);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    private MockResponse jsonResponse(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Test
    public void getCharacters_resposta_sucesso_chama_OnSuccess() throws InterruptedException {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));

        CountDownLatch latch = new CountDownLatch(1);
        CharacterResponse[] result = {null};

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse response) {
                result[0] = response;
                latch.countDown();
            }
            @Override public void onError(String message) { latch.countDown(); }
        });

        assertTrue("Timeout", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(result[0]);
        assertEquals(2, result[0].getResults().size());
    }

    @Test
    public void getCharacters_sucesso_parsear_nome_corretamente() throws InterruptedException {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));

        CountDownLatch latch = new CountDownLatch(1);
        CharacterResponse[] result = {null};

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { result[0] = r; latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Rick Sanchez", result[0].getResults().get(0).getName());
    }

    @Test
    public void getCharacters_sucesso_envia_query_page_correta() throws Exception {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getCharacters(3, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().contains("page=3"));
    }

    @Test
    public void getCharacters_com_filtro_status_envia_query_status() throws Exception {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getCharacters(1, "alive", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertNotNull(request.getPath());
        assertTrue(request.getPath().contains("status=alive"));
    }

    @Test
    public void getCharacters_com_filtro_gender_envia_query_gender() throws Exception {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getCharacters(1, "", "male", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertNotNull(request.getPath());
        assertTrue(request.getPath().contains("gender=male"));
    }

    @Test
    public void getCharacters_com_filtro_species_envia_query_species() throws Exception {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getCharacters(1, "", "", "human", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertNotNull(request.getPath());
        assertTrue(request.getPath().contains("species=human"));
    }

    @Test
    public void getCharacters_filtros_vazios_nao_envia_query_de_filtro() throws Exception {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        assertNotNull(request.getPath());
        assertFalse(request.getPath().contains("status="));
        assertFalse(request.getPath().contains("gender="));
        assertFalse(request.getPath().contains("species="));
    }

    @Test
    public void getCharacters_todos_filtros_envia_todas_as_queries() throws Exception {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getCharacters(2, "alive", "male", "human", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String m) { latch.countDown(); }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        RecordedRequest request = server.takeRequest();
        String path = request.getPath();
        assertNotNull(path);
        assertTrue(path.contains("page=2"));
        assertTrue(path.contains("status=alive"));
        assertTrue(path.contains("gender=male"));
        assertTrue(path.contains("species=human"));
    }

    @Test
    public void getCharacters_resposta_erro_404_chama_OnError_com_mensagem_padrao() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(404));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Nenhum resultado encontrado", errorMsg[0]);
    }

    @Test
    public void getCharacters_resposta_erro_500_chama_OnError() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(500));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Nenhum resultado encontrado", errorMsg[0]);
    }

    @Test
    public void getCharacters_sucesso_nao_emite_erro() throws InterruptedException {
        server.enqueue(jsonResponse(200, RESPONSE_COM_RESULTADOS));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNull(errorMsg[0]);
    }
    @Test
    public void getCharacters_falha_de_conexao_chama_OnError_conexao() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.getCharacters(1, "", "", "", new CharacterRepository.CharacterCallback() {
            @Override public void onSuccess(CharacterResponse r) { latch.countDown(); }
            @Override public void onError(String message) {
                errorMsg[0] = message;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro de conexão", errorMsg[0]);
    }
}