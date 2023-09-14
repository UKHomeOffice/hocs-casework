package uk.gov.digital.ho.hocs.casework.domain.exception;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;

public interface ApplicationExceptions {

    @Getter
    class EntityCreationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public EntityCreationException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

        public EntityCreationException(String msg, LogEvent event, LogEvent exception) {
            super(msg);
            this.event = event;
            this.exception = exception;
        }

    }

    @Getter
    class EntityNotFoundException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public EntityNotFoundException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

        public EntityNotFoundException(String msg, LogEvent event, LogEvent exception) {
            super(msg);
            this.event = event;
            this.exception = exception;
        }

    }

    class InvalidPriorityTypeException extends RuntimeException {

        public InvalidPriorityTypeException(String msg) {
            super(msg);
        }

    }

    @Getter
    class TeamAllocationException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public TeamAllocationException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

    }

    @Getter
    class DataMappingException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public DataMappingException(String msg, LogEvent event, LogEvent exception) {
            super(msg);
            this.event = event;
            this.exception = exception;
        }

    }

    @Getter
    class ConfigFileReadException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ConfigFileReadException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

    }

    @Getter
    class ConfigFolderReadException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ConfigFolderReadException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

    }

    @Getter
    class DatabaseConflictException extends RuntimeException {
        private final LogEvent event;
        private final LogEvent exception;

        public DatabaseConflictException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

    }

    @Getter
    class StreamingResponseBodyException extends RuntimeException {

        private final LogEvent event;
        private final LogEvent exception;

        public StreamingResponseBodyException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

    }

    @Getter
    class ReportBodyStreamingException extends RuntimeException {

        private final LogEvent event;
        private final LogEvent exception;

        public ReportBodyStreamingException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

    }

    @Getter
    class ReportCaseTypeNotSupportedException extends RuntimeException {

        private final LogEvent event;
        private final LogEvent exception;

        public ReportCaseTypeNotSupportedException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
            this.exception = null;
        }

    }

}
