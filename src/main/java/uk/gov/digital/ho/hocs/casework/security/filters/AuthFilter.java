package uk.gov.digital.ho.hocs.casework.security.filters;

public interface AuthFilter {

    String getKey();

    void applyFilter(Object filerObject);
}
