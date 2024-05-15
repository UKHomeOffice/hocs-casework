package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Convert(attributeName = "pgsql_enum", converter = org.hibernate.type.EnumType.class)
@Table(name = "bank_holiday")
public class BankHoliday {

    @Getter
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    @JdbcTypeCode(SqlTypes.ENUM)
    private BankHolidayRegion region;

    @Getter
    @Column(name = "date")
    LocalDate date;

    public BankHoliday(String region, LocalDate date) {
        this.region = BankHolidayRegion.fromString(region);
        this.date = date;
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

    @Override
    public boolean equals(Object o) {
        if (this==o) {return true;}
        if (o==null || getClass()!=o.getClass()) {return false;}
        BankHoliday that = (BankHoliday) o;
        return region==that.region && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, date);
    }

}
