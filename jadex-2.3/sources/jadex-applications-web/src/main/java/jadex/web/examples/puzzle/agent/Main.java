package jadex.web.examples.puzzle.agent;

import java.util.SortedSet;

import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ThreadSuspendable;
import jadex.web.examples.puzzle.Board;
import jadex.web.examples.puzzle.HighscoreEntry;
import jadex.web.examples.puzzle.IPuzzleService;
import jadex.web.examples.puzzle.Move;

/**
 *  Simple test for the puzzle agent.
 * @author Alex
 *
 */
public class Main
{
	public static void	main(String[] args)
	{
		args	= new String[]
		{
//			"-logging_level", "java.util.logging.Level.INFO",
			"-awareness", "false",
			"-gui", "false",
			"-extensions", "null",
			"-component", "jadex/web/examples/puzzle/agent/Sokrates.agent.xml"
		};
		int	timeout	= 300000;
		ThreadSuspendable	sus	= new ThreadSuspendable();
		IExternalAccess	platform	= Starter.createPlatform(args).get(sus, timeout);
		IPuzzleService	puzzle	= SServiceProvider.getService(platform.getServiceProvider(), IPuzzleService.class).get(sus, timeout);
		
		Board	board	= new Board(11);
		int	hints	= 0;
		while( !board.isSolution())
		{
			Move	move	= puzzle.hint(board, 15000).get(sus, timeout);
			hints++;
			System.out.println("Move "+hints+": "+move);
			board.move(move);
		}
		
		try
		{
			puzzle.addHighscore(new HighscoreEntry(platform.getComponentIdentifier().getLocalName(), board.getSize(), hints)).get(sus, timeout);
			System.out.println("New highscore entry!");
		}
		catch(RuntimeException e)
		{
			System.out.println("Sorry, no new highscore entry.");
		}
		
		SortedSet<HighscoreEntry>	entries	= puzzle.getHighscore(board.getSize()).get(sus, timeout);
		int place	= 1;
		for(HighscoreEntry entry : entries)
		{
			System.out.println(""+(place++)+": "+entry.getName()+" used "+entry.getHintCount()+" hints on "+entry.getDate());
		}
		
		platform.killComponent().get(sus, timeout);
	}
}
