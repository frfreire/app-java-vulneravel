package br.unicesumar.seg.server;

import br.unicesumar.seg.controller.ApiController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleHttpServer {

    private final int port;
    private HttpServer server;
    private final ApiController apiController;

    // VULNERABILIDADE: Informações sensíveis expostas
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String DEBUG_MODE = "true";
    private static final String SECRET_KEY = "super-secret-key-12345";

    public SimpleHttpServer(int port) {
        this.port = port;
        this.apiController = new ApiController();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // VULNERABILIDADE: Rota de debug exposta
        server.createContext("/debug", new DebugHandler());
        server.createContext("/admin", new AdminHandler());

        // Rotas normais
        server.createContext("/", new HomeHandler());
        server.createContext("/api/users", new ApiHandler());

        // VULNERABILIDADE: Endpoint que expõe informações do sistema
        server.createContext("/system", new SystemInfoHandler());

        server.setExecutor(null);
        server.start();

        // VULNERABILIDADE: Log com informações sensíveis
        System.out.println("Servidor iniciado na porta " + port);
        System.out.println("DEBUG MODE: " + DEBUG_MODE);
        System.out.println("Admin password: " + ADMIN_PASSWORD);
        System.out.println("Secret key: " + SECRET_KEY);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // VULNERABILIDADE: Handler de debug que expõe informações sensíveis
    class DebugHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("<h1>Debug Information</h1>");
            debugInfo.append("<p><b>Admin Password:</b> ").append(ADMIN_PASSWORD).append("</p>");
            debugInfo.append("<p><b>Secret Key:</b> ").append(SECRET_KEY).append("</p>");
            debugInfo.append("<p><b>Java Version:</b> ").append(System.getProperty("java.version")).append("</p>");
            debugInfo.append("<p><b>OS:</b> ").append(System.getProperty("os.name")).append("</p>");
            debugInfo.append("<p><b>User:</b> ").append(System.getProperty("user.name")).append("</p>");
            debugInfo.append("<p><b>Home:</b> ").append(System.getProperty("user.home")).append("</p>");

            // VULNERABILIDADE: Permite execução de comandos via parâmetro
            if (params.containsKey("cmd")) {
                String comando = params.get("cmd");
                debugInfo.append("<h2>Command Output:</h2>");
                debugInfo.append("<pre>").append(executarComando(comando)).append("</pre>");
            }

            // VULNERABILIDADE: Permite leitura de arquivos via parâmetro
            if (params.containsKey("file")) {
                String arquivo = params.get("file");
                debugInfo.append("<h2>File Content:</h2>");
                debugInfo.append("<pre>").append(lerArquivo(arquivo)).append("</pre>");
            }

            sendResponse(exchange, 200, debugInfo.toString(), "text/html");
        }
    }

    // VULNERABILIDADE: Painel admin sem autenticação
    class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <h1>Admin Panel</h1>
                <p>Bem-vindo ao painel administrativo!</p>
                <h2>Ações disponíveis:</h2>
                <ul>
                    <li><a href="/debug?cmd=whoami">Ver usuário atual</a></li>
                    <li><a href="/debug?cmd=dir">Listar diretório</a></li>
                    <li><a href="/debug?file=/etc/passwd">Ver arquivo passwd</a></li>
                    <li><a href="/debug?file=C:\\Windows\\System32\\drivers\\etc\\hosts">Ver arquivo hosts</a></li>
                </ul>
                <h2>Informações do Sistema:</h2>
                <p>Admin Password: """ + ADMIN_PASSWORD + """
                </p>
                <p>Secret Key: """ + SECRET_KEY + """
                </p>
                """;
            sendResponse(exchange, 200, response, "text/html");
        }
    }

    // VULNERABILIDADE: Endpoint que expõe informações do sistema
    class SystemInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String systemInfo = apiController.getSystemInfo();
            sendResponse(exchange, 200, systemInfo, "application/json");
        }
    }

    // Handler para página inicial
    class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = createHomePage();
            sendResponse(exchange, 200, response, "text/html");
        }
    }

    // Handler para API com vulnerabilidades
    class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            // VULNERABILIDADE: Log de todos os requests
            System.out.println("Request: " + method + " " + path + " Query: " + query);
            System.out.println("Headers: " + exchange.getRequestHeaders().toString());

            String response;
            int statusCode = 200;

            try {
                switch (method) {
                    case "GET":
                        if (path.equals("/api/users")) {
                            response = apiController.getAllUsers();
                        } else if (path.startsWith("/api/users/") && path.contains("/email/")) {
                            String email = path.substring(path.lastIndexOf("/") + 1);
                            // VULNERABILIDADE: Não decodifica URL
                            response = apiController.getUserByEmail(email);
                        } else if (path.startsWith("/api/users/")) {
                            String idStr = path.substring(path.lastIndexOf("/") + 1);
                            try {
                                Long id = Long.parseLong(idStr);
                                response = apiController.getUserById(id);
                            } catch (NumberFormatException e) {
                                // VULNERABILIDADE: Exposição de stack trace
                                response = "{\"erro\":\"ID inválido: " + e.getMessage() + "\"}";
                                statusCode = 400;
                            }
                        } else if (path.equals("/api/users") && query != null && query.startsWith("termo=")) {
                            String termo = query.substring(6);
                            // VULNERABILIDADE: Não decodifica URL
                            response = apiController.searchUsers(termo);
                        } else {
                            response = "{\"erro\":\"Endpoint não encontrado\"}";
                            statusCode = 404;
                        }
                        break;

                    case "POST":
                        if (path.equals("/api/users")) {
                            String body = readRequestBody(exchange);
                            response = apiController.createUser(body);
                            statusCode = 201;
                        } else {
                            response = "{\"erro\":\"Endpoint não encontrado\"}";
                            statusCode = 404;
                        }
                        break;

                    case "PUT":
                        if (path.startsWith("/api/users/")) {
                            String idStr = path.substring(path.lastIndexOf("/") + 1);
                            Long id = Long.parseLong(idStr);
                            String body = readRequestBody(exchange);
                            response = apiController.updateUser(id, body);
                        } else {
                            response = "{\"erro\":\"Endpoint não encontrado\"}";
                            statusCode = 404;
                        }
                        break;

                    case "DELETE":
                        if (path.startsWith("/api/users/")) {
                            String idStr = path.substring(path.lastIndexOf("/") + 1);
                            Long id = Long.parseLong(idStr);
                            response = apiController.deleteUser(id);
                        } else {
                            response = "{\"erro\":\"Endpoint não encontrado\"}";
                            statusCode = 404;
                        }
                        break;

                    default:
                        response = "{\"erro\":\"Método não suportado\"}";
                        statusCode = 405;
                }
            } catch (Exception e) {
                // VULNERABILIDADE: Exposição completa de stack trace
                response = "{\"erro\":\"" + e.getMessage() + "\", \"stackTrace\":\"" +
                        java.util.Arrays.toString(e.getStackTrace()) + "\"}";
                statusCode = 500;

                // VULNERABILIDADE: Log completo do erro
                System.err.println("Erro completo:");
                e.printStackTrace();
            }

            sendResponse(exchange, statusCode, response, "application/json");
        }
    }

    // VULNERABILIDADE: Execução de comandos sem validação
    private String executarComando(String comando) {
        try {
            Process process = Runtime.getRuntime().exec(comando);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        } catch (IOException e) {
            return "Erro ao executar comando: " + e.getMessage();
        }
    }

    // VULNERABILIDADE: Leitura de arquivos sem validação
    private String lerArquivo(String caminho) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(caminho)));
        } catch (IOException e) {
            return "Erro ao ler arquivo: " + e.getMessage();
        }
    }

    // VULNERABILIDADE: Parser de query simples e inseguro
    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    // VULNERABILIDADE: Não decodifica URL
                    params.put(kv[0], kv[1]);
                }
            }
        }
        return params;
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        // VULNERABILIDADE: Headers permissivos demais
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
        exchange.getResponseHeaders().set("Server", "VulnerableServer/1.0");
        exchange.getResponseHeaders().set("X-Powered-By", "Java-Vulnerable-App");

        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String createHomePage() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>UNICESUMAR - Aplicação Vulnerável</title>
                    <meta charset="UTF-8">
                    <style>
                        body { 
                            font-family: Arial, sans-serif; 
                            margin: 40px; 
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: #333;
                        }
                        .container { 
                            max-width: 1000px; 
                            margin: 0 auto; 
                            background: white; 
                            padding: 40px; 
                            border-radius: 15px; 
                            box-shadow: 0 10px 30px rgba(0,0,0,0.2); 
                        }
                        h1 { color: #667eea; text-align: center; }
                        .warning { 
                            background: #ff4757; 
                            color: white; 
                            padding: 15px; 
                            border-radius: 8px; 
                            margin: 20px 0; 
                            text-align: center;
                            font-weight: bold;
                        }
                        .university { color: #764ba2; text-align: center; font-size: 1.2em; margin: 20px 0; }
                        .tech { background: #e74c3c; color: white; padding: 8px 15px; border-radius: 15px; display: inline-block; }
                        .stats { background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0; }
                        .api-link { 
                            background: #667eea; 
                            color: white; 
                            padding: 10px 15px; 
                            text-decoration: none; 
                            border-radius: 5px; 
                            display: inline-block; 
                            margin: 5px; 
                        }
                        .danger-link {
                            background: #ff4757;
                            color: white;
                            padding: 10px 15px;
                            text-decoration: none;
                            border-radius: 5px;
                            display: inline-block;
                            margin: 5px;
                        }
                        .endpoints { background: #fff; border: 1px solid #ddd; padding: 20px; margin: 20px 0; border-radius: 5px; }
                        pre { background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 5px; overflow-x: auto; }
                        .vulnerabilities {
                            background: #ffe8e8;
                            border: 2px solid #ff4757;
                            padding: 20px;
                            border-radius: 8px;
                            margin: 20px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <h1>🚨 Aplicação Java VULNERÁVEL</h1>
                        <div class='university'>🏛️ UNICESUMAR - Segurança da Informação</div>
                        
                        <div class='warning'>
                            ⚠️ ATENÇÃO: Esta aplicação contém vulnerabilidades INTENCIONAIS para fins educacionais!
                        </div>
                        
                        <div style='text-align: center;'>
                            <span class='tech'>Java """ + System.getProperty("java.version") + """
                            </span>
                        </div>
                        
                        <div class='vulnerabilities'>
                            <h3>🔴 VULNERABILIDADES INCLUÍDAS:</h3>
                            <ul>
                                <li>🔓 <strong>Credenciais Hard-coded</strong></li>
                                <li>💉 <strong>SQL Injection</strong></li>
                                <li>🗂️ <strong>Path Traversal</strong></li>
                                <li>⚡ <strong>Command Injection</strong></li>
                                <li>📝 <strong>Information Disclosure</strong></li>
                                <li>🔑 <strong>Weak Authentication</strong></li>
                                <li>📊 <strong>Sensitive Data Exposure</strong></li>
                                <li>🔍 <strong>Insecure Deserialization</strong></li>
                                <li>⏳ <strong>ReDoS (Regular Expression DoS)</strong></li>
                                <li>🗢 <strong>XXE (XML External Entity)</strong></li>
                            </ul>
                        </div>
                        
                        <div class='stats'>
                            <h3>🎯 Endpoints para Teste:</h3>
                            <h4>📋 API Normal:</h4>
                            <p><a href='/api/users' class='api-link'>GET /api/users</a> - Listar usuários</p>
                            <p><a href='/api/users?termo=João' class='api-link'>GET /api/users?termo=João</a> - Buscar usuários</p>
                            
                            <h4>🚨 Endpoints Vulneráveis:</h4>
                            <p><a href='/debug' class='danger-link'>GET /debug</a> - Debug Info (Information Disclosure)</p>
                            <p><a href='/admin' class='danger-link'>GET /admin</a> - Admin Panel (No Auth)</p>
                            <p><a href='/system' class='danger-link'>GET /system</a> - System Info</p>
                            <p><a href='/debug?cmd=whoami' class='danger-link'>Command Injection</a></p>
                            <p><a href='/debug?file=/etc/passwd' class='danger-link'>Path Traversal</a></p>
                        </div>

                        <h3>📝 Exemplos de Teste:</h3>
                        
                        <h4>1. Command Injection:</h4>
                        <pre>curl "http://localhost:8080/debug?cmd=whoami"
curl "http://localhost:8080/debug?cmd=ls"</pre>

                        <h4>2. Path Traversal:</h4>
                        <pre>curl "http://localhost:8080/debug?file=/etc/passwd"
curl "http://localhost:8080/debug?file=../../../etc/hosts"</pre>

                        <h4>3. Information Disclosure:</h4>
                        <pre>curl http://localhost:8080/debug
curl http://localhost:8080/system</pre>

                        <h4>4. SQL Injection (simulado):</h4>
                        <pre>curl "http://localhost:8080/api/users?termo=' OR 1=1 --"</pre>

                        <div style='text-align: center; margin-top: 40px; color: #666;'>
                            <p>🔥 Use esta aplicação para testar ferramentas como SonarQube, OWASP ZAP, etc.</p>
                            <p>⚠️ NUNCA execute em ambiente de produção!</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}