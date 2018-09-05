package uk.gov.digital.ho.hocs.casework.domain;

public interface Command {

    void execute(HocsCaseContext hocsCaseContext);
}
