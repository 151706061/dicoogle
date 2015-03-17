/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle.
 *
 * Dicoogle/dicoogle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dcm4che2.data.TransferSyntax;
import pt.ua.dicoogle.plugins.PluginController;
import pt.ua.dicoogle.plugins.ServiceController;
import pt.ua.dicoogle.sdk.datastructs.Report;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.task.Task;

/**
 * Main class for Dicoogle
 * @author Frederico Valente
 * @author Filipe Freitas
 * @author Luís A. Bastião Silva <bastiao@ua.pt>
 * @author Samuel Campos <samuelcampos@ua.pt>
 */
public class Dicoogle
{
    
    PluginController pluginController = new PluginController(new File("./plugins"));
    ServiceController serviceController = new ServiceController();
    
    /**
     * Inits application
     * parses command line arguments, launches tasks and waits for their completion
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        System.setProperty("log4j.configurationFile", "log4j-2.xml");

        try{
            Dicoogle dicoogle = new Dicoogle();
            System.err.println("Parsing");
            for(String arg : args){
                System.err.println(arg);
            }
            
            
            List<Task<Report>> tasks = dicoogle.parseCommandLine(args);
            System.err.println("Done Parsing");
            dicoogle.initialize();
            dicoogle.serviceController.manageJettyPlugins(dicoogle.pluginController.getJettyPlugins());
            dicoogle.serviceController.manageRestPlugins(dicoogle.pluginController.getRestPlugins());
            
            System.err.println("Pre loop");
            
            for(Task<Report> forest : tasks){
                System.err.println("ON TASK RUNNER");
                Report r = forest.get();
                System.out.println(r);
            }            
            System.err.println("aft loop");
        }
        catch (Exception ex) {
            Logger.getLogger(Dicoogle.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
        }
                               
        ExceptionHandler.registerExceptionHandler();
    }


    public void initialize()
    {
        //what is this? should be moved somewhere else...
        TransferSyntax.add(new TransferSyntax("1.2.826.0.1.3680043.2.682.1.40", false,false, false, true));
        TransferSyntax.add(new TransferSyntax("1.2.840.10008.1.2.4.70", true,false, false, true));
        TransferSyntax.add(new TransferSyntax("1.2.840.10008.1.2.5.50", false,false, false, true));    



        // Lauch Async Index 
        // It monitors a folder, and when a file is touched an event
        // triggers and index is updated.
        
        //TODO: This SHOULD be parametrized and created somewhere else depending
        //on commandline/config file paramenters
//        AsyncIndex asyncIndex = new AsyncIndex();
    }

    private List<Task<Report>> parseCommandLine(String[] args) throws URISyntaxException {
        ArrayList<Task<Report>> actions = new ArrayList<>();
        String workingDirectoryPath = "./";
        int i=0;
        while(i< args.length){
            switch(args[i]){
                //set working directory
                case "-w": //todo: make logs write into working directory
                    workingDirectoryPath = args[i+1];
                    i+=2;
                    break;
                    
                //set loggers to verborse and output to stdout
                case "-v": break;
                
                
                //index
                case "-i":{
                    Task<Report> indexTask = pluginController.indexClosure("lucene", new URI(args[i+1]));
                    actions.add(indexTask);
                    i+=2;
                    break;
                }
                
                //index using specific plugin
                case "-in": break;
                    
                //execute query
                case "-q": {
                    if(i+1 >= args.length) {
                        System.err.println("Query parameter requires a query expression");
                        System.exit(1);
                    }
                    Task task = pluginController.queryClosure("lucene",args[i+1] );
                    actions.add(task);
                    i+=2;
                    break;
                }
                    
                //await previous instructions
                case "-barrier": break;
 
                //initializes services
                case "-s":
                    
                    
                //terminate after this instruction
                case "-e": break;
                
                //use this config file
                case "-c": break;
                
                default:
                    i++;
                
            }
            
            
        }
        return actions;
    }
}