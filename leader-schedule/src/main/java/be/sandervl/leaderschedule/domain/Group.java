package be.sandervl.leaderschedule.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@PlanningEntity
@JsonIdentityInfo(scope = Group.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class Group {

    @PlanningId
    private String name;
    private int minimumLeaders;
    private int maximumLeaders;

    @PlanningListVariable(allowsUnassignedValues = false)
    private List<Leader> leaders;

    public Group() {
    }

    public Group(String name, int minimumLeaders, int maximumLeaders) {
        this.name = name;
        this.minimumLeaders = minimumLeaders;
        this.maximumLeaders = maximumLeaders;
        this.leaders = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinimumLeaders() {
        return minimumLeaders;
    }

    public void setMinimumLeaders(int minimumLeaders) {
        this.minimumLeaders = minimumLeaders;
    }

    public int getMaximumLeaders() {
        return maximumLeaders;
    }

    public void setMaximumLeaders(int maximumLeaders) {
        this.maximumLeaders = maximumLeaders;
    }

    public List<Leader> getLeaders() {
        return leaders;
    }

    public void setLeaders(List<Leader> leaders) {
        this.leaders = leaders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Group customer))
            return false;
        return Objects.equals(getName(), customer.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
