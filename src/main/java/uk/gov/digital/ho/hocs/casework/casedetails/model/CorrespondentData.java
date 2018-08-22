package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "correspondent_data")
public class CorrespondentData {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;
    @Getter
    @Column(name = "title")
    private String title;
    @Getter
    @Column(name = "first_name")
    private String firstName;
    @Getter
    @Column(name = "surname")
    private String surname;
    @Getter
    @Column(name = "postcode")
    private String postcode;
    @Getter
    @Column(name = "address1")
    private String address1;
    @Getter
    @Column(name = "address2")
    private String address2;
    @Getter
    @Column(name = "address3")
    private String address3;
    @Getter
    @Column(name = "country")
    private String country;
    @Getter
    @Column(name = "telephone")
    private String telephone;
    @Getter
    @Column(name = "email")
    private String email;
    @Getter
    @Column(name = "added")
    private LocalDateTime added;
    @Getter
    @Column(name = "updated")
    private LocalDateTime updated;

    @Getter
    @Column(name = "type", insertable = false)
    private String type;

    public CorrespondentData(String title, String firstName, String surname, String postcode, String address1, String address2, String address3, String country, String telephone, String email) {
        this.uuid = UUID.randomUUID();
        this.title = title;
        this.firstName = firstName;
        this.surname = surname;
        this.postcode = postcode;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.country = country;
        this.telephone = telephone;
        this.email = email;
        this.added = LocalDateTime.now();
    }
}
