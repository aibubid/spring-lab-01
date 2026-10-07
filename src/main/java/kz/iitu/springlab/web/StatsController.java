package kz.iitu.springlab.web;

import kz.iitu.springlab.aspect.CallCounterAspect;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class StatsController {

    private final CallCounterAspect callCounterAspect;

    public StatsController(CallCounterAspect callCounterAspect) {
        this.callCounterAspect = callCounterAspect;
    }

    @GetMapping("/stats")
    public Map<String, Integer> stats() {
        return callCounterAspect.getStatistics();
    }
}