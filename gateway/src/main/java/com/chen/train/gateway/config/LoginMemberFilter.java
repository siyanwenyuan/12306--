package com.chen.train.gateway.config;

import com.chen.train.gateway.util.JwtUtil;
import com.chen.train.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginMemberFilter implements Ordered, GlobalFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //先得到所有的被拦截的路径
        String path = exchange.getRequest().getURI().getPath();

        // 排除不需要拦截的请求
        if (path.contains("/admin")
                || path.contains("/hello")
                || path.contains("/member/member/login")
                || path.contains("/member/member/send-code")
                 || path.contains("/business/Redis/setRedis")
                 || path.contains("/business/Redis/getRedis")
                 || path.contains("/business/kaptcha")
        ) {
            LOG.info("不需要登录验证：{}", path);

            //返回这个请求
            return chain.filter(exchange);
        } else {
            LOG.info("需要登录验证：{}", path);
        }
        // 获取header的token参数
        String token = exchange.getRequest().getHeaders().getFirst("token");
        LOG.info("会员登录验证开始，token：{}", token);
        if (token == null || token.isEmpty()) {
            LOG.info( "token为空，请求被拦截" );
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //中断这个请求
            return exchange.getResponse().setComplete();
        }

        // 校验token是否有效，包括token是否被改过，是否过期
        boolean validate = JwtUtil.validate(token);
        if (validate) {
            LOG.info("token有效，放行该请求");
            //请求被放行
            return chain.filter(exchange);
        } else {
            LOG.warn( "token无效，请求被拦截" );
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //请求被中断
            return exchange.getResponse().setComplete();
        }

    }

    /**
     * 优先级设置  值越小  优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
