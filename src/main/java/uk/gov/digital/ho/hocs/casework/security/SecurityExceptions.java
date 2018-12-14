package uk.gov.digital.ho.hocs.casework.security;

import uk.gov.digital.ho.hocs.casework.application.LogEvent;

public interface SecurityExceptions {
    class StageNotAssignedToLoggedInUserException extends RuntimeException {
        private final LogEvent event;

        StageNotAssignedToLoggedInUserException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class StageNotAssignedToUserTeamException extends RuntimeException {
        private final LogEvent event;

        StageNotAssignedToUserTeamException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class PermissionCheckException extends RuntimeException {
        private final LogEvent event;

        PermissionCheckException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }


}
