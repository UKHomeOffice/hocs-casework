package uk.gov.digital.ho.hocs.casework.api.factory;

import org.apache.commons.lang3.StringUtils;
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

    public Optional<CaseCopyStrategy> getStrategy(String fromCaseType, String toCaseType) {

        if (StringUtils.isBlank(fromCaseType)) {
            throw new IllegalArgumentException("fromCaseType must not be null or blank");
        }

        if (StringUtils.isBlank(toCaseType)) {
            throw new IllegalArgumentException("toCaseType must not be null or blank");
        }

        // search the annotated strategies
        return copyStrategies.stream().filter(isCopyStrategyForTypes(fromCaseType, toCaseType)).findFirst();

    }

    private Predicate<CaseCopyStrategy> isCopyStrategyForTypes(String fromCaseType, String toCaseType) {

        assert fromCaseType!=null;
        assert toCaseType!=null;

        return caseCopyStrategy -> {
            // check annotation is present and contains the requested from & to
            Class<? extends CaseCopyStrategy> strategyClass = caseCopyStrategy.getClass();
            if (strategyClass.isAnnotationPresent(CaseCopy.class)) {
                CaseCopy annotation = strategyClass.getAnnotation(CaseCopy.class);
                return annotation.fromCaseType().equals(fromCaseType) && annotation.toCaseType().equals(toCaseType);
            }
            return false;
        };
    }

}
