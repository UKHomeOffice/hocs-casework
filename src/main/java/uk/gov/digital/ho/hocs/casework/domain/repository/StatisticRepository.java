package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.UUIDCharType;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.domain.model.Statistic;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class StatisticRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Statistic> findTeamsAndCaseCountByTeamUuidandCaseTypes(Set<UUID> teamUuidSet, Set<String> caseTypeSet) {
        Query query = entityManager.createNativeQuery("SELECT st.team_uuid as teamUuid, count(*) FROM casework.stage st INNER JOIN casework.case_data cd ON st.case_uuid = cd.uuid WHERE NOT cd.deleted AND (st.team_uuid IN ?1 OR cd.type IN ?2) AND st.team_uuid IS NOT NULL GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);
        query.setParameter(2, caseTypeSet);

        query.unwrap(NativeQuery.class)
                .addScalar("teamUuid", UUIDCharType.INSTANCE)
                .addScalar("count", IntegerType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(Statistic.class));

        return (List<Statistic>) query
                .getResultList();
    };

    public List<Statistic> findUnallocatedCasesByTeam(Set<UUID> teamUuidSet) {
        Query query = entityManager.createNativeQuery("SELECT st.team_uuid as teamUuid, COUNT(*) FROM casework.stage st WHERE st.team_uuid in ?1 AND st.user_uuid IS NULL GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);

        query.unwrap(NativeQuery.class)
                .addScalar("teamUuid", UUIDCharType.INSTANCE)
                .addScalar("count", IntegerType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(Statistic.class));

        return (List<Statistic>) query
                .getResultList();
    }

    public List<Statistic> findOverdueCasesByTeam(Set<UUID> teamUuidSet) {
        Query query = entityManager.createNativeQuery("SELECT st.team_uuid as teamUuid, COUNT(*) FROM casework.stage st WHERE st.team_uuid IN ?1 AND st.deadline < CURRENT_DATE GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);

        query.unwrap(NativeQuery.class)
                .addScalar("teamUuid", UUIDCharType.INSTANCE)
                .addScalar("count", IntegerType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(Statistic.class));

        return (List<Statistic>) query
                .getResultList();
    }

    public List<Statistic> findOverdueUserCasesInTeams(Set<UUID> teamUuidSet, String userUuid) {
        Query query = entityManager.createNativeQuery("SELECT st.team_uuid as teamUuid, COUNT(*) FROM casework.stage st WHERE st.team_uuid in ?1 AND st.user_uuid = ?2 AND st.deadline < CURRENT_DATE GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);
        query.setParameter(2, userUuid);


        query.unwrap(NativeQuery.class)
                .addScalar("teamUuid", UUIDCharType.INSTANCE)
                .addScalar("count", IntegerType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(Statistic.class));

        return (List<Statistic>) query
                .getResultList();
    }

    public List<Statistic> findUserCasesInTeams(Set<UUID> teamUuidSet, String userUuid) {
        Query query = entityManager.createNativeQuery("SELECT CAST(st.team_uuid as varchar) as teamUuid, COUNT(*) FROM casework.stage st WHERE st.team_uuid in ?1 AND st.user_uuid = ?2 GROUP BY st.team_uuid");
        query.setParameter(1, teamUuidSet);
        query.setParameter(2, userUuid);

        query.unwrap(NativeQuery.class)
                .addScalar("teamUuid", UUIDCharType.INSTANCE)
                .addScalar("count", IntegerType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(Statistic.class));

        return (List<Statistic>) query
                .getResultList();
    }

}
