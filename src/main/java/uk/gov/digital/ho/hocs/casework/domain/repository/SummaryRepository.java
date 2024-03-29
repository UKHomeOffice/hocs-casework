package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.UUIDCharType;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.domain.model.Summary;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class SummaryRepository {

    private static final String TEAM_UUID_COLUMN_NAME = "teamUuid";

    private static final String COUNT_COLUMN_NAME = "count";

    @PersistenceContext
    private EntityManager entityManager;

    public List<Summary> findTeamsAndCaseCountByTeamUuid(Set<UUID> teamUuidSet) {
        Query query = entityManager.createNativeQuery(
            "SELECT st.team_uuid as teamUuid, count(*) FROM stage st INNER JOIN case_data cd ON st.case_uuid = cd.uuid WHERE NOT cd.deleted AND st.team_uuid IS NOT NULL AND st.team_uuid IN ?1 AND NOT cd.data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);

        query.unwrap(NativeQuery.class).addScalar(TEAM_UUID_COLUMN_NAME, UUIDCharType.INSTANCE).addScalar(
            COUNT_COLUMN_NAME, IntegerType.INSTANCE).setResultTransformer(Transformers.aliasToBean(Summary.class));

        return query.getResultList();
    }

    public List<Summary> findUnallocatedCasesByTeam(Set<UUID> teamUuidSet) {
        Query query = entityManager.createNativeQuery(
            "SELECT st.team_uuid as teamUuid, COUNT(*) FROM casework.stage st INNER JOIN casework.case_data cd ON st.case_uuid = cd.uuid WHERE st.team_uuid in ?1 AND st.user_uuid IS NULL AND NOT cd.deleted AND NOT cd.data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);

        query.unwrap(NativeQuery.class).addScalar(TEAM_UUID_COLUMN_NAME, UUIDCharType.INSTANCE).addScalar(
            COUNT_COLUMN_NAME, IntegerType.INSTANCE).setResultTransformer(Transformers.aliasToBean(Summary.class));

        return query.getResultList();
    }

    public List<Summary> findOverdueCasesByTeam(Set<UUID> teamUuidSet) {
        Query query = entityManager.createNativeQuery(
            "SELECT st.team_uuid as teamUuid, COUNT(*) FROM casework.stage st INNER JOIN casework.case_data cd ON st.case_uuid = cd.uuid WHERE st.team_uuid IN ?1 AND st.deadline < CURRENT_DATE AND NOT cd.deleted AND NOT cd.data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);

        query.unwrap(NativeQuery.class).addScalar(TEAM_UUID_COLUMN_NAME, UUIDCharType.INSTANCE).addScalar(
            COUNT_COLUMN_NAME, IntegerType.INSTANCE).setResultTransformer(Transformers.aliasToBean(Summary.class));

        return query.getResultList();
    }

    public List<Summary> findOverdueUserCasesInTeams(Set<UUID> teamUuidSet, String userUuid) {
        Query query = entityManager.createNativeQuery(
            "SELECT st.team_uuid as teamUuid, COUNT(*) FROM casework.stage st INNER JOIN casework.case_data cd ON st.case_uuid = cd.uuid WHERE st.team_uuid in ?1 AND st.user_uuid = ?2 AND st.deadline < CURRENT_DATE AND NOT cd.deleted AND NOT cd.data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);
        query.setParameter(2, userUuid);

        query.unwrap(NativeQuery.class).addScalar(TEAM_UUID_COLUMN_NAME, UUIDCharType.INSTANCE).addScalar(
            COUNT_COLUMN_NAME, IntegerType.INSTANCE).setResultTransformer(Transformers.aliasToBean(Summary.class));

        return query.getResultList();
    }

    public List<Summary> findUserCasesInTeams(Set<UUID> teamUuidSet, String userUuid) {
        Query query = entityManager.createNativeQuery(
            "SELECT CAST(st.team_uuid as varchar) as teamUuid, COUNT(*) FROM casework.stage st INNER JOIN casework.case_data cd ON st.case_uuid = cd.uuid WHERE st.team_uuid in ?1 AND st.user_uuid = ?2 AND NOT cd.deleted AND NOT cd.data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);
        query.setParameter(2, userUuid);

        query.unwrap(NativeQuery.class).addScalar(TEAM_UUID_COLUMN_NAME, UUIDCharType.INSTANCE).addScalar(
            COUNT_COLUMN_NAME, IntegerType.INSTANCE).setResultTransformer(Transformers.aliasToBean(Summary.class));

        return query.getResultList();
    }

}
