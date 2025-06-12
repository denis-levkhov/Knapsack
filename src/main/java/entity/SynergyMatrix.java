package entity;

public class SynergyMatrix {
    private final double[][] synergy;

    public SynergyMatrix(int size) {
        this.synergy = new double[size][size];
    }

    public void setSynergy(int i, int j, double value) {
        synergy[i][j] = value;
        synergy[j][i] = value;
    }

    public double getSynergy(int i, int j) {
        return synergy[i][j];
    }

    public int getSize() {
        return synergy.length;
    }
}