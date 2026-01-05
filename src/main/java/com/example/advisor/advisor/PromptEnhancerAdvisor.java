package com.example.advisor.advisor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PromptEnhancerAdvisor uses Spring AOP to modify user prompts before
 * processing.
 * 
 * This demonstrates how to create custom cross-cutting concerns for AI
 * interactions.
 * The advisor enhances prompts by adding formatting instructions.
 */
@Aspect
@Component
public class PromptEnhancerAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(PromptEnhancerAdvisor.class);

    // Enhancement instruction added to all prompts
    private static final String ENHANCEMENT = " Please format your response with clear bullet points.";

    /**
     * Intercepts the chatCustomFeature method and enhances the user's message.
     */
    @Around("execution(* com.example.advisor.controller.AdvisorAssignmentController.chatCustomFeature(..)) && args(message)")
    public Object enhancePrompt(ProceedingJoinPoint joinPoint, String message) throws Throwable {
        // Enhance the original message
        String enhancedMessage = message + ENHANCEMENT;

        logger.info("PromptEnhancer - Original: '{}' -> Enhanced: '{}'", message, enhancedMessage);
        System.out.println("PromptEnhancer - Original: '" + message + "' -> Enhanced with bullet point instruction");

        // Proceed with the enhanced message
        Object[] args = joinPoint.getArgs();
        args[0] = enhancedMessage;

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed(args);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("PromptEnhancer - Execution completed in " + duration + "ms");

        return result;
    }
}
