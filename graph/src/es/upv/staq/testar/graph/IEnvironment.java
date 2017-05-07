/*****************************************************************************************
 *                                                                                       *
 * COPYRIGHT (2015):                                                                     *
 * Universitat Politecnica de Valencia                                                   *
 * Camino de Vera, s/n                                                                   *
 * 46022 Valencia, Spain                                                                 *
 * www.upv.es                                                                            *
 *                                                                                       * 
 * D I S C L A I M E R:                                                                  *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)     *
 * in the context of the TESTAR Proof of Concept project:                                *
 *               "UPV, Programa de Prueba de Concepto 2014, SP20141402"                  *
 * This graph project is distributed FREE of charge under the TESTAR license, as an open *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package es.upv.staq.testar.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

/**
 * Graph environment.
 * 
 * @author Urko Rueda Molina (alias: urueda)
 *
 */
public interface IEnvironment {
	
	/**
	 * Retrieves next movement.
	 * @return The movement.
	 */
	public Movement getMovement();
	
	/**
	 * Retrieves the corresponding graph state.
	 * @param state A state.
	 * @return The corresponding graph state.
	 */
	public IGraphState get(State state);
	
	/**
	 * Retrieves an existing graph state.
	 * @param stateID The state ID.
	 * @return The graph state.
	 */
	public IGraphState getState(String stateID);
	
	/**
	 * Retrieves the corresponding graph action.
	 * @param action An action.
	 * @return The corresponding graph action.
	 */
	public IGraphAction get(Action action);

	/**
	 * Retrieves an existing graph action.
	 * @param actionID The action ID.
	 * @return The graph action.
	 */
	public IGraphAction getAction(String actionID);
	
	/**
	 * Populates the graph with movement information.
	 * @param fromState Graph state before action execution.
	 * @param action Executed action, abstracted for graph.
	 * @param toState Graph state after action execution.
	 */
	public void populateEnvironment(IGraphState fromState, IGraphAction action, IGraphState toState);
	
	/**
	 * Retrieves information about an action set for a SUT state.
	 * @param state A SUT state.
	 * @param actions The actions available for the state.
	 */
	public void notifyEnvironment(State state, Set<Action> actions);
	
	/**
	 * Retrieves the graph states.
	 */
	public Collection<IGraphState> getGraphStates();

	/**
	 * Retrieves the graph actions.
	 */
	public Collection<IGraphAction> getGraphActions();
	
	/**
	 * Retrieves the source state of an action.
	 * @param action A graph action.
	 * @return The source graph state or 'null' if the action is not at graph.
	 */
	public IGraphState getSourceState(IGraphAction action);

	/**
	 * Retrieves the target state of an action.
	 * @param action A graph action.
	 * @return The target graph state or 'null' if the action is not at graph.
	 */
	public IGraphState getTargetState(IGraphAction action);
	
	/**
	 * Retrieves the incoming edges to a graph state.
	 * @param state A graph state.
	 * @return The incoming edges ids.
	 */
	public Collection<String> getIncomingActions(IGraphState state);
	
	/**
	 * Retrieves the outgoing edges from a graph state.
	 * @param state A graph state.
	 * @return The outgoing edges ids.
	 */
	public Collection<String> getOutgoingActions(IGraphState state);

	/**
	 * Computes the number of times an abstract action was executed.
	 * @param graphState A graph state.
	 * @param graphAction A state action.
	 * @return The count.
	 */
	public int getAbstractExecutionCount(IGraphState graphState, IGraphAction graphAction);	
	
	/**
	 * Gets the walked count for a graph action/edge.
	 * @param action The graph action.
	 * @return [0] = The number of times the state' action was walked in the graph.
	 *         [1] = The number of times the action type was walked in the graph.
	 */
	public int[] getWalkedCount(IGraphAction action);	
	
	/**
	 * Returns the number of left clicks in a state.
	 * @param state A state.
	 * @return The number of left clicks.
	 */
	public int getLeftClicks(IGraphState state);
	
	/**
	 * Returns the number of types in a state.
	 * @param state A state.
	 * @return The number of types.
	 */
	public int getTypesInto(IGraphState state);
			
	/**
	 * Retrieves all the scanned actions for a SUT state by decreased reward.
	 * @param state The SUT graph state.
	 * @return Actions osrted by decreased reward.
	 */
	//public IGraphAction[] getSortedStateActionsByDecReward(IGraphState state);
	
	/**
	 * Retrieves a list of incrementally ordered actions by their test execution order.
	 * Examples:
	 * 	* Full list: fromOder = 1, toOrder = executed_actions_number
	 *  * Sublist: fromOder = 1 &lt; X &lt; executed_actions_number, toOrder = X &lt; Y &lt; executed_actions_number.
	 *  * Single element: 1 &lt;= fromOrder = toOrder &lt;= executed_actions_number.
	 * @param fromOrder Retrieves the list from:  1 .. executed_actions_number.
	 * @param toOrder Retrieves the list to: fromOrder .. executed_actions_number.
	 * @return The list of actions.
	 */
	public IGraphAction[] getSortedActionsByOrder(int fromOrder, int toOrder);
	
