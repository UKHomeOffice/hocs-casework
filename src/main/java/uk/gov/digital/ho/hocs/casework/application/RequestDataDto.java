package uk.gov.digital.ho.hocs.casework.application;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestDataDto {
    private String correlationId;
    private String userId;
    private String username;
    private String groups;


    public static RequestDataDto from(RequestData requestData) {
        return new RequestDataDto(requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }
}
