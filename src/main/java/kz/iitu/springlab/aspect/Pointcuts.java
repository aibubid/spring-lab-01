package kz.iitu.springlab.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Pointcuts {

    // все классы в пакете service и его подпакетах
    @Pointcut("within(kz.iitu.springlab.service..*)")
    public void serviceLayer() { }

    // любой public-метод с любым возвращаемым типом и любыми аргументами
    @Pointcut("execution(public * *(..))")
    public void publicMethod() { }

    // пересечение: public-методы именно сервисного слоя
    @Pointcut("serviceLayer() && publicMethod()")
    public void serviceOperation() { }
}