---
description: How to wrap an existing Spring Boot API with a simple UI
---

This workflow guides you through the process of adding a lightweight UI to an existing Spring Boot API using Thymeleaf and Bootstrap.

1.  **Add Dependencies**
    Add the follwing to your `pom.xml`:
    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.htmlunit</groupId>
        <artifactId>htmlunit</artifactId>
        <scope>test</scope>
    </dependency>
    ```

2.  **Create UI Controller**
    Create a controller to serve the HTML page:
    ```java
    @Controller
    public class UiController {
        @GetMapping("/")
        public String home() {
            return "chat";
        }
    }
    ```

3.  **Create View Templates**
    -   Create `src/main/resources/templates/chat.html`
    -   Use `fetch` API in JavaScript to call your existing REST endpoints.
    -   Use Bootstrap for quick styling.

4.  **Add Automated UI Test**
    Create a `WebMvcTest` using `HtmlUnit` to verify the UI loads:
    ```java
    @WebMvcTest(UiController.class)
    public class AdvisorUiTest {
        @Autowired private MockMvc mockMvc;
        @Test
        void homePageShouldLoad() throws Exception {
            WebClient webClient = MockMvcWebClientBuilder.mockMvcSetup(mockMvc).build();
            HtmlPage page = webClient.getPage("http://localhost/");
            assertThat(page).isNotNull();
        }
    }
    ```

// turbo
5.  **Verify**
    Run the tests to ensure everything works:
    ```bash
    mvn test
    ```
