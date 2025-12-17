package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      if (args.length != 3 || args==null) {
        System.err.println("Error: Expected 3 arguments: <threads> <input.json> <output.json>");
            return;
      }
       final String threadsStr = args[0];
        final String inputPath  = args[1];
        final String outputPath = args[2];
          final int numThreads;
        try {
            numThreads = Integer.parseInt(threadsStr);
            if (numThreads <= 0) throw new NumberFormatException("threads must be > 0");
             } catch (NumberFormatException e) {
            try {
               OutputWriter.write("Invalid number of threads: " + threadsStr, outputPath);
             } catch (IOException io) {
               System.err.println("Failed to write error output: " + io.getMessage());
        }
         return; 
        }
        try{
         InputParser parser = new InputParser();
          ComputationNode rootNode = parser.parse(inputPath);
          rootNode.associativeNesting();
          LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);
          ComputationNode resultNode = engine.run(rootNode);
          OutputWriter.write(resultNode.getMatrix(), outputPath);
           }
           catch(ParseException e){
            try {
               OutputWriter.write("Failed to parse input file: " + e.getMessage(), outputPath);
             } catch (IOException io) {
               System.err.println("Failed to write error output: " + io.getMessage());
             }
            } catch (IOException e) {
            try {
               OutputWriter.write("I/O error: " + e.getMessage(), outputPath);
             } catch (IOException io) {
               System.err.println("Failed to write error output: " + io.getMessage());
             }
             catch (Exception e2) {
            try {
                OutputWriter.write("Unexpected error: " + e2.getMessage(), outputPath);
            } catch (IOException io) {
                System.err.println("Failed to write error output: " + io.getMessage());
            }

          }
  }
    }

          
       
    
}