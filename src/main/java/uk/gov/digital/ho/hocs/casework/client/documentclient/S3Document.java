package uk.gov.digital.ho.hocs.casework.client.documentclient;

import lombok.AllArgsConstructor;
import lombok.Getter;

@java.lang.SuppressWarnings("squid:S1068")
@AllArgsConstructor
@Getter
public class S3Document {

    private final String filename;
    private final String originalFilename;
    private final byte[] data;
    private final String fileType;
    private final String mimeType;
}
