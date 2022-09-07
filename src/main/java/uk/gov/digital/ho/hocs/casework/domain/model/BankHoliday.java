package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@TypeDef(name = "pgsql_enum", typeClass = org.hibernate.type.EnumType.class)
@Table(name = "bank_holiday")
public class BankHoliday {

    @Getter
    @Column(name = "date")
    LocalDate date;

    @Getter
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    @Type(type = "pgsql_enum")
    private BankHolidayRegion region;

    public BankHoliday(String region, LocalDate date) {
        this.region = BankHolidayRegion.fromString(region);
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        BankHoliday that = (BankHoliday) o;
        return region == that.region && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, date);
    }

    public enum BankHolidayRegion {
        UNITED_KINGDOM,
        ENGLAND_AND_WALES,
        SCOTLAND,
        NORTHERN_IRELAND;

        public static BankHolidayRegion fromString(String from) {
            switch (from) {
                case "united-kingdom":
                    return UNITED_KINGDOM;
                case "england-and-wales":
                    return ENGLAND_AND_WALES;
                case "scotland":
                    return SCOTLAND;
                case "northern-ireland":
                    return NORTHERN_IRELAND;
            }

            throw new IllegalArgumentException("Bank holiday region not recognised");
        }
    }

}
