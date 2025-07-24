package be.sandervl.leaderschedule.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.bendable.BendableScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import be.sandervl.leaderschedule.solver.LeaderScheduleConstraintProvider;

import java.util.List;

@PlanningSolution
public class LeaderScheduleSolution {

    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<Leader> leaders;

    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<Group> groups;

    @PlanningScore(
            bendableHardLevelsSize = LeaderScheduleConstraintProvider.BENDABLE_SCORE_HARD_LEVELS_SIZE,
            bendableSoftLevelsSize = LeaderScheduleConstraintProvider.BENDABLE_SCORE_SOFT_LEVELS_SIZE
    )
    private BendableScore score;

    private SolverStatus solverStatus;

    public LeaderScheduleSolution() {
    }

    public LeaderScheduleSolution(BendableScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public List<Leader> getLeaders() {
        return leaders;
    }

    public void setLeaders(List<Leader> leaders) {
        this.leaders = leaders;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
