/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.jtillserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.davidg95.JTill.jtill.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class HttpController {

    private static final Logger LOG = Logger.getLogger(HttpController.class.getName());

    public static final int PORT = 52342;

    private final DataConnect dc;

    HttpServer server;

    public HttpController() {
        dc = DataConnect.get();
    }

    public void start() throws IOException {
        LOG.log(Level.INFO, "Starting HTTP server on port " + PORT);
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/products", new ProductsHandler());
        server.createContext("/departments", new DepartmentsHandler());
        server.start();
        LOG.log(Level.INFO, "HTTP server started");
    }

    public void stop() {
        LOG.log(Level.INFO, "Stopping HTTP server");
        server.stop(1);
    }

    public void restart() throws IOException {
        stop();
        start();
    }

    public class DepartmentsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "";
            LOG.info("Received request on /departments");
            if (he.getRequestMethod().equalsIgnoreCase("GET")) {
                Map<String, String> params = getParameters(he);
                if (params.containsKey("id")) {
                    try {
                        Department d = dc.getDepartment(Integer.parseInt(params.get("id")));
                        JSONObject department = new JSONObject();
                        department.put("id", d.getId());
                        department.put("name", d.getName());
                        response = department.toString();
                        he.sendResponseHeaders(200, response.length());
                    } catch (SQLException | JTillException ex) {
                        response = "Not found";
                        he.sendResponseHeaders(404, response.length());
                    } catch (JSONException ex) {
                        Logger.getLogger(HttpController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    JSONArray departments = new JSONArray();
                    try {
                        for (Department d : dc.getAllDepartments()) {
                            JSONObject department = new JSONObject();
                            department.put("id", d.getId());
                            department.put("name", d.getName());
                            departments.put(department);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(HttpController.class.getName()).log(Level.SEVERE, null, ex);
                        he.sendResponseHeaders(404, response.length());
                    } catch (JSONException ex) {
                        Logger.getLogger(HttpController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    response = departments.toString();
                    he.sendResponseHeaders(200, response.length());
                }
            } else if (he.getRequestMethod().equalsIgnoreCase("POST")) {

            }
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            he.getResponseHeaders().add("Content-Type", "application/json");
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

    }

    public class ProductsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "";
            LOG.info("Received request on /products");
            if (he.getRequestMethod().equalsIgnoreCase("GET")) {
                Map<String, String> params = getParameters(he);
                if (params.containsKey("barcode")) {
                    try {
                        Product p = dc.getProduct(params.get("barcode"));
                        JSONObject product = getProduct(p);
                        response = product.toString();
                        he.sendResponseHeaders(200, response.length());
                    } catch (ProductNotFoundException | SQLException ex) {
                        response = "Product not found";
                        he.sendResponseHeaders(404, response.length());
                    }
                } else {
                    JSONArray products = new JSONArray();
                    try {
                        for (Product p : dc.getAllProducts()) {
                            JSONObject product = getProduct(p);
                            products.put(product);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(HttpController.class.getName()).log(Level.SEVERE, null, ex);
                        he.sendResponseHeaders(404, response.length());
                    }
                    response = products.toString();
                    he.sendResponseHeaders(200, response.length());
                }
            } else if (he.getRequestMethod().equalsIgnoreCase("POST")) {

            }
            he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            he.getResponseHeaders().add("Content-Type", "application/json");
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private JSONObject getProduct(Product p) {
            JSONObject product = new JSONObject();
            try {
                product.put("barcode", p.getBarcode());
                product.put("longName", p.getLongName());
                product.put("shortName", p.getShortName());
                product.put("price", p.getPrice());
                product.put("costPrice", p.getCostPrice());
                product.put("packSize", p.getPackSize());
                product.put("tax", p.getTax().getName());
                product.put("category", p.getCategory().getId());
                product.put("comments", p.getComments());
                product.put("ingredients", p.getIngredients());
                product.put("stock", p.getStock());
                product.put("minStock", p.getMinStockLevel());
                product.put("maxStock", p.getMaxStockLevel());
            } catch (JSONException ex) {
                Logger.getLogger(HttpController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return product;
        }

    }

    private static Map<String, String> getParameters(HttpExchange he) {
        String query = he.getRequestURI().getQuery();
        if (query == null) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    private static Map<String, String> postParameters(HttpExchange httpExchange) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        InputStream inputStream = httpExchange.getRequestBody();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read = 0;
        while ((read = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, read);
        }
        String[] keyValuePairs = byteArrayOutputStream.toString().split("&");
        for (String keyValuePair : keyValuePairs) {
            String[] keyValue = keyValuePair.split("=");
            if (keyValue.length != 2) {
                continue;
            }
            parameters.put(keyValue[0], keyValue[1]);
        }
        return parameters;
    }
}
