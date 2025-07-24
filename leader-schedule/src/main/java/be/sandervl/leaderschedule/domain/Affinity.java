package be.sandervl.leaderschedule.domain;

public enum Affinity {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int durationMultiplier;

    Affinity(int durationMultiplier) {
        this.durationMultiplier = durationMultiplier;
    }

    public int getDurationMultiplier() {
        return durationMultiplier;
    }
}
