package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class CopyCompToComp2Test {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static String FROM_CLOB;

    static {
        Map<String, String> fromCaseValues = Map.of(
                "CatLost", "CatLostValue",
                "CatRude","CatRudeValue",
                "Channel","ChannelValue",
                "CatCCPhy","CatCCPhyValue");
        try {
            FROM_CLOB = MAPPER.writeValueAsString(fromCaseValues);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static final UUID TO_CASE_UUID = UUID.randomUUID();
    private static final UUID FROM_CASE_UUID = UUID.randomUUID();
    private static final String CORRESPONDENT_TYPE = "correspondent_type";
    private static final String FULLNAME = "fullname";
    private static final String ADDRESS_1 = "address1";
    private static final String ADDRESS_2 = "address2";
    private static final String ADDRESS_3 = "address3";
    private static final String POSTCODE = "postcode";
    private static final String COUNTRY = "country";
    private static final String TELEPHONE = "telephone";
    private static final String EMAIL = "email";
    private static final String REFERENCE = "reference";
    private static final String EXTERNAL_KEY = "externalKey";
    private static final Correspondent PRIMARY_CORRESPONDENT = new Correspondent(FROM_CASE_UUID,
            CORRESPONDENT_TYPE,
            FULLNAME,
            Address.builder()
                    .address1(ADDRESS_1)
                    .address2(ADDRESS_2)
                    .address3(ADDRESS_3)
                    .postcode(POSTCODE)
                    .country(COUNTRY)
                    .build(),
            TELEPHONE,
            EMAIL,
            REFERENCE,
            EXTERNAL_KEY);

    private static CaseData FROM_CASE = new CaseData(1L,
            FROM_CASE_UUID,
            null,
            null,
            null,
            false,
            FROM_CLOB,
            null,
            null,
            PRIMARY_CORRESPONDENT.getUuid(),
            PRIMARY_CORRESPONDENT,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private CorrespondentService correspondentService;

    private CaseData toCase;

    @Before
    public void setUp() {
        toCase = new CaseData(2L,
                TO_CASE_UUID,
                null,
                null,
                null,
                false,
                "{}", // this is the default
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    public void shouldCopyCaseDetails() {

        // given
        var compToComp2 = new CopyCompToComp2(MAPPER, caseDataService, correspondentService);

        // when
        compToComp2.copyCase(FROM_CASE, toCase);

        // then
        verify(caseDataService, times(1)).updateCaseData(eq(toCase.getUuid()), any(), anyMap());
        verify(correspondentService, times(1)).copyCorrespondents(FROM_CASE.getUuid(), toCase.getUuid());

        // clob values were copied - there's a separate test for copying values
        assertThat(toCase.getData()).isNotNull();
        assertThat(toCase.getDataMap(MAPPER)).isEqualTo(FROM_CASE.getDataMap(MAPPER));

    }
}