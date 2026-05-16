package com.hireconnect.jobservice.specification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class JobSpecificationTest {

    @Mock
    private Root<Job> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate predicate;

    @Mock
    private Path<Object> path;

    @Test
    void filterOpenJobs_AllCriteria_ShouldAddPredicates() {
        Specification<Job> spec = JobSpecification.filterOpenJobs(
                "Java", "Bhopal", JobType.FULL_TIME, ExperienceLevel.FRESHER, 300000.0, 600000.0
        );

        Path stringPath = mock(Path.class);
        Path doublePath = mock(Path.class);
        Path genericPath = mock(Path.class);
        Order order = mock(Order.class);

        lenient().when(root.get(anyString())).thenReturn(genericPath);
        lenient().when(root.get("title")).thenReturn(stringPath);
        lenient().when(root.get("description")).thenReturn(stringPath);
        lenient().when(root.get("companyName")).thenReturn(stringPath);
        lenient().when(root.get("skillsRequired")).thenReturn(stringPath);
        lenient().when(root.get("location")).thenReturn(stringPath);
        lenient().when(root.get("salaryMin")).thenReturn(doublePath);
        lenient().when(root.get("salaryMax")).thenReturn(doublePath);

        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.lower(any())).thenReturn(stringPath);
        lenient().when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.greaterThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        lenient().when(cb.lessThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.desc(any())).thenReturn(order);

        spec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).equal(any(), eq(JobStatus.OPEN));
        verify(cb, atLeastOnce()).like(any(), contains("java"));
        verify(cb, atLeastOnce()).equal(any(), eq(JobType.FULL_TIME));
        verify(cb, atLeastOnce()).greaterThanOrEqualTo(any(), eq(300000.0));
        verify(query).orderBy(any(Order.class));
    }

    @Test
    void filterOpenJobs_NullCriteria_ShouldOnlyAddStatusPredicate() {
        Specification<Job> spec = JobSpecification.filterOpenJobs(
                null, null, null, null, null, null
        );

        Path genericPath = mock(Path.class);
        Order order = mock(Order.class);
        
        when(root.get("status")).thenReturn(genericPath);
        when(cb.equal(any(), eq(JobStatus.OPEN))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        when(cb.desc(any())).thenReturn(order);
        when(root.get("createdAt")).thenReturn(genericPath);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(any(), eq(JobStatus.OPEN));
        verify(cb, never()).like(any(), anyString());
        verify(query).orderBy(any(Order.class));
    }
}
