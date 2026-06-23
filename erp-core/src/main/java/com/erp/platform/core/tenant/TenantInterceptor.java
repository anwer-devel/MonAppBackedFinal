package com.erp.platform.core.tenant;

import com.erp.platform.core.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String jwt = (String) request.getAttribute("jwt");
        if (jwt != null) {
            try {
                String partnerCode = jwtService.extractPartnerCode(jwt);
                if (partnerCode != null) {
                    String schema = TenantSchemaResolver.resolveSchema(partnerCode);
                    TenantContext.setCurrentTenant(schema);
                    log.debug("Tenant set to schema: {}", schema);
                }
            } catch (Exception e) {
                log.debug("Could not extract tenant from JWT: {}", e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        TenantContext.clear();
    }
}
