package com.rickstaff.app.data;

import com.google.gson.Gson;
import com.rickstaff.app.data.model.Employee;
import com.rickstaff.app.data.remote.EmployeeRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class EmployeeRepositoryTest {

    private MockWebServer server;
    private EmployeeRepository repository;
    private final Gson gson = new Gson();

    private static final long TIMEOUT_SECONDS = 5;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        OkHttpClient testClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build();

        String baseUrl = server.url("/").toString().replaceAll("/$", "");
        repository = new EmployeeRepository(testClient, baseUrl);
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

    private Employee buildEmployee(int id, String nome, String email, String cargo,
                                   double salario, boolean ativo) {
        Employee e = new Employee();
        e.setId(id);
        e.setNome(nome);
        e.setEmail(email);
        e.setCargo(cargo);
        e.setSalario(salario);
        e.setAtivo(ativo);
        return e;
    }

    @Test
    public void getAll_resposta_sucesso_chama_OnSuccess() throws InterruptedException {
        Employee e1 = buildEmployee(1, "Alice", "alice@email.com", "Dev", 5000, true);
        Employee e2 = buildEmployee(2, "Bob",   "bob@email.com",   "QA",  4000, true);
        server.enqueue(jsonResponse(200, gson.toJson(new Employee[]{e1, e2})));

        CountDownLatch latch = new CountDownLatch(1);
        List<Employee>[] result = new List[]{null};

        repository.getAll(new EmployeeRepository.ListCallback() {
            @Override public void onSuccess(List<Employee> list) {
                result[0] = list;
                latch.countDown();
            }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue("Timeout", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(result[0]);
        assertEquals(2, result[0].size());
        assertEquals("Alice", result[0].get(0).getNome());
    }

    @Test
    public void getAll_resposta_sucesso_faz_get_no_endpoint_correto() throws Exception {
        Employee e1 = buildEmployee(1, "Alice", "alice@email.com", "Dev", 5000, true);
        server.enqueue(jsonResponse(200, gson.toJson(new Employee[]{e1})));
        CountDownLatch latch = new CountDownLatch(1);

        repository.getAll(new EmployeeRepository.ListCallback() {
            @Override public void onSuccess(List<Employee> list) { latch.countDown(); }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().endsWith("/api/funcionarios"));
    }

    @Test
    public void getAll_falha_de_conexao_chama_OnError() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.getAll(new EmployeeRepository.ListCallback() {
            @Override public void onSuccess(List<Employee> list) { latch.countDown(); }
            @Override public void onError(String msg) {
                errorMsg[0] = msg;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro de conexão", errorMsg[0]);
    }

    @Test
    public void create_resposta_sucesso_chama_OnSuccess_com_employee_criado()
            throws InterruptedException {
        Employee created = buildEmployee(3, "Carol", "carol@email.com", "Sales", 4500, true);
        server.enqueue(jsonResponse(201, gson.toJson(created)));

        CountDownLatch latch = new CountDownLatch(1);
        Employee[] result = {null};

        repository.create(
                buildEmployee(0, "Carol", "carol@email.com", "Sales", 4500, true),
                new EmployeeRepository.ActionCallback() {
                    @Override public void onSuccess(Employee employee) {
                        result[0] = employee;
                        latch.countDown();
                    }
                    @Override public void onError(String msg) { latch.countDown(); }
                });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(result[0]);
        assertEquals(3, result[0].getId());
        assertEquals("Carol", result[0].getNome());
    }

    @Test
    public void create_resposta_sucesso_faz_post_no_endpoint_correto() throws Exception {
        Employee emp = buildEmployee(0, "Carol", "carol@email.com", "Sales", 4500, true);
        server.enqueue(jsonResponse(201, gson.toJson(
                buildEmployee(3, "Carol", "carol@email.com", "Sales", 4500, true))));
        CountDownLatch latch = new CountDownLatch(1);

        repository.create(emp, new EmployeeRepository.ActionCallback() {
            @Override public void onSuccess(Employee e) { latch.countDown(); }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().endsWith("/api/funcionarios"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("Carol"));
        assertTrue(body.contains("carol@email.com"));
    }

    @Test
    public void create_resposta_erro_http_chama_OnError_com_codigo() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(400));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.create(
                buildEmployee(0, "Eve", "eve@email.com", "Finance", 3000, true),
                new EmployeeRepository.ActionCallback() {
                    @Override public void onSuccess(Employee e) { latch.countDown(); }
                    @Override public void onError(String msg) {
                        errorMsg[0] = msg;
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro: 400", errorMsg[0]);
    }

    @Test
    public void create_falha_de_conexao_chama_OnError() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.create(
                buildEmployee(0, "Dave", "dave@email.com", "HR", 3500, true),
                new EmployeeRepository.ActionCallback() {
                    @Override public void onSuccess(Employee e) { latch.countDown(); }
                    @Override public void onError(String msg) {
                        errorMsg[0] = msg;
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro de conexão", errorMsg[0]);
    }

    @Test
    public void update_resposta_sucesso_chama_OnSuccess_com_employee_atualizado()
            throws InterruptedException {
        Employee updated = buildEmployee(5, "Frank Updated", "frank@email.com", "IT", 6000, true);
        server.enqueue(jsonResponse(200, gson.toJson(updated)));

        CountDownLatch latch = new CountDownLatch(1);
        Employee[] result = {null};

        repository.update(updated, new EmployeeRepository.ActionCallback() {
            @Override public void onSuccess(Employee employee) {
                result[0] = employee;
                latch.countDown();
            }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull(result[0]);
        assertEquals("Frank Updated", result[0].getNome());
    }

    @Test
    public void update_resposta_sucesso_faz_put_no_endpoint_com_id() throws Exception {
        Employee emp = buildEmployee(5, "Frank", "frank@email.com", "IT", 6000, true);
        server.enqueue(jsonResponse(200, gson.toJson(emp)));
        CountDownLatch latch = new CountDownLatch(1);

        repository.update(emp, new EmployeeRepository.ActionCallback() {
            @Override public void onSuccess(Employee e) { latch.countDown(); }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        RecordedRequest request = server.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().endsWith("/api/funcionarios/5"));
    }

    @Test
    public void update_resposta_erro_http_chama_OnError_com_codigo() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(500));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.update(
                buildEmployee(5, "Frank", "frank@email.com", "IT", 6000, true),
                new EmployeeRepository.ActionCallback() {
                    @Override public void onSuccess(Employee e) { latch.countDown(); }
                    @Override public void onError(String msg) {
                        errorMsg[0] = msg;
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro: 500", errorMsg[0]);
    }

    @Test
    public void update_falha_de_conexao_chama_OnError() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.update(
                buildEmployee(5, "Frank", "frank@email.com", "IT", 6000, true),
                new EmployeeRepository.ActionCallback() {
                    @Override public void onSuccess(Employee e) { latch.countDown(); }
                    @Override public void onError(String msg) {
                        errorMsg[0] = msg;
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro de conexão", errorMsg[0]);
    }

    @Test
    public void delete_resposta_204_chama_OnSuccess() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(204));

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] success = {false};

        repository.delete(7, new EmployeeRepository.DeleteCallback() {
            @Override public void onSuccess() {
                success[0] = true;
                latch.countDown();
            }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertTrue(success[0]);
    }

    @Test
    public void delete_resposta_204_faz_delete_no_endpoint_com_Id() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));
        CountDownLatch latch = new CountDownLatch(1);

        repository.delete(7, new EmployeeRepository.DeleteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onError(String msg) { latch.countDown(); }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        RecordedRequest request = server.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().endsWith("/api/funcionarios/7"));
    }

    @Test
    public void delete_resposta_nao_204_chama_OnError() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(404));

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.delete(7, new EmployeeRepository.DeleteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onError(String msg) {
                errorMsg[0] = msg;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro ao excluir", errorMsg[0]);
    }

    @Test
    public void delete_falha_de_conexao_chama_OnError() throws Exception {
        server.shutdown();

        CountDownLatch latch = new CountDownLatch(1);
        String[] errorMsg = {null};

        repository.delete(7, new EmployeeRepository.DeleteCallback() {
            @Override public void onSuccess() { latch.countDown(); }
            @Override public void onError(String msg) {
                errorMsg[0] = msg;
                latch.countDown();
            }
        });

        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Erro de conexão", errorMsg[0]);
    }
}