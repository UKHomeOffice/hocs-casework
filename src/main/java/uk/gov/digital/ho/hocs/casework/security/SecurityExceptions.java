package uk.gov.digital.ho.hocs.casework.security;

import uk.gov.digital.ho.hocs.casework.application.LogEvent;

public interface SecurityExceptions {
    abstract class EventRuntimeException extends RuntimeException {
        final LogEvent event;

        EventRuntimeException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }
    }

    class StageNotAssignedToLoggedInUserException extends EventRuntimeException {
        StageNotAssignedToLoggedInUserException(String s, LogEvent event) {
            super(s, event);
        }
    }

    class StageNotAssignedToUserTeamException extends EventRuntimeException {
        StageNotAssignedToUserTeamException(String s, LogEvent event) {
            super(s, event);
        }
    }

    class PermissionCheckException extends EventRuntimeException {
        public PermissionCheckException(String s, LogEvent event) {
            super(s, event);
        }
    }

    class ForbiddenException extends EventRuntimeException {
        public ForbiddenException(String s, LogEvent event) {
            super(s, event);
        }
    }

    class AuthFilterException extends RuntimeException {
        final LogEvent event;

        public AuthFilterException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }

        public LogEvent getEvent() {return event;}
    }

}
