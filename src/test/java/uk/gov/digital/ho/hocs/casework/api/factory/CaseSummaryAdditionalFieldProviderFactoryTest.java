package uk.gov.digital.ho.hocs.casework.api.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.DcuMinCaseSummaryAdditionalFieldProvider;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.DefaultCaseSummaryAdditionalFieldProvider;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
public class CaseSummaryAdditionalFieldProviderFactoryTest {

    CaseSummaryAdditionalFieldProviderFactory caseSummaryAdditionalFieldProviderFactory;

    @Before
    public void setUp() {
        caseSummaryAdditionalFieldProviderFactory = new CaseSummaryAdditionalFieldProviderFactory();
    }

    @Test
    public void shouldReturnTheDefaultFieldProvider() {
        // When
        final CaseSummaryAdditionalFieldProvider result = caseSummaryAdditionalFieldProviderFactory
                .getCaseSummaryAdditionalFieldProvider("DEFAULT");

        // Then
        assertThat(result.getClass()).isEqualTo(DefaultCaseSummaryAdditionalFieldProvider.class);
    }

    @Test
    public void shouldReturnTheDcuMinFieldProvider() {
        // When
        final CaseSummaryAdditionalFieldProvider result = caseSummaryAdditionalFieldProviderFactory
                .getCaseSummaryAdditionalFieldProvider("MIN");

        // Then
        assertThat(result.getClass()).isEqualTo(DcuMinCaseSummaryAdditionalFieldProvider.class);
    }
}