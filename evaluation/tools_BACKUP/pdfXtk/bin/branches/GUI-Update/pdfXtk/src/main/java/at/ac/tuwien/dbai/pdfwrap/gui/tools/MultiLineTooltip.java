package at.ac.tuwien.dbai.pdfwrap.gui.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for splitting up the tool tip into new lines.
 * 
 * @author Timo Schleicher
 *
 */
public class MultiLineTooltip {
	
    private static int DIALOG_TOOLTIP_MAX_SIZE = 65;

    /**
     * Splits a String in order to format a tool tip -> insert break line command
     * 
     * @param tip The tool tip you want to format
     * @return The formatted tool tip
     */
    public static String splitToolTip(String tip) {
    	
        return splitToolTip(tip,DIALOG_TOOLTIP_MAX_SIZE);
    }
    
    /**
     * Splits a String in order to format a tool tip -> insert break line command
     * 
     * @param tip The tool tip you want to format
     * @param length The length after that a new line break should be inserted
     * @return The formatted tool tip
     */
    public static String splitToolTip(String tip,int length) {
    	
    	//Return if there is no need to insert a line break
        if (tip.length() <= length) {
        	
            return tip;
        }

        //Each element of this list later stands for one line
        List<String> parts = new ArrayList<>();
        
        String[] splitTip = tip.split(" ");
        
        StringBuilder lines = new StringBuilder();
        
        for (int i = 0; i < splitTip.length; i++) {
        	
        	//Check whether the adding of the next word does exceed the maximal length
        	if (lines.length() + splitTip[i].length() < length) {
        		
        		lines.append(splitTip[i]);
        		lines.append(" ");
        		
        	} else {
        		
        		lines.deleteCharAt(lines.length()-1);
        		parts.add(lines.toString());
        		lines.setLength(0);
        		i--;
        	}
        }

		lines.deleteCharAt(lines.length()-1);
		parts.add(lines.toString());
		
		//Format by means of HTML tags
        StringBuilder sb = new StringBuilder("<html>");

        for (int i=0;i<parts.size() - 1;i++) {
        	
            sb.append(parts.get(i)+"<br>");
        }
        
        sb.append(parts.get(parts.size() - 1));
        sb.append(("</html>"));
        
        return sb.toString();
    }
}