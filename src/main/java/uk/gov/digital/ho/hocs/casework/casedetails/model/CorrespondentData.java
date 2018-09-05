package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

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
    @Column(name = "address_ident")
    private String addressIdentity;

    @Getter
    @Column(name = "email_ident")
    private String emailIdentity;

    @Getter
    @Column(name = "telephone_ident")
    private String telephoneIdentity;


    public CorrespondentData(String title, String firstName, String surname, String postcode, String address1, String address2, String address3, String country, String telephone, String email, String addressIdentity, String emailIdentity, String telephoneIdentity) {
        if (firstName == null || surname == null || (postcode == null)) {
            throw new EntityCreationException("Cannot create CorrespondentData(%s, %s, %s, %s, %s).", firstName, surname, postcode, telephone, email);
        }
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
        this.addressIdentity = addressIdentity;
        this.emailIdentity = emailIdentity;
        this.telephoneIdentity = telephoneIdentity;
    }

    private static boolean notNullOrEmpty(String value) {
        return (value != null && !value.equals(""));
    }

    public void update(String title, String firstName, String surname, String postcode, String address1, String address2, String address3, String country, String telephone, String email, String addressIdentity, String emailIdentity, String telephoneIdentity) {
        if (notNullOrEmpty(title)) {
            this.title = title;
        }
        if (notNullOrEmpty(firstName)) {
            this.firstName = firstName;
        }
        if (notNullOrEmpty(surname)) {
            this.surname = surname;
        }
        if (notNullOrEmpty(postcode)) {
            this.postcode = postcode;
        }
        if (notNullOrEmpty(address1)) {
            this.address1 = address1;
        }
        if (notNullOrEmpty(address2)) {
            this.address2 = address2;
        }
        if (notNullOrEmpty(address3)) {
            this.address3 = address3;
        }
        if (notNullOrEmpty(country)) {
            this.country = country;
        }
        if (notNullOrEmpty(telephone)) {
            this.telephone = telephone;
        }
        if (notNullOrEmpty(email)) {
            this.email = email;
        }

        this.addressIdentity = addressIdentity;
        this.emailIdentity = emailIdentity;
        this.telephoneIdentity = telephoneIdentity;
        this.updated = LocalDateTime.now();
    }


}
