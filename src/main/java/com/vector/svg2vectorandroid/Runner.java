package com.vector.svg2vectorandroid;

import org.apache.commons.cli.*;

import java.io.IOException;


/**
 * Created by ravi on 19/12/17.
 */
public class Runner {

    public static void main(String args[]){

        Transformer transformer = null;
        Options options = new Options();
        options.addOption(
                Option.builder("w")
                        .longOpt("width")
                        .desc("android:width value.")
                        .required(false)
                        .hasArg()
                        .argName("dimension")
                        .valueSeparator()
                        .build()
        );
        options.addOption(
                Option.builder("h")
                        .longOpt("height")
                        .desc("android:height value.")
                        .required(false)
                        .hasArg()
                        .argName("dimension")
                        .valueSeparator()
                        .build()
        );
        options.addOption(
                Option.builder("i")
                        .longOpt("input_dir")
                        .desc("The directory containing the svg files.")
                        .required(true)
                        .hasArg()
                        .argName("path")
                        .valueSeparator()
                        .build()
        );
        options.addOption(
                Option.builder("o")
                        .longOpt("output_dir")
                        .desc("The directory to which the xml vector files are written.")
                        .required(true)
                        .hasArg()
                        .argName("path")
                        .valueSeparator()
                        .build()
        );
        options.addOption(
                Option.builder("m")
                        .longOpt("mapping")
                        .desc("A mapping file for attributes.")
                        .required(false)
                        .hasArg()
                        .argName("path")
                        .valueSeparator()
                        .build()
        );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        HelpFormatter hf = new HelpFormatter();

        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.out.println("Command line arg parsing failed.  Reason: " + exp.getMessage());
            hf.printHelp("svg2vec", options);
            System.exit(1);
        }

        if (cmdLine.hasOption("m")) {
            try {
                transformer = new Transformer(cmdLine.getOptionValue("m"));
            } catch (IOException e) {
                System.out.println("Your mapping file is invalid. Reason: " + e.getMessage());
                System.exit(1);
            }
        }

        String sourceDirectory = cmdLine.getOptionValue('i');
        String targetDirectory = cmdLine.getOptionValue('o');

        try {
            SvgFilesProcessor processor = new SvgFilesProcessor(sourceDirectory, targetDirectory, transformer);
            processor.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
