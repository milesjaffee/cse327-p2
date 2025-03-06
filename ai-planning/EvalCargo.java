import fol.*;
import planning.*;
import search.*;

class EvalCargo {
    public static void main(String[] args) {
        final Predicate In = new Predicate("In",2); //cargo in plane
        final Predicate At = new Predicate("At",2);
        final Predicate Plane = new Predicate("Plane",1);
        final Predicate Airport = new Predicate("Airport", 1);
        final Predicate Cargo = new Predicate("Cargo", 1);

        final Constant p1 = new Constant("p1");
        final Constant p2 = new Constant("p2");
        final Constant c1 = new Constant("c1");
        final Constant c2 = new Constant("c2");
        final Constant c3 = new Constant("c3");
        final Constant jfk = new Constant("JFK");
        final Constant ord = new Constant("ORD");
        final Constant lax = new Constant("LAX");
        final Constant abe = new Constant("ABE");

        final Variable x = new Variable("x");
        final Variable y = new Variable("y");
        final Variable z = new Variable("z");

        final ActionSchema load = 
            new ActionSchema("Load", 
            new Variable[]{x, y, z}, //cargo x, plane y, airport z
            new Literal[]{new Literal(At, x, z), new Literal(At,y,z), new Literal(Cargo,x), new Literal(Plane,y), new Literal(Airport,z)},
            new Atom[]{new Atom(In,x,y)},
            new Atom[]{new Atom(At,x,z)});

        final ActionSchema unload = 
            new ActionSchema("Unload", 
            new Variable[]{x, y, z}, //cargo x, plane y, airport z
            new Literal[]{new Literal(In,x,y), new Literal(At,y,z), new Literal(Cargo,x), new Literal(Plane,y), new Literal(Airport,z)},
            new Atom[]{new Atom(At,x,z)},
            new Atom[]{new Atom(In,x,y)});

        final ActionSchema fly = 
            new ActionSchema("Fly", 
            new Variable[]{x, y, z}, //plane x, old airport y, new airport z
            new Literal[]{new Literal(Plane,x), new Literal(Airport,y), new Literal(Airport,z), new Literal(At,x,y)},
            new Atom[]{new Atom(At,x,z)},
            new Atom[]{new Atom(At,x,y)});   

        final ActionSchema[] planeActions = {load, unload, fly};

        final Atom[] initialState1 = {
            new Atom(At, p1, lax), new Atom(At, p2, jfk), new Atom(At, c1, lax),
            new Atom(In, c2, p1),
            new Atom(Plane, p1), new Atom(Plane, p2),
            new Atom(Cargo, c1), new Atom(Cargo, c2),
            new Atom(Airport, jfk), new Atom(Airport, lax), new Atom(Airport, ord),
        };

        final Literal[] goalState1 = {
            new Literal(At, p1, jfk),
            new Literal(At, c1, jfk),
        };
        
        final Atom[] initialState2 = {
            new Atom(At, p1, lax),
            new Atom(At, p2, jfk),
            new Atom(At, c1, lax),
            new Atom(In, c2, p1),
            new Atom(Plane, p1),
            new Atom(Plane, p2),
            new Atom(Cargo, c1),
            new Atom(Cargo, c2),
            new Atom(Airport, jfk),
            new Atom(Airport, lax),
            new Atom(Airport, ord)
        };
        
        final Literal[] goalState2 = {
            new Literal(At, p1, jfk),
            new Literal(At, p2, ord),
            new Literal(At, c1, jfk),
            new Literal(In, c2, p2)
        };
        
        final Atom[] initialState3 = {
            new Atom(At, p1, lax),
            new Atom(At, c1, abe),
            new Atom(At, c2, jfk),
            new Atom(At, c3, ord),
            new Atom(Plane, p1),
            new Atom(Cargo, c1),
            new Atom(Cargo, c2),
            new Atom(Cargo, c3),
            new Atom(Airport, jfk),
            new Atom(Airport, lax),
            new Atom(Airport, ord),
            new Atom(Airport, abe)
        };
        
        final Literal[] goalState3 = {
            new Literal(At, c1, lax),
            new Literal(At, c2, lax),
            new Literal(At, c3, lax)
        };

        PlanningProblem problem1 = new PlanningProblem(initialState1, goalState1, planeActions);
        PlanningProblem problem2 = new PlanningProblem(initialState2, goalState2, planeActions);
        PlanningProblem problem3 = new PlanningProblem(initialState3, goalState3, planeActions);

        PlanningProblem[] problems = {problem1, problem2, problem3};

        for (int i = 1; i <= 3; i ++) {
            SearchProblem searchProblem = new ForwardStateSpaceSearch(problems[i-1]);

            System.out.println("Uniform Cost Prob "+i);
            SearchMethod searchMethodUcost = new UniformCost(searchProblem);
            searchMethodUcost.search();

            System.out.println("A Star Prob "+i);
            SearchMethod searchMethodAstar = new AStar(searchProblem);
            searchMethodAstar.search();
        }

    }
}
