package kz.iitu.springlab.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Order(4)
public class CallCounterAspect {

    private static final Logger log = LoggerFactory.getLogger(CallCounterAspect.class);

    // имя метода -> сколько раз вызван
    private final Map<String, Integer> counters = new ConcurrentHashMap<>();

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void count(JoinPoint jp) {
        String name = jp.getSignature().toShortString();
        int total = counters.merge(name, 1, Integer::sum); // +1 к счётчику
        log.info("[COUNT] {} called {} times", name, total);
    }

    // копия статистики, отсортированная по имени метода
    public Map<String, Integer> getStatistics() {
        return new TreeMap<>(counters);
    }
}