package uk.gov.digital.ho.hocs.casework.api.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.CaseCopyStrategy;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class CaseCopyFactory {

    private List<CaseCopyStrategy> copyStrategies;

    @Autowired
    public CaseCopyFactory(@Lazy List<CaseCopyStrategy> copyStrategies) {
        this.copyStrategies = copyStrategies;
    }

    public Optional<CaseCopyStrategy> getStrategy(String fromCase, String toCase) {

        // search the annotated strategies
        return copyStrategies.stream()
                .filter(matchCaseTypes(fromCase, toCase))
                .findFirst();

    }

    private Predicate<CaseCopyStrategy> matchCaseTypes(String fromCase, String toCase) {
        return caseCopyStrategy -> {
            Optional<CaseCopyStrategy> first = copyStrategies.stream().filter(strategy -> {
                // check annotation is present and contains the requested from & to
                Class<? extends CaseCopyStrategy> strategyClass = strategy.getClass();
                if (strategyClass.isAnnotationPresent(CaseCopy.class)) {
                    CaseCopy annotation = strategyClass.getAnnotation(CaseCopy.class);
                    return annotation.fromCaseType().equals(fromCase) && annotation.toCaseType().equals(toCase);
                }
                return false;
            }).findFirst();

            return first.isPresent();

        };
    }
}
