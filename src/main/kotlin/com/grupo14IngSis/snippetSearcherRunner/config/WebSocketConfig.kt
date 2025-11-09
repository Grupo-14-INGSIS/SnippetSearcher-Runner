//package com.grupo14IngSis.snippetSearcherRunner.config
//
//// opcional
//
//import org.springframework.context.annotation.Configuration
//import org.springframework.web.socket.config.annotation.*
//
//@Configuration
//@EnableWebSocket
//class WebSocketConfig : WebSocketConfigurer {
//
//    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
//        registry.addHandler(SnippetExecutionWebSocketHandler(), "/ws/execution")
//            .setAllowedOrigins("*")
//    }
//}