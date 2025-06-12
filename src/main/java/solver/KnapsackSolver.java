package solver;

import entity.Item;

import java.util.List;

public interface KnapsackSolver {
    public List<Item> solve(List<Item> items, double[][] synergyMatrix, double capacity);
}
