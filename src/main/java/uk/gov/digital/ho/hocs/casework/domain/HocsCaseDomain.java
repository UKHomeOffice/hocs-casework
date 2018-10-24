package uk.gov.digital.ho.hocs.casework.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HocsCaseDomain {

    private final HocsCaseContext hocsCaseContext;

    @Autowired
    public HocsCaseDomain(HocsCaseContext hocsCaseContext) {
        this.hocsCaseContext = hocsCaseContext;
    }

    public void executeCommand(Command command) {
        log.debug("Process command: {}", command);
        command.execute(hocsCaseContext);
        log.debug("Processed command: {}", command);
    }

}