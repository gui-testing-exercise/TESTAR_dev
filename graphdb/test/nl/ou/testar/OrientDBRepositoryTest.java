package nl.ou.testar;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test to validate the storage of State objects
 * A State will be stored ones. Every time the same state is stored a flag visited is updated.
 * Created by florendegier on 03/06/2017.
 */
public class OrientDBRepositoryTest {

    private OrientGraphFactory factory;

    private OrientDBRepository sut;


    @Before
    public void setup() {
        factory = new OrientGraphFactory("memory:demo");
        factory.setMaxRetries(10);
        factory.setupPool(1, 2);
        factory.setRequireTransaction(false);

        sut = new OrientDBRepository("memory:demo","admin","admin");
    }

    @After
    public void cleanup() {
        OrientGraph graph = factory.getTx();
        graph.drop();
        graph.shutdown();
        factory.close();
    }

    @Test
    public void testAddState() {

        StdState state = new StdState();
        state.set(Tags.ConcreteID, "0xCAFE");

        sut.addState(state);

        OrientGraph graph = factory.getTx();
        long data = graph.getRawGraph().countClass("State");
        assertEquals("The database shall contain one element", 1, data);
        graph.shutdown();

        sut.addState(state);

        graph = factory.getTx();
        data = graph.getRawGraph().countClass("State");
        assertEquals("The database shall contain one element", 1, data);

        state.set(Tags.ConcreteID, "0xDEAD");

        sut.addState(state);

        graph = factory.getTx();
        data = graph.getRawGraph().countClass("State");
        assertEquals("The database shall contain two elements", 2, data);

        graph.shutdown();

    }

    @Test
    public void testAddSAction() {

        Action action  = new NOP();
        action.set(Tags.ConcreteID,"0xDAAD");
        action.set(Tags.TargetID,"0xDADA");
        action.set(Tags.Desc,"test");
        State  from = new StdState();
        from.set(Tags.ConcreteID, "0xDEAD");
        State  to = new StdState();
        to.set(Tags.ConcreteID, "0xCAFE");

        Widget widget = new StdWidget();
        widget.set(Tags.ConcreteID,"0xDADA");

        sut.addState(from);
        sut.addWidget("0xDEAD",widget);
        sut.addState(to);

        sut.addAction("0xDADA",action,"0xCAFE");

    }

    @Test
    public void testAddActionWithMissingFromState() {
        try {
            Action action  = new NOP();
            action.set(Tags.Desc,"test");
            action.set(Tags.ConcreteID,"0xDAAD");
            action.set(Tags.TargetID,"0xCAFE");
            State  to = new StdState();
            to.set(Tags.ConcreteID, "0xCAFE");

            sut.addState(to);

            sut.addAction("0xDEAD",action,"0xCAFE");
            fail("addAction should throw an exception");
        } catch (Exception e) {
            assertTrue("Expect a GraphDBException", e instanceof  GraphDBException);
        }
    }

    @Test
    public void testAddActionWithMissingToState() {
        try {
            Action action  = new NOP();
            action.set(Tags.Desc,"test");
            action.set(Tags.ConcreteID,"0xDAAD");
            action.set(Tags.TargetID,"0xCAFE");
            State  from = new StdState();
            from.set(Tags.ConcreteID, "0xCAFE");

            sut.addState(from);

            sut.addAction("0xCAFE",action,"0xDEAD");
            fail("addAction should throw an exception");
        } catch (Exception e) {
            assertTrue("Expect a GraphDBException", e instanceof  GraphDBException);
        }
    }

}