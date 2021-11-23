package uk.gov.digital.ho.hocs.casework.api.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseCorrespondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Set;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
public class CorrespondentTypeNameDecoratorTest {

    private final CorrespondentTypeNameDecorator correspondentTypeNameDecorator = new CorrespondentTypeNameDecorator();
    private final Address address = new Address("postcode", "line1", "line2", "line3", "country");
    private Correspondent correspondent;

    @Before
    public void setup() {
        correspondent = new Correspondent(
                UUID.randomUUID(),
                "Type",
                "full name",
                "organisation",
                address,
                "01923478393",
                "email@test.com",
                "ref",
                "key");
    }

    @Test
    public void addCorrespondentTypeName_nullCorrespondentType() {
        Set<BaseCorrespondent> correspondents = Set.of(correspondent);

        Assert.assertEquals(correspondentTypeNameDecorator.addCorrespondentTypeName(null, correspondents), correspondents);

        Assert.assertEquals(correspondentTypeNameDecorator.addCorrespondentTypeName(null, correspondent), correspondent);
    }

    @Test
    public void addCorrespondentTypeName_correspondentTypeNotPresent() {
        Set<BaseCorrespondent> correspondents = Set.of(correspondent);
        Set<CorrespondentTypeDto> correspondentTypeDtos = Set.of(new CorrespondentTypeDto(UUID.randomUUID(), "Test", "Test"));

        Set<BaseCorrespondent> correspondentsAfter = correspondentTypeNameDecorator.addCorrespondentTypeName(correspondentTypeDtos, correspondents);
        Assert.assertTrue(correspondentsAfter.contains(correspondent));

        BaseCorrespondent correspondentAfter = correspondentTypeNameDecorator.addCorrespondentTypeName(correspondentTypeDtos, correspondent);
        Assert.assertEquals(correspondentAfter, correspondent);
    }

    @Test
    public void addCorrespondentTypeName_correspondentTypePresent() {
        Set<BaseCorrespondent> correspondents = Set.of(correspondent);
        Set<CorrespondentTypeDto> correspondentTypeDtos = Set.of(new CorrespondentTypeDto(UUID.randomUUID(), "This is a test.", "Type"));

        correspondent.setCorrespondentTypeName("This is a test.");

        Set<BaseCorrespondent> correspondentsAfterDecorated = correspondentTypeNameDecorator.addCorrespondentTypeName(correspondentTypeDtos, correspondents);
        Assert.assertTrue(correspondentsAfterDecorated.contains(correspondent));

        BaseCorrespondent correspondentAfterDecorated = correspondentTypeNameDecorator.addCorrespondentTypeName(correspondentTypeDtos, correspondent);
        Assert.assertEquals(correspondentAfterDecorated, correspondent);
    }


}
