package uk.gov.digital.ho.hocs.casework.security;

import uk.gov.digital.ho.hocs.casework.application.LogEvent;

public interface SecurityExceptions {
    class StageNotAssignedToLoggedInUserException extends RuntimeException {
        LogEvent event;
        public StageNotAssignedToLoggedInUserException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class StageNotAssignedToUserTeamException extends RuntimeException {
        LogEvent event;
        public StageNotAssignedToUserTeamException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class PermissionCheckException extends RuntimeException {
        LogEvent event;
        public PermissionCheckException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }


}