	/**
	 * Retrieves a list of incrementally ordered actions by their test execution order.
	 * @return A forward iterator.
	 */
	public Iterator<IGraphAction> getForwardActions();
	
	/**
	 * Retrieves a list of decrementally ordered actions by their test execution order.
	 * @return A backward iterator.
	 */
	public ListIterator<IGraphAction> getBackwardActions();
	
	/**
	 * Checks whether the graph already contains a state.
	 * @param gs A graph state.
	 * @return 'true' if contained, 'false' otherwise.
	 */
	public boolean stateAtGraph(IGraphState gs);
	
	/**
	 * Checks whether the graph already contains an action.
	 * @param ga A graph action.
	 * @return 'true' if contained, 'false' otherwise.
	 */
	public boolean actionAtGraph(IGraphAction ga);
	
	/**
	 * Retrieves graph states grouped by clusters.
	 * @return Clusters of related UI states.
	 */
	public HashMap<String,Set<String>> getGraphStateClusters();

	/**
	 * Retrieves graph actions grouped by clusters.
	 * @return Clusters of related UI actions.
	 */
	public HashMap<String,Set<String>> getGraphActionClusters();
	
	/**
	 * Finish the graph environment with ending test sequence. 
	 * @param walkStatus Test verdict: 'true' test OK, 'false' test FAIL.
	 * @param lastState Last grah state.
	 * @param lastAction Last graph action.
	 * @param walkEndState SUT state after executing last action from the last state.
	 */
	public void finishGraph(boolean walkStatus, IGraphState lastState, IGraphAction lastAction, State walkEndState);
	
	/**
	 * Retrieves coverage metrics.
	 * @return Minimum and maximum coverage for all states.
	 */
	public double[] getCoverageMetrics();
	
	/**
	 * Retrieves graph resuming metrics.
	 * @return Known states, revisited states and new states.
	 */
	public int[] getGraphResumingMetrics();
	
	/**
	 * Retrieves data for the exploration curve.
	 * @return [0] unique_states, [1] unique_actions, [2] abstract_states, [3] abstract_actions
  	 */
	public List<int[]> getExplorationCurve();
	
	/**
	 * Gets the last sample from the GUI exploration curve.
	 * @return A text representation of the sample.
	 */
	public String getExplorationCurveSample();	
	
	/**
	 * Gest the last sample coverage from known UI space.
	 * @return The coverage as %.
	 */
	public int getExplorationCurveSampleCvg();

	/**
	 * Gets the last sample known UI space scale.
	 * @return The known UI space scale.
	 */
	public int getExplorationCurveSampleScale();

	/**
	 * Retrieves the current longest path in the GUI explored space.
	 * @return A text representation of the list of states in the longest path.
	 */
	public String getLongestPath();
	
	/**
	 * Retrieves the current longest path length in the GUI explored space.
	 * @return The length of the longest path.
	 */
	public int getLongestPathLength();
	
	/**
	 * Retrieves a path of states between two graph states.
	 * @param from Starting graph state.
	 * @param to Ending graph state.
	 * @return The list of ordered states between states or 'null' if no path exists.
	 */
	public List<IGraphState> getPath(IGraphState from, IGraphState to);
	
	/**
	 * Gets the previous state from the execution.
	 * @param graphState A graph state.
	 * @return The previous state.
	 */
	public IGraphState getPrevious(IGraphState graphState);
	
	/**
	 * Retrieves the N ancestors of a state.
	 * @param graphState The graph state.
	 * @param n The number N of ancestors to retrieve.
	 * @return The list of N ancestors.
	 */
	public List<IGraphState> getAncestors(IGraphState graphState, int n);
	
	/**
	 * Gets the execution number of a state' action.
	 * @param graphAction The graph action executed at a state.
	 * @return The number of times the action was executed at a state.
	 */
	public int getExecutionNumber(IGraphAction graphAction);

	/**
	 * Retrieves the number of different actions that were executed in a state.
	 * @param graphState The graph state.
	 * @return The number of different actions executed at the state.
	 */
	public int getExecutedActionNumber(IGraphState graphState);
	
	/**
	 * Retrieves the number of actions that were not executed in a state.
	 * These actions were discovered, but never executed.
	 * @param graphState The graph state.
	 * @return The number of unexecuted actions in the state.
	 */
	public int getUnexecutedActionNumber(IGraphState graphState);
	
	/**
	 * Loads a graph from xml.
	 * @param xmlPath The xml file path.
	 * @return Number of graph movements from the loaded graph.
	 */
	public int loadFromXML(String xmlPath);
	
}