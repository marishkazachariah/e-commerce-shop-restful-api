package com.startstepszalando.ecommerceshop.product.controller;

import com.google.gson.*;

import com.startstepszalando.ecommerceshop.config.ApplicationConfig;
import com.startstepszalando.ecommerceshop.config.SecurityConfig;
import com.startstepszalando.ecommerceshop.exception.product.DuplicateProductException;
import com.startstepszalando.ecommerceshop.exception.product.ProductNotFoundException;
import com.startstepszalando.ecommerceshop.exception.user.UserNotFoundException;
import com.startstepszalando.ecommerceshop.jwt.JwtService;
import com.startstepszalando.ecommerceshop.product.dto.ProductRequest;
import com.startstepszalando.ecommerceshop.product.model.Product;
import com.startstepszalando.ecommerceshop.product.repository.ProductRepository;
import com.startstepszalando.ecommerceshop.product.service.ProductService;
import com.startstepszalando.ecommerceshop.user.model.Role;
import com.startstepszalando.ecommerceshop.user.model.User;
import com.startstepszalando.ecommerceshop.user.repository.UserRepository;
import com.startstepszalando.ecommerceshop.user.service.UserImpl;
import com.startstepszalando.ecommerceshop.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProductController.class, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE)})
@Import({SecurityConfig.class, ApplicationConfig.class, UserService.class})
class ProductControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    ProductRepository productRepository;
    @MockBean
    JwtService jwtService;
    @MockBean
    ProductService productService;
    @MockBean
    UserImpl userImpl;
    Gson gson;
    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    public void initTest() {
        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, type, jsonDeserializationContext) ->
                                ZonedDateTime.parse(json.getAsJsonPrimitive()
                                        .getAsString()).toLocalDateTime())
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (localDate, type, jsonSerializationContext) ->
                                new JsonPrimitive(formatter.format(localDate)))
                .create();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenAdminAndValidProduct_Return200OnCreateProduct() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Dark Denim Jeans");
        productRequest.setDescription("long dark jeans");
        productRequest.setPrice(new BigDecimal("79.99"));
        productRequest.setStock(343);
        productRequest.setAdminId(7656L);

        User admin = new User(7656L, "Mary Sunderland", "msunder@example.com", "23343password", Role.ADMIN);
        Product product = new Product(543L, productRequest.getName(), productRequest.getPrice(), productRequest.getDescription(), productRequest.getStock(), admin);

        given(productService.createProduct(any(Product.class), eq(productRequest.getAdminId()))).willReturn(product);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(productRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Dark Denim Jeans")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenProductWithDuplicateNameAndPriceInDB_Return400Error() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Elegant Wristwatch");
        productRequest.setDescription("testing product");
        productRequest.setPrice(BigDecimal.valueOf(199.99));
        productRequest.setStock(5);
        productRequest.setAdminId(50L);

        String errorMessage = "Product with name Elegant Wristwatch and price 5,50 already exists";

        given(productService.createProduct(any(Product.class), eq(productRequest.getAdminId())))
                .willThrow(new DuplicateProductException(errorMessage));

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(productRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(errorMessage)));
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void givenCreateValidProductAsUser_ReturnError403OnCreate() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("product1");
        productRequest.setDescription("testing product");
        productRequest.setPrice(BigDecimal.valueOf(5.50));
        productRequest.setStock(5);
        productRequest.setAdminId(50L);

        mvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(productRequest))
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Access Denied - you don't have permissions for this action")));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenAdminAndInvalidAdminId_Return400Error() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Dark Denim Jeans");
        productRequest.setDescription("long dark jeans");
        productRequest.setPrice(new BigDecimal("79.99"));
        productRequest.setStock(343);
        productRequest.setAdminId(4534543535L);

        given(productService.createProduct(any(Product.class), eq(productRequest.getAdminId())))
                .willThrow(new UserNotFoundException("Invalid userId: user not found"));

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(productRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Invalid userId: user not found")));
    }


    @Test
    void givenNoTokenAndValidProduct_ReturnError403OnCreate() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Dark Denim Jeans");
        productRequest.setDescription("long dark jeans");
        productRequest.setPrice(new BigDecimal("79.99"));
        productRequest.setStock(343);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(productRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenUpdateProductThatIsNotInDB_Return404Error() throws Exception {
        long productId = 1L;
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("product1");
        productRequest.setDescription("testing product");
        productRequest.setPrice(BigDecimal.valueOf(5.50));
        productRequest.setStock(5);
        productRequest.setAdminId(50L);

        String errorMessage = String.format("Product with id %s was not found", productId);

        given(productService.updateProduct(eq(productId), any(Product.class), eq(productRequest.getAdminId())))
                .willThrow(new ProductNotFoundException(errorMessage));

        mvc.perform(put("/api/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(productRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));

        verify(productService).updateProduct(eq(productId), any(Product.class), eq(productRequest.getAdminId()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenUpdateProduct_Return200() throws Exception {
        long productId = 1L;
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("product1");
        productRequest.setDescription("testing product");
        productRequest.setPrice(BigDecimal.valueOf(5.50));
        productRequest.setStock(5);
        productRequest.setAdminId(50L);

        User admin = new User(7656L, "Mary Sunderland", "msunder@example.com", "23343password", Role.ADMIN);
        Product updatedProduct = new Product(productId, productRequest.getName(), productRequest.getPrice(), productRequest.getDescription(), productRequest.getStock(), admin);

        given(this.productService.updateProduct(eq(productId), any(Product.class), eq(productRequest.getAdminId())))
                .willReturn(updatedProduct);

        mvc.perform(put("/api/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(productRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is(updatedProduct.getName())));

        verify(productService).updateProduct(eq(productId), any(Product.class), eq(productRequest.getAdminId()));
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void givenGetProductThatNotInDB_ReturnError404() throws Exception {
        long productId = 499L;
        String errorMessage = String.format("Product with id %d is not found", productId);

        given(productService.getProductById(productId))
                .willThrow(new ProductNotFoundException(errorMessage));

        mvc.perform(get("/api/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", is(errorMessage)));


        verify(productService).getProductById(productId);
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void whenGetProductWithValidId_ThenReturnProductAndStatus200() throws Exception {
        long productId = 1L;
        Product expectedProduct = new Product(productId, "product1", new BigDecimal("5.50"), "testing product", 5, null); // Adjust constructor as per actual definition
        given(productService.getProductById(productId)).willReturn(expectedProduct);

        mvc.perform(get("/api/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(expectedProduct.getName())))
                .andExpect(jsonPath("$.price", is(expectedProduct.getPrice().doubleValue())))
                .andExpect(jsonPath("$.description", is(expectedProduct.getDescription())))
                .andExpect(jsonPath("$.stock", is(expectedProduct.getStock())));

        verify(productService).getProductById(productId);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void whenDeleteNonExistentProduct_ThenReturnStatus404WithError() throws Exception {
        long productId = 1L;
        String expectedErrorMessage = String.format("Product with id %d is not found", productId);
        doThrow(new ProductNotFoundException(expectedErrorMessage))
                .when(productService).deleteProduct(productId);

        mvc.perform(delete("/api/products/" + productId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(expectedErrorMessage)));

        verify(productService).deleteProduct(productId);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void whenDeleteProductWithValidId_ThenReturnStatus200AndConfirmationMessage() throws Exception {
        long productId = 1L;
        given(productService.deleteProduct(productId)).willReturn(true);

        mvc.perform(delete("/api/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(productService).deleteProduct(productId);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void givenValidPageAndSize_ReturnPagedProducts() throws Exception {
        int page = 0;
        int size = 3;

        User admin = new User(7656L, "Mary Sunderland", "msunder@example.com", "23343password", Role.ADMIN);
        List<Product> sampleProducts = Arrays.asList(
                new Product(1L, "Product 1", BigDecimal.valueOf(10.0), "Description 1", 5, admin),
                new Product(2L, "Product 2", BigDecimal.valueOf(20.0), "Description 2", 10, admin)
        );

        Page<Product> samplePage = new PageImpl<>(sampleProducts, PageRequest.of(page, size), sampleProducts.size());

        given(productService.getAllProducts(page, size)).willReturn(samplePage);

        mvc.perform(get("/api/products?page=" + page + "&size=" + size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(sampleProducts.size())))
                .andExpect(jsonPath("$.limit", is(size)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalProducts", is(sampleProducts.size())))
                .andExpect(jsonPath("$.currentPage", is(page)));

        verify(productService).getAllProducts(page, size);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void givenRequestedPageNumberThatIsEmpty_ReturnEmptyPage() throws Exception {
        int page = 2;
        int size = 4;

        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        given(productService.getAllProducts(page, size)).willReturn(emptyPage);

        mvc.perform(get("/api/products?page=" + page + "&size=" + size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty())
                .andExpect(jsonPath("$.limit", is(size)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.totalProducts", is(0)))
                .andExpect(jsonPath("$.currentPage", is(page)));

        verify(productService).getAllProducts(page, size);
    }
}