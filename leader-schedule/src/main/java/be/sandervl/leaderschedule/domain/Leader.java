package be.sandervl.leaderschedule.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PlanningEntity
@JsonIdentityInfo(scope = Leader.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "fullName")
public class Leader {

    @PlanningId
    private String fullName;

    private int experience = 0;
    private Map<String, Affinity> groupAffinityMap;
    private Set<Leader> preferredLeaders;
    private Set<Leader> unwantedLeaders;

    @InverseRelationShadowVariable(sourceVariableName = "leaders")
    @JsonIgnore
    private Group group;

    public Leader() {
    }

    public Leader(String fullName, int experience) {
        this.fullName = fullName;
        this.experience = experience;
        this.groupAffinityMap = new LinkedHashMap<>();
    }

    public Leader(String fullName, int experience, Map<String, Affinity> groupAffinityMap, Set<Leader> preferredLeaders, Set<Leader> unwantedLeaders, Group group) {
        this.fullName = fullName;
        this.experience = experience;
        this.groupAffinityMap = groupAffinityMap;
        this.preferredLeaders = preferredLeaders;
        this.unwantedLeaders = unwantedLeaders;
        this.group = group;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public Map<String, Affinity> getGroupAffinityMap() {
        return groupAffinityMap;
    }

    public void setGroupAffinityMap(Map<String, Affinity> groupAffinityMap) {
        this.groupAffinityMap = groupAffinityMap;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Set<Leader> getPreferredLeaders() {
        return preferredLeaders;
    }

    public void setPreferredLeaders(Set<Leader> preferredLeaders) {
        this.preferredLeaders = preferredLeaders;
    }

    public Set<Leader> getUnwantedLeaders() {
        return unwantedLeaders;
    }

    public void setUnwantedLeaders(Set<Leader> unwantedLeaders) {
        this.unwantedLeaders = unwantedLeaders;
    }
    // ************************************************************************
    // Complex methods
    // ************************************************************************

//    @JsonIgnore
//    public Affinity getAffinity(Group customer) {
//        Affinity affinity = groupAffinityMap.get(customer);
//        if (affinity == null) {
//            affinity = Affinity.NONE;
//        }
//        return affinity;
//    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leader leader)) return false;

        return fullName.equals(leader.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }
}
