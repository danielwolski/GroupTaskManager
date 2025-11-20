package com.grouptaskmanager.service;

import com.grouptaskmanager.repository.DailyTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyTaskResetterTest {

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @InjectMocks
    private DailyTaskResetter dailyTaskResetter;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testResetAtTheStartIfNecessary() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        dailyTaskResetter.resetAtTheStartIfNecessary();

        // Then
        verify(dailyTaskRepository, times(1)).setIsDoneToFalseIfNecessary(today);
    }

    @Test
    void testResetDailyTasksOnNextDay() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        dailyTaskResetter.resetDailyTasksOnNextDay();

        // Then
        verify(dailyTaskRepository, times(1)).setAllIsDoneToFalse(today);
    }
}
