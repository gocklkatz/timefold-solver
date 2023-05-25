package ai.timefold.solver.core.impl.exhaustivesearch;

import static ai.timefold.solver.core.impl.testdata.util.PlannerAssert.assertCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import ai.timefold.solver.core.api.score.buildin.simple.SimpleScore;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.config.exhaustivesearch.ExhaustiveSearchPhaseConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;
import ai.timefold.solver.core.impl.exhaustivesearch.decider.ExhaustiveSearchDecider;
import ai.timefold.solver.core.impl.exhaustivesearch.node.ExhaustiveSearchLayer;
import ai.timefold.solver.core.impl.exhaustivesearch.node.ExhaustiveSearchNode;
import ai.timefold.solver.core.impl.exhaustivesearch.scope.ExhaustiveSearchPhaseScope;
import ai.timefold.solver.core.impl.exhaustivesearch.scope.ExhaustiveSearchStepScope;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import ai.timefold.solver.core.impl.heuristic.selector.entity.EntitySelector;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirector;
import ai.timefold.solver.core.impl.testdata.domain.TestdataEntity;
import ai.timefold.solver.core.impl.testdata.domain.TestdataSolution;
import ai.timefold.solver.core.impl.testdata.domain.TestdataValue;
import ai.timefold.solver.core.impl.testdata.domain.pinned.TestdataPinnedEntity;
import ai.timefold.solver.core.impl.testdata.domain.pinned.TestdataPinnedSolution;
import ai.timefold.solver.core.impl.testdata.util.PlannerTestUtils;

import org.junit.jupiter.api.Test;

class DefaultExhaustiveSearchPhaseTest {

    @Test
    void restoreWorkingSolution() {
        ExhaustiveSearchPhaseScope<TestdataSolution> phaseScope = mock(ExhaustiveSearchPhaseScope.class);
        ExhaustiveSearchStepScope<TestdataSolution> lastCompletedStepScope = mock(ExhaustiveSearchStepScope.class);
        when(phaseScope.getLastCompletedStepScope()).thenReturn(lastCompletedStepScope);
        ExhaustiveSearchStepScope<TestdataSolution> stepScope = mock(ExhaustiveSearchStepScope.class);
        when(stepScope.getPhaseScope()).thenReturn(phaseScope);
        TestdataSolution workingSolution = new TestdataSolution();
        when(phaseScope.getWorkingSolution()).thenReturn(workingSolution);
        InnerScoreDirector<TestdataSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        when(phaseScope.getScoreDirector()).thenReturn((InnerScoreDirector) scoreDirector);

        SolutionDescriptor<TestdataSolution> solutionDescriptor = TestdataSolution.buildSolutionDescriptor();
        when(phaseScope.getSolutionDescriptor()).thenReturn(solutionDescriptor);

        ExhaustiveSearchLayer layer0 = new ExhaustiveSearchLayer(0, mock(Object.class));
        ExhaustiveSearchLayer layer1 = new ExhaustiveSearchLayer(1, mock(Object.class));
        ExhaustiveSearchLayer layer2 = new ExhaustiveSearchLayer(2, mock(Object.class));
        ExhaustiveSearchLayer layer3 = new ExhaustiveSearchLayer(3, mock(Object.class));
        ExhaustiveSearchLayer layer4 = new ExhaustiveSearchLayer(4, mock(Object.class));
        ExhaustiveSearchNode node0 = new ExhaustiveSearchNode(layer0, null);
        node0.setMove(mock(Move.class));
        node0.setUndoMove(mock(Move.class));
        ExhaustiveSearchNode node1 = new ExhaustiveSearchNode(layer1, node0);
        node1.setMove(mock(Move.class));
        node1.setUndoMove(mock(Move.class));
        ExhaustiveSearchNode node2A = new ExhaustiveSearchNode(layer2, node1);
        node2A.setMove(mock(Move.class));
        node2A.setUndoMove(mock(Move.class));
        ExhaustiveSearchNode node3A = new ExhaustiveSearchNode(layer3, node2A); // oldNode
        node3A.setMove(mock(Move.class));
        node3A.setUndoMove(mock(Move.class));
        ExhaustiveSearchNode node2B = new ExhaustiveSearchNode(layer2, node1);
        node2B.setMove(mock(Move.class));
        node2B.setUndoMove(mock(Move.class));
        ExhaustiveSearchNode node3B = new ExhaustiveSearchNode(layer3, node2B);
        node3B.setMove(mock(Move.class));
        node3B.setUndoMove(mock(Move.class));
        ExhaustiveSearchNode node4B = new ExhaustiveSearchNode(layer4, node3B); // newNode
        node4B.setMove(mock(Move.class));
        node4B.setUndoMove(mock(Move.class));
        node4B.setScore(SimpleScore.ofUninitialized(-96, 7));
        when(lastCompletedStepScope.getExpandingNode()).thenReturn(node3A);
        when(stepScope.getExpandingNode()).thenReturn(node4B);

        DefaultExhaustiveSearchPhase<TestdataSolution> phase = new DefaultExhaustiveSearchPhase.Builder<>(0, "", null, null,
                mock(EntitySelector.class), mock(ExhaustiveSearchDecider.class)).build();
        phase.restoreWorkingSolution(stepScope);

        verify(node0.getMove(), times(0)).doMove(any(ScoreDirector.class));
        verify(node0.getUndoMove(), times(0)).doMoveOnly(any(ScoreDirector.class));
        verify(node1.getMove(), times(0)).doMove(any(ScoreDirector.class));
        verify(node1.getUndoMove(), times(0)).doMoveOnly(any(ScoreDirector.class));
        verify(node2A.getMove(), times(0)).doMove(any(ScoreDirector.class));
        verify(node2A.getUndoMove(), times(1)).doMoveOnly(scoreDirector);
        verify(node3A.getMove(), times(0)).doMove(any(ScoreDirector.class));
        verify(node3A.getUndoMove(), times(1)).doMoveOnly(scoreDirector);
        verify(node2B.getMove(), times(1)).doMoveOnly(scoreDirector);
        verify(node2B.getUndoMove(), times(0)).doMoveOnly(any(ScoreDirector.class));
        verify(node3B.getMove(), times(1)).doMoveOnly(scoreDirector);
        verify(node3B.getUndoMove(), times(0)).doMoveOnly(any(ScoreDirector.class));
        verify(node4B.getMove(), times(1)).doMoveOnly(scoreDirector);
        verify(node4B.getUndoMove(), times(0)).doMoveOnly(any(ScoreDirector.class));
        // TODO FIXME
        // verify(workingSolution).setScore(newScore);
    }

