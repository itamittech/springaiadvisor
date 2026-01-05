package com.example.advisor.ui;

import com.example.advisor.controller.UiController;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(UiController.class)
public class AdvisorUiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageShouldLoad() throws Exception {
        WebClient webClient = MockMvcWebClientBuilder.mockMvcSetup(mockMvc).build();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        // MockMvc handles the request, localhost/ works as a base
        HtmlPage page = webClient.getPage("http://localhost/");

        assertThat(page).isNotNull();
        assertThat(page.getTitleText()).isEqualTo("Spring AI Advisor");
    }
}
