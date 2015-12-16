package edu.isi.bmkeg.lapdf.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;

public class CustomLapdfEngine extends LapdfEngine {

  public CustomLapdfEngine() throws Exception {
    super();
  }

  @Override
  public String readBasicText(LapdfDocument document) 
      throws IOException,FileNotFoundException {

    List<Set<String>> stack = new ArrayList<Set<String>>();
    
    Set<String> sections = new HashSet<String>();   
    sections.add(ChunkBlock.TYPE_BODY);
    sections.add(ChunkBlock.TYPE_HEADING);
    stack.add(sections);
    
//    sections = new HashSet<String>();   
//    sections.add(ChunkBlock.TYPE_FIGURE_LEGEND);
//    stack.add(sections);
        
    return this.readClassifiedText(document, stack);
    
  }
}