    @Test
    void solveWithInitializedEntities() {
        SolverConfig solverConfig = PlannerTestUtils.buildSolverConfig(TestdataSolution.class,
                TestdataEntity.class);
        solverConfig.setPhaseConfigList(Collections.singletonList(new ExhaustiveSearchPhaseConfig()));

        TestdataSolution solution = new TestdataSolution("s1");
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        solution.setValueList(Arrays.asList(v1, v2, v3));
        solution.setEntityList(Arrays.asList(
                new TestdataEntity("e1", null),
                new TestdataEntity("e2", v2),
                new TestdataEntity("e3", v1)));

        solution = PlannerTestUtils.solve(solverConfig, solution);
        assertThat(solution).isNotNull();
        TestdataEntity solvedE1 = solution.getEntityList().get(0);
        assertCode("e1", solvedE1);
        assertThat(solvedE1.getValue()).isNotNull();
        TestdataEntity solvedE2 = solution.getEntityList().get(1);
        assertCode("e2", solvedE2);
        assertThat(solvedE2.getValue()).isEqualTo(v2);
        TestdataEntity solvedE3 = solution.getEntityList().get(2);
        assertCode("e3", solvedE3);
        assertThat(solvedE3.getValue()).isEqualTo(v1);
        assertThat(solution.getScore().initScore()).isEqualTo(0);
    }

    @Test
    void solveWithPinnedEntities() {
        SolverConfig solverConfig =
                PlannerTestUtils.buildSolverConfig(TestdataPinnedSolution.class, TestdataPinnedEntity.class);
        solverConfig.setPhaseConfigList(Collections.singletonList(new ExhaustiveSearchPhaseConfig()));

        TestdataPinnedSolution solution = new TestdataPinnedSolution("s1");
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        solution.setValueList(Arrays.asList(v1, v2, v3));
        solution.setEntityList(Arrays.asList(
                new TestdataPinnedEntity("e1", null, false, false),
                new TestdataPinnedEntity("e2", v2, true, false),
                new TestdataPinnedEntity("e3", null, false, true)));

        solution = PlannerTestUtils.solve(solverConfig, solution);
        assertThat(solution).isNotNull();
        TestdataPinnedEntity solvedE1 = solution.getEntityList().get(0);
        assertCode("e1", solvedE1);
        assertThat(solvedE1.getValue()).isNotNull();
        TestdataPinnedEntity solvedE2 = solution.getEntityList().get(1);
        assertCode("e2", solvedE2);
        assertThat(solvedE2.getValue()).isEqualTo(v2);
        TestdataPinnedEntity solvedE3 = solution.getEntityList().get(2);
        assertCode("e3", solvedE3);
        assertThat(solvedE3.getValue()).isEqualTo(null);
        assertThat(solution.getScore().initScore()).isEqualTo(-1);
    }

    @Test
    void solveWithEmptyEntityList() {
        SolverConfig solverConfig =
                PlannerTestUtils.buildSolverConfig(TestdataSolution.class, TestdataEntity.class);
        solverConfig.setPhaseConfigList(Collections.singletonList(new ExhaustiveSearchPhaseConfig()));

        TestdataSolution solution = new TestdataSolution("s1");
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        solution.setValueList(Arrays.asList(v1, v2, v3));
        solution.setEntityList(Collections.emptyList());

        solution = PlannerTestUtils.solve(solverConfig, solution, false);
        assertThat(solution).isNotNull();
        assertThat(solution.getEntityList()).isEmpty();
    }

}
