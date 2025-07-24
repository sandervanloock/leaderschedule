package be.sandervl.leaderschedule.solver;

import ai.timefold.solver.core.api.score.buildin.bendable.BendableScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import be.sandervl.leaderschedule.domain.Affinity;
import be.sandervl.leaderschedule.domain.Group;
import be.sandervl.leaderschedule.domain.Leader;
import be.sandervl.leaderschedule.domain.LeaderScheduleSolution;

public class LeaderScheduleConstraintProvider implements ConstraintProvider {

    public static final int BENDABLE_SCORE_HARD_LEVELS_SIZE = 1;
    public static final int BENDABLE_SCORE_SOFT_LEVELS_SIZE = 2;

    private static int getPreferredMatches(Group group) {
        var result = 0;
        for (Leader a : group.getLeaders()) {
            for (Leader b : group.getLeaders()) {
                if (a.getPreferredLeaders() != null && a.getPreferredLeaders().contains(b)) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                minimumNumberOfLeaders(constraintFactory),
                maximumNumberOfLeaders(constraintFactory),
                maximizeGroupAffinity(constraintFactory),
                noUnwantedLeader(constraintFactory),
                balanceExperience(constraintFactory),
//                larsHasSpeelclub(constraintFactory),
                preferredLeader(constraintFactory),
                atLeastExperience(constraintFactory),
        };
    }

    protected Constraint minimumNumberOfLeaders(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .filter(group -> group.getLeaders().size() < group.getMinimumLeaders())
                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint("minimum leaders not respected");
    }

    protected Constraint maximumNumberOfLeaders(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .filter(group -> group.getLeaders().size() > group.getMaximumLeaders())
                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint("maximum leaders not respected");
    }

    protected Constraint noUnwantedLeader(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .filter(group -> group.getLeaders().stream().anyMatch(a -> group.getLeaders().stream().anyMatch(b -> a.getUnwantedLeaders() != null && a.getUnwantedLeaders().contains(b))))
                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint("unwanted leader not respected");
    }

    protected Constraint atLeastExperience(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .filter(group -> group.getLeaders().stream().mapToInt(Leader::getExperience).sum() <= 0)
                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint("At least 1 year experience");
    }

//    protected Constraint larsHasSpeelclub(ConstraintFactory constraintFactory) {
//        return constraintFactory.forEach(Leader.class)
//                .filter(leader -> leader.getFullName().equals("Lars") && !leader.getGroup().getName().equals("Speelclub"))
//                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
//                .asConstraint("Lars must be in Speelclub");
//    }

    protected Constraint maximizeGroupAffinity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .reward(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1),
                        group -> group.getLeaders().stream().map(Leader::getGroupAffinityMap).mapToInt(affinity -> affinity.getOrDefault(group.getName(), Affinity.NONE).getDurationMultiplier()).sum())
                .asConstraint("Maximize group affinity");
    }

    protected Constraint preferredLeader(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .reward(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 1, 1),
                        LeaderScheduleConstraintProvider::getPreferredMatches)
                .asConstraint("preferred leaders respected");
    }

    protected Constraint balanceExperience(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Group.class)
                .filter(g -> g.getLeaders().stream().map(Leader::getExperience).reduce(0, Integer::sum) <= 1)
                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint("balanced experience");
    }



}
