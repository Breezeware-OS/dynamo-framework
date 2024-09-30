package net.breezeware.dynamo.logging.aop;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

/**
 * Logging Aspect class to handle Log4J's MDC/Thread context.
 */
@Slf4j
@Component
@Aspect
public class LoggingAspect {

    @Autowired
    Environment environment;

    /**
     * Joint point to generate Log4J's MDC/Thread context.
     * @param joinPoint method execution point interpreted by the aspect.
     */
    @Before(value = "@annotation(net.breezeware.dynamo.utils.aop.UpdateMdcLogData)")
    public void saveMdcData(JoinPoint joinPoint) {
        log.info("Entering saveMdcData(), joinPoint {}", joinPoint);

        HttpServletRequest httpRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestId = httpRequest.getHeader("requestId");
        log.info("requestId from header {}", requestId);

        // Updating requestId, if null or empty string else propagate the same requestId
        // from the header.
        requestId = requestId != null && !requestId.equals("") ? requestId : UUID.randomUUID().toString();

        log.info("ThreadContext before manipulation {}", ThreadContext.getContext());
        ThreadContext.put("applicationName", environment.getProperty("spring.application.name"));
        ThreadContext.put("requestId", requestId);
        try {
            ThreadContext.put("ipAddress", InetAddress.getLocalHost().getHostAddress());
            ThreadContext.put("host", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            log.error("Error while retrieving application's Host details like host-address and host-name, error {}",
                    e.getMessage());
        }

        log.info("ThreadContext after manipulation {}", ThreadContext.getContext());

        log.info("Leaving saveMdcData()");
    }

    /**
     * Joint point to clear Log4J's MDC/Thread context.
     * @param joinPoint method execution point interpreted by the aspect.
     */
    @After(value = "@annotation(net.breezeware.dynamo.utils.aop.UpdateMdcLogData)")
    public void clearMdcData(JoinPoint joinPoint) {
        log.info("Entering clearMdcData(), joinPoint {}", joinPoint);

        log.info("ThreadContext before manipulation {}", ThreadContext.getContext());
        ThreadContext.clearAll();
        log.info("ThreadContext after manipulation {}", ThreadContext.getContext());

        log.info("Leaving clearMdcData()");
    }

}
