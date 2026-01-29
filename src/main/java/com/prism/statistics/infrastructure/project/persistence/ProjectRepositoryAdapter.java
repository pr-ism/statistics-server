package com.prism.statistics.infrastructure.project.persistence;

import static com.prism.statistics.domain.project.QProject.project;

import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final JpaProjectRepository jpaProjectRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Project save(Project project) {
        return jpaProjectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findIdByApiKey(String apiKey) {
        return Optional.ofNullable(
                queryFactory
                        .select(project.id)
                        .from(project)
                        .where(project.apiKey.eq(apiKey))
                        .fetchOne()
        );
    }

    @Override
    public List<Project> findByUserId(Long userId) {
        return jpaProjectRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findIdByProjectIdAndUserId(Long projectId, Long userId) {
        return Optional.ofNullable(
                queryFactory
                        .select(project.id)
                        .from(project)
                        .where(
                                project.id.eq(projectId),
                                project.userId.eq(userId)
                        )
                        .fetchOne()
        );
    }
}
