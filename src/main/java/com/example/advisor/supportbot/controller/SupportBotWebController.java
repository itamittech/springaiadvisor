package com.example.advisor.supportbot.controller;

import com.example.advisor.supportbot.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Web controller for serving the Support Bot UI pages.
 */
@Controller
@RequestMapping("/supportbot")
public class SupportBotWebController {

    private final CustomerService customerService;

    public SupportBotWebController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Main Support Bot chat interface.
     */
    @GetMapping
    public String supportBotHome(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "supportbot";
    }

    /**
     * Alias for root path.
     */
    @GetMapping("/")
    public String supportBotHomeSlash(Model model) {
        return supportBotHome(model);
    }
}
