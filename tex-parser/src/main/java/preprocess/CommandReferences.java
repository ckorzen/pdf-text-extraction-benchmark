package preprocess;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.freiburg.iif.path.LineReader;
import model.Command;

/**
 * 
 * Class holding all command references.
 * 
 * @author Claudius Korzen
 */
public class CommandReferences {
  
  protected Map<String, CommandReference> references;
  
  public CommandReferences(String path, String separator) {
    this.references = read(path, separator);
  }
  
  /**
   * Reads the commands to consider.
   * 
   * @throws URISyntaxException
   */
  protected Map<String, CommandReference> read(final String path, 
      final String separator) {
    final Map<String, CommandReference> references = new HashMap<>();
    
    // Read the command references file line by line.
    LineReader reader = new LineReader() {
      public void handleLine(String line) {
        // Ignore comment lines.
        if (line.startsWith("#")) {
          return;
        }

        // Ignore empty lines.
        if (line.trim().isEmpty()) {
          return;
        }

        String[] fields = line.split(separator, -1);
        CommandReference reference = new CommandReference(fields);
        references.put(reference.getCommandName(), reference);
      }
    };
    
    InputStream is = getClass().getResourceAsStream(path);
    reader.read(is);
    
    try {
      is.close();
    } catch (IOException e) {
      return references;
    }
    
    return references;
  }

  // ___________________________________________________________________________

  /**
   * Returns true, if the given command has a command reference.
   */
  protected boolean hasCommandReference(Command command) {
    return get(command) != null;
  }

  /**
   * Returns the command reference for the given command.
   */
  public CommandReference get(Command command) {
    if (command == null) {
      return null;
    }

    if (references == null) {
      return null;
    }
    
    if (references.containsKey(command.toShortString())) {
      return references.get(command.toShortString());
    }
    return references.get(command.getName());
  }
}