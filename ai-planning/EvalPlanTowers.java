import fol.*;
import planning.*;
import search.*;

class EvalPlanTowers {
    public static void main(String[] args) {
        final Predicate Disk = new Predicate("Disk",1);
        final Predicate On = new Predicate("On",2);
        final Predicate Larger = new Predicate("LargerThan",2); 
        final Predicate Clear = new Predicate("Clear", 1);

        final Constant rod1 = new Constant("rod1");
        final Constant rod2 = new Constant("rod2");
        final Constant rod3 = new Constant("rod3");
        final Constant disk1 = new Constant("disk1");
        final Constant disk2 = new Constant("disk2");
        final Constant disk3 = new Constant("disk3");

        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final Variable z = new Variable("z");

        final ActionSchema move = 
            new ActionSchema("Move", 
            new Variable[]{x, y, z}, //disk x, former location y, new location z
            new Literal[]{new Literal(Disk, x), new Literal(Clear, x), new Literal(Clear, z), new Literal(On, x, y), new Literal(Larger, z, x)},
            new Atom[]{new Atom(On,x,y), new Atom(Clear, z)},
            new Atom[]{new Atom(On,x,z), new Atom(Clear, y)});

        final ActionSchema[] towerActions = {move};

        final Atom[] initialState1 = {
            new Atom(Disk, disk1), new Atom(Disk, disk2), new Atom(Disk, disk3),
            new Atom(On, disk1, rod1), new Atom(On, disk2, rod1), new Atom(On, disk3, rod1),
            new Atom(Clear, disk3), new Atom(Clear, disk2),
            new Atom(Larger, disk2, disk1), new Atom(Larger, disk3, disk1),
            new Atom(Larger, rod1, disk1), new Atom(Larger, rod2, disk1), new Atom(Larger, rod3, disk1),
            new Atom(Larger, rod1, disk2), new Atom(Larger, rod2, disk2), new Atom(Larger, rod3, disk2),
            new Atom(Larger, rod1, disk3), new Atom(Larger, rod2, disk3), new Atom(Larger, rod3, disk3),
            new Atom(Clear, rod2), new Atom(Clear, rod3),
        };

        final Literal[] goalState1 = {
            new Literal(On, disk1, rod3), new Literal(On, disk2, rod3), new Literal(On, disk3, rod3),
        };

        PlanningProblem towersThreeDisks = new PlanningProblem(initialState1, goalState1, towerActions);
        System.out.println("Initial State: ");
        for (Atom a: initialState1) {
            System.out.println(a);
        }
        System.out.println("Goal State: ");
        for (Literal l: goalState1) {
            System.out.println(l);
        }
        System.out.println("Plan: ");
        SearchProblem searchProblem = new ForwardStateSpaceSearch(towersThreeDisks);

        System.out.println("Uniform Cost: ");
        SearchMethod searchMethodUcost = new UniformCost(searchProblem);
        searchMethodUcost.search();

        System.out.println("A Star: ");
        SearchMethod searchMethodAstar = new AStar(searchProblem);
        searchMethodAstar.search();



    }
}
