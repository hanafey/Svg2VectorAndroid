package com.vector.svg2vectorandroid;

import java.io.IOException;

/**
 * Created by ravi on 19/12/17.
 */
public class Runner {

    public static void main(String args[]){

        if(args.length == 0){
            System.out.println(" Provide source directory as first arguement for svg files to be converted\n example: java -jar Svg2VectorAndroid-1.0.jar <SourceDirectoryPath> ");
            return;
        }

        String sourceDirectory = args[0];

        try {
            SvgFilesProcessor processor = new SvgFilesProcessor(sourceDirectory);
            processor.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
