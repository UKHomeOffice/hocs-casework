package uk.gov.digital.ho.hocs.casework.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class UUIDAttributeConverterTest {

    private UUIDAttributeConverter converter;

    @Before
    public void setUp() {
        this.converter = new UUIDAttributeConverter();
    }

    @Test
    public void shouldConvertToDatabaseColumn() {

        UUID uuid = UUID.randomUUID();
        UUID convertedUUID = converter.convertToDatabaseColumn(uuid);

        assertThat(convertedUUID).isNotNull();
        assertThat(convertedUUID).isInstanceOf(UUID.class);
        assertThat(convertedUUID.toString()).isEqualTo(uuid.toString());
    }

    @Test
    public void shouldConvertToEntityAttribute() {
        UUID uuid = UUID.randomUUID();
        UUID convertedUUID = converter.convertToEntityAttribute(uuid);

        assertThat(convertedUUID).isNotNull();
        assertThat(convertedUUID).isInstanceOf(UUID.class);
        assertThat(convertedUUID.toString()).isEqualTo(uuid.toString());
    }

}