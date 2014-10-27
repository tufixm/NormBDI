package jadex.web.examples.puzzle.agent;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.web.examples.puzzle.Board;
import jadex.web.examples.puzzle.Move;


/**
 *  Make a move and dispatch a subgoal for the next.
 */
public class MovePlan extends Plan
{
	//-------- attributes --------

	/** The move to try. */
	protected Move move;

	/** The board. */
	protected Board board;

	/** The deadline. */
	protected long deadline;

	//-------- constrcutors --------

	/**
	 *  Create a new move plan.
	 */
	public MovePlan()
	{
		this.move = (Move)getParameter("move").getValue();
		this.board = (Board)getParameter("board").getValue();
		this.deadline = ((Long)getParameter("deadline").getValue()).longValue();
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Make the move.
		board.move(move);
		
//		System.out.println("Move: "+(deadline-System.currentTimeMillis()));
		
		// Plan will be aborted when board is solution,
		// otherwise continue with next move
		IGoal mm = createGoal("makemove");
		mm.getParameter("board").setValue(board);
		mm.getParameter("deadline").setValue(deadline);	// Hack!!! Deadline of top goal should suffice.
		dispatchSubgoalAndWait(mm);
	}

	/**
	 *  The plan failure code.
	 */
	public void failed()
	{
		board.takeback();
	}
}
