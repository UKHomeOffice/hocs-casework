package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataTypeServiceTest {

    @Mock
    private InfoClient infoClient;

    private CaseDataTypeService caseDataTypeService;

    @Before
    public void setUp() {
        this.caseDataTypeService = new CaseDataTypeService(infoClient);
    }

    @Test
    public void ShouldGetAllCaseDataTypes() {
        var caseDataTypes = List.of(
                CaseDataTypeFactory.from("A Type", "01"),
                CaseDataTypeFactory.from("B Type", "45"));

        when(infoClient.getAllCaseDataTypes()).thenReturn(caseDataTypes);

        // Call with an uninteresting predicate
        var result = caseDataTypeService.getCaseDataTypes(Objects::nonNull).collect(Collectors.toSet());

        verify(infoClient).getAllCaseDataTypes();
        verifyNoMoreInteractions(infoClient);

        assertEquals(2, result.size());
        assertTrue(result.contains(caseDataTypes.get(0)));
        assertTrue(result.contains(caseDataTypes.get(1)));
    }

    @Test
    public void ShouldGetAllCaseDataTypes_Empty() {
        List<CaseDataType> caseDataTypes = List.of();

        when(infoClient.getAllCaseDataTypes()).thenReturn(caseDataTypes);

        // Call with an uninteresting predicate
        var result = caseDataTypeService.getCaseDataTypes(Objects::nonNull).collect(Collectors.toSet());

        verify(infoClient).getAllCaseDataTypes();
        verifyNoMoreInteractions(infoClient);

        assertEquals(0, result.size());
    }

    @Test
    public void ShouldGetAllCaseDataTypes_RespectsPredicate() {
        var caseDataTypes = List.of(
                CaseDataTypeFactory.from("A Type", "01"),
                CaseDataTypeFactory.from("B Type", "45"));

        when(infoClient.getAllCaseDataTypes()).thenReturn(caseDataTypes);

        // Call with an uninteresting predicate
        var result = caseDataTypeService.getCaseDataTypes(it -> it.getShortCode().equals("45")).collect(Collectors.toSet());

        verify(infoClient).getAllCaseDataTypes();
        verifyNoMoreInteractions(infoClient);

        assertEquals(1, result.size());
        assertTrue(result.contains(caseDataTypes.get(1)));
    }

    @Test
    public void ShouldGetAllCaseDataTypes_RespectsPredicateNoMatches() {
        var caseDataTypes = List.of(
                CaseDataTypeFactory.from("A Type", "01"),
                CaseDataTypeFactory.from("B Type", "45"));

        when(infoClient.getAllCaseDataTypes()).thenReturn(caseDataTypes);

        // Call with an uninteresting predicate
        var result = caseDataTypeService.getCaseDataTypes(it -> it.getShortCode().equals("99")).collect(Collectors.toSet());

        verify(infoClient).getAllCaseDataTypes();
        verifyNoMoreInteractions(infoClient);

        assertEquals(0, result.size());
    }

    @Test
    public void ShouldGetCaseDataType_ByCaseType() {
        var caseDataTypes = List.of(
                CaseDataTypeFactory.from("A Type", "01"),
                CaseDataTypeFactory.from("B Type", "45"));

        when(infoClient.getAllCaseDataTypes()).thenReturn(caseDataTypes);

        var result = caseDataTypeService.getCaseDataType("A Type");

        verify(infoClient).getAllCaseDataTypes();
        verifyNoMoreInteractions(infoClient);

        assertEquals(caseDataTypes.get(0), result);
    }

    @Test
    public void ShouldGetCaseDataType_ByCaseType_NoMatch() {
        var caseDataTypes = List.of(
                CaseDataTypeFactory.from("A Type", "01"),
                CaseDataTypeFactory.from("B Type", "45"));

        when(infoClient.getAllCaseDataTypes()).thenReturn(caseDataTypes);

        assertThrows(ApplicationExceptions.EntityNotFoundException.class, () ->
                caseDataTypeService.getCaseDataType("C Type"));
    }

    @Test
    public void ShouldGetCaseDataType_ByCaseType_Null() {
        assertThrows(ApplicationExceptions.EntityNotFoundException.class, () ->
            caseDataTypeService.getCaseDataType(null));
    }


}
