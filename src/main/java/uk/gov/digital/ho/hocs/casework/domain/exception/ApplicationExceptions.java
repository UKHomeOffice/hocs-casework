package uk.gov.digital.ho.hocs.casework.domain.exception;

import uk.gov.digital.ho.hocs.casework.application.LogEvent;

public interface ApplicationExceptions {

    class EntityCreationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public EntityCreationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public EntityCreationException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class EntityNotFoundException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public EntityNotFoundException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public EntityNotFoundException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class InvalidPriorityTypeException extends RuntimeException {

        public InvalidPriorityTypeException(String msg, Object... args) {
            super(String.format(msg, args));
        }

    }

    class TeamAllocationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public TeamAllocationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public TeamAllocationException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

    }

    class DataMappingException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public DataMappingException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

    }

    class ConfigFileReadException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ConfigFileReadException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ConfigFolderReadException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ConfigFolderReadException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class DatabaseConflictException extends RuntimeException {
        private final LogEvent event;
        private final LogEvent exception;

        public DatabaseConflictException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class StreamingResponseBodyException extends RuntimeException {

        private final LogEvent event;
        private final LogEvent exception;

        public StreamingResponseBodyException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ReportBodyStreamingException extends RuntimeException {

        private final LogEvent event;
        private final LogEvent exception;

        public ReportBodyStreamingException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ReportCaseTypeNotSupportedException extends RuntimeException {

        private final LogEvent event;
        private final LogEvent exception;

        public ReportCaseTypeNotSupportedException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

}
