package uk.gov.digital.ho.hocs.casework.api.utils;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class CorrespondentTypeNameDecorator {

    public Set<Correspondent> addCorrespondentTypeName(Set<CorrespondentTypeDto> correspondentTypes,
                                                       Set<Correspondent> correspondents) {
        if (correspondentTypes == null) {
            return correspondents;
        }

        correspondents.forEach(correspondent -> addCorrespondentTypeName(correspondentTypes, correspondent));

        return correspondents;
    }

    public Correspondent addCorrespondentTypeName(Set<CorrespondentTypeDto> correspondentTypes,
                                                  Correspondent correspondent) {
        if (correspondentTypes == null) {
            return correspondent;
        }

        Optional<CorrespondentTypeDto> correspondentType = correspondentTypes.stream().filter(
            correspondentTypeDto -> Objects.equals(correspondentTypeDto.getType(),
                correspondent.getCorrespondentType())).findFirst();

        correspondentType.ifPresent(
            correspondentTypeDto -> correspondent.setCorrespondentTypeName(correspondentTypeDto.getDisplayName()));

        return correspondent;
    }

}
