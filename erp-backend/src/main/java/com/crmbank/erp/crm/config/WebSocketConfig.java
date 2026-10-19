package com.crmbank.erp.crm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.crm.handler.CtiWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CtiWebSocketHandler ctiWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 💡 모든 도메인 허용 및 인터셉터 간소화로 연결 무조건 성공 보장
        registry.addHandler(ctiWebSocketHandler, "/ws/cti")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        // 💡 [기존 로직 활용] 부모 클래스의 메서드를 호출하여 세션 속성을 자동으로 복사합니다.
                        super.beforeHandshake(request, response, wsHandler, attributes);
                        
                        String query = request.getURI().getQuery();
                        if (query != null && query.contains("exten=")) {
                            String exten = query.split("exten=")[1].split("&")[0];
                            attributes.put("inner_no", exten);
                            log.info("🎯 [WebSocket] 내선번호 [{}] 회사코드 연동 완료", exten);
                        }
                        return true;
                    }
                });
    }
}
